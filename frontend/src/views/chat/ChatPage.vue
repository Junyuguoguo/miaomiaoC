<template>
  <div class="chat-container">
    <!-- Header -->
    <div class="chat-header">
      <button class="header-back" @click="$router.push('/chat')">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M12.5 15l-5-5 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <h3 class="header-title">{{ chatTitle }}</h3>
      <span class="status-dot" :class="connectionStatus ? 'online' : 'offline'" :title="connectionStatus ? '已连接' : '未连接'"></span>
    </div>

    <!-- Message list -->
    <div class="message-list" ref="messageListRef">
      <div v-if="hasMore" class="load-more">
        <button class="load-more-btn" @click="loadMoreMessages" :disabled="loadingMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>

      <template v-for="(msg, idx) in messages" :key="msg.id">
        <!-- Time separator -->
        <div v-if="shouldShowTime(idx)" class="time-separator">
          <span class="time-label">{{ formatTimeFull(msg.createTime) }}</span>
        </div>

        <div
          class="message-row"
          :class="msg.senderId === currentUserId ? 'self' : ''"
          :style="{ animationDelay: Math.min(idx * 40, 400) + 'ms' }"
        >
          <img
            class="msg-avatar"
            :class="{ clickable: msg.senderId !== currentUserId }"
            :src="fixAvatarUrl(msg.senderAvatar)"
            alt="avatar"
            @click="msg.senderId !== currentUserId && goPrivateChat(msg.senderId, msg.senderName)"
          />
          <div class="msg-body">
            <div class="message-meta">
              <span class="sender-name">{{ getSenderName(msg) }}</span>
              <span v-if="msg.senderRole === 4" class="role-badge admin-badge">管理员</span>
              <span v-else-if="msg.senderRole === 3" class="role-badge teacher-badge">教师</span>
              <span v-else-if="msg.senderRole === 2" class="role-badge vip-badge">VIP学生</span>
              <span class="level-badge">{{ getSenderLevel(msg) }}</span>
              <span class="message-time">{{ formatMessageTime(msg.createTime) }}</span>
            </div>
            <div class="bubble" :class="getBubbleClass(msg)">
              {{ msg.content }}
            </div>
            <div class="message-actions">
              <button
                type="button"
                class="message-action"
                :class="{ active: isMessageLiked(msg) }"
                @click="toggleMessageLike(msg)"
              >
                <span>赞</span>
                <strong v-if="getMessageLikeCount(msg)">{{ getMessageLikeCount(msg) }}</strong>
              </button>
              <button type="button" class="message-action" @click="replyToMessage(msg)">
                回复
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Extension panel -->
    <div v-if="showPanel" class="extension-panel">
      <div class="panel-item" @click="onPanelAction('camera')">
        <div class="panel-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z" stroke="var(--app-primary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="13" r="4" stroke="var(--app-primary)" stroke-width="1.5"/></svg>
        </div>
        <span class="panel-label">拍照</span>
      </div>
      <div class="panel-item" @click="onPanelAction('album')">
        <div class="panel-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="var(--app-primary)" stroke-width="1.5"/><circle cx="8.5" cy="8.5" r="1.5" fill="var(--app-primary)"/><path d="M21 15l-5-5L5 21" stroke="var(--app-primary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <span class="panel-label">相册</span>
      </div>
      <div class="panel-item" @click="onPanelAction('location')">
        <div class="panel-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 1118 0z" stroke="var(--app-primary)" stroke-width="1.5"/><circle cx="12" cy="10" r="3" stroke="var(--app-primary)" stroke-width="1.5"/></svg>
        </div>
        <span class="panel-label">位置</span>
      </div>
    </div>

    <!-- Input bar -->
    <div class="input-bar">
      <button class="image-btn" @click="onPanelAction('album')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="var(--app-primary)" stroke-width="1.8"/><circle cx="8.5" cy="8.5" r="1.5" fill="var(--app-primary)"/><path d="M21 15l-5-5L5 21" stroke="var(--app-primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <input
        ref="messageInputRef"
        v-model="inputMessage"
        class="msg-input"
        placeholder="输入消息..."
        @keydown.enter.exact.prevent="sendMessage"
        @focus="showPanel = false"
      />
      <button
        v-if="inputMessage.trim()"
        class="send-btn"
        @click="sendMessage"
      >发送</button>
      <button
        v-else
        class="plus-btn"
        :class="{ active: showPanel }"
        @click="showPanel = !showPanel"
      >+</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import chatWebSocket from '@/utils/chat-websocket'
import { getPrivateMessages, getRoomMessages, markAsRead, markAllAsRead } from '@/api/chat'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// State
const messages = ref([])
const inputMessage = ref('')
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(0)
const messageListRef = ref(null)
const messageInputRef = ref(null)
const connectionStatus = ref(false)
const showPanel = ref(false)
const likedMessageKeys = ref([])

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const fixAvatarUrl = (url) => {
  if (!url) return defaultAvatar
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

const goPrivateChat = (userId, userName) => {
  const query = { userId }
  if (userName) query.userName = userName
  router.push({ path: '/chat/private', query })
}

// Computed
const currentUserId = computed(() => userStore.getUserId)

const getBubbleClass = (msg) => {
  const classes = []
  if (msg.senderId === currentUserId.value) {
    classes.push('bubble-self')
  } else {
    classes.push('bubble-peer')
  }
  if (msg.senderRole === 2) classes.push('bubble-vip')
  else if (msg.senderRole === 3) classes.push('bubble-teacher')
  else if (msg.senderRole === 4) classes.push('bubble-admin')
  return classes.join(' ')
}
const chatType = computed(() => {
  if (route.path.includes('/chat/private')) return 'private'
  if (route.path.includes('/chat/room')) return 'room'
  return 'private'
})
const chatTitle = computed(() => {
  if (chatType.value === 'private') return route.query.userName || '用户'
  if (chatType.value === 'room') return route.query.roomName || '群聊'
  return '聊天'
})

// Time helpers
const formatTimeFull = (time) => {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  const hm = d.format('HH:mm')
  if (d.isSame(now, 'day')) return hm
  if (d.isSame(now.subtract(1, 'day'), 'day')) return `昨天 ${hm}`
  return d.format('MM/DD HH:mm')
}

const shouldShowTime = (idx) => {
  if (idx === 0) return true
  const prev = dayjs(messages.value[idx - 1].createTime)
  const curr = dayjs(messages.value[idx].createTime)
  return curr.diff(prev, 'minute') > 5
}

const getSenderName = (msg) => {
  if (msg.senderId === currentUserId.value) return userStore.getUserName || msg.senderName || '我'
  return msg.senderName || '匿名用户'
}

const getSenderLevel = (msg) => {
  const level = msg.senderLevel || msg.level || msg.userLevel
  if (level) return String(level).startsWith('LV.') ? level : `LV.${level}`
  if (msg.senderRole === 4) return 'LV.9'
  if (msg.senderRole === 3) return 'LV.8'
  if (msg.senderRole === 2) return 'LV.6'
  return 'LV.4'
}

const formatMessageTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

const getMessageKey = (msg) => String(msg.id || `${msg.senderId}-${msg.createTime}-${msg.content}`)

const isMessageLiked = (msg) => likedMessageKeys.value.includes(getMessageKey(msg))

const getMessageLikeCount = (msg) => {
  const base = Number(msg.likeCount ?? msg.likes ?? 0)
  return base + (isMessageLiked(msg) ? 1 : 0)
}

const toggleMessageLike = (msg) => {
  const key = getMessageKey(msg)
  if (likedMessageKeys.value.includes(key)) {
    likedMessageKeys.value = likedMessageKeys.value.filter(item => item !== key)
    return
  }
  likedMessageKeys.value = [...likedMessageKeys.value, key]
}

const replyToMessage = (msg) => {
  const prefix = `回复 ${getSenderName(msg)}：`
  inputMessage.value = inputMessage.value.trim()
    ? `${inputMessage.value} ${prefix}`
    : prefix
  nextTick(() => {
    messageInputRef.value?.focus?.()
  })
}

// Load messages
const loadMessages = async (page = 0) => {
  try {
    let response
    if (chatType.value === 'private') {
      response = await getPrivateMessages(route.query.userId, page, 20)
    } else if (chatType.value === 'room') {
      response = await getRoomMessages(route.query.roomId, page, 20)
    }
    if (response && response.code === 200) {
      const newMessages = response.data.content
      if (page === 0) {
        messages.value = newMessages.reverse()
      } else {
        messages.value = [...newMessages.reverse(), ...messages.value]
      }
      hasMore.value = !response.data.last
      currentPage.value = page
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

const loadMoreMessages = async () => {
  loadingMore.value = true
  try {
    await loadMessages(currentPage.value + 1)
  } finally {
    loadingMore.value = false
  }
}

// Send message
const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  if (!chatWebSocket.isConnected) {
    ElMessage.warning('未连接到服务器，正在重连...')
    connectWebSocket()
    return
  }
  const content = inputMessage.value.trim()
  const messageData = { messageType: 'TEXT', content }

  // Optimistic render — show message immediately
  const optimisticMsg = {
    id: Date.now(),
    senderId: currentUserId.value,
    senderName: userStore.getUserName || '',
    senderAvatar: userStore.getUserAvatar || '',
    content,
    createTime: new Date().toISOString(),
    mine: true
  }
  messages.value.push(optimisticMsg)
  nextTick(() => scrollToBottom())

  let sent = false
  if (chatType.value === 'private') {
    messageData.receiverId = Number(route.query.userId)
    sent = chatWebSocket.sendPrivateMessage(messageData)
  } else if (chatType.value === 'room') {
    messageData.roomId = Number(route.query.roomId)
    sent = chatWebSocket.sendRoomMessage(messageData)
  }
  if (sent) {
    inputMessage.value = ''
    showPanel.value = false
  } else {
    // Remove optimistic message on failure
    messages.value.pop()
    ElMessage.error('消息发送失败')
  }
}

// Receive message
const handleMessageReceived = (message) => {
  // Dedup: skip if message with same id already exists
  if (message.id && messages.value.some(m => m.id === message.id)) return
  // Replace optimistic message if it matches
  const last = messages.value[messages.value.length - 1]
  if (last && last.mine && last.content === message.content && message.senderId === currentUserId.value) {
    const idx = messages.value.length - 1
    messages.value[idx] = message
    return
  }
  messages.value.push(message)
  nextTick(() => scrollToBottom())
  if (message.senderId !== currentUserId.value) {
    markAsRead([message.id]).catch(console.error)
  }
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// Extension panel actions
const onPanelAction = (type) => {
  ElMessage.info('功能开发中')
  showPanel.value = false
}

// WebSocket
const connectWebSocket = async () => {
  try {
    const userId = currentUserId.value
    const token = userStore.token
    if (!token || !userId) {
      ElMessage.error('登录信息异常，请重新登录')
      return
    }
    await chatWebSocket.connect(token, userId)
    connectionStatus.value = true
    chatWebSocket.subscribePrivateMessages(userId, handleMessageReceived)
    if (chatType.value === 'room' && route.query.roomId) {
      chatWebSocket.subscribeRoomMessages(Number(route.query.roomId), handleMessageReceived)
    }
    chatWebSocket.onError((error) => {
      ElMessage.error(error.message || '消息发送失败')
    })
  } catch (error) {
    console.error('WebSocket 连接失败:', error)
    ElMessage.error('实时消息连接失败，历史消息仍可查看')
  }
}

onMounted(async () => {
  await loadMessages(0)
  if (chatType.value === 'private' && route.query.userId) {
    markAllAsRead(Number(route.query.userId)).catch(console.error)
  }
  await connectWebSocket()
})

onUnmounted(() => {
  // chatWebSocket.disconnect() — keep connection alive if desired
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, var(--app-bg) 100%);
  position: relative;
}

/* --- Header --- */
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  height: 50px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
  z-index: 10;
}

.header-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #333;
  cursor: pointer;
  transition: background 0.15s;
}

.header-back:hover {
  background: #e8e8e8;
}

.header-title {
  flex: 1;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.online {
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

.status-dot.offline {
  background: #ef4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* --- Message list --- */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  scroll-behavior: smooth;
}

.load-more {
  text-align: center;
  padding: 12px 0 8px;
}

.load-more-btn {
  border: none;
  background: var(--app-surface-muted);
  color: var(--app-text-muted);
  font-size: 13px;
  padding: 6px 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.load-more-btn:hover:not(:disabled) {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

/* --- Time separator --- */
.time-separator {
  display: flex;
  justify-content: center;
  margin: 12px 0;
}

.time-label {
  font-size: 12px;
  color: var(--app-text-muted);
  background: var(--app-surface-muted);
  padding: 3px 12px;
  border-radius: 10px;
}

/* --- Message row --- */
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
  animation: slideIn 300ms cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.message-row.self {
  flex-direction: row-reverse;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(40, 78, 142, 0.12);
}

.msg-avatar.clickable {
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}

.msg-avatar.clickable:hover {
  opacity: 0.8;
  transform: scale(1.08);
}

.msg-body {
  max-width: min(76%, 760px);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin: 0;
}

.message-row.self .msg-body {
  align-items: flex-end;
}

.message-meta {
  width: 100%;
  min-height: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.message-row.self .message-meta {
  justify-content: flex-end;
}

.sender-name {
  display: inline-flex;
  max-width: 160px;
  margin: 0;
  color: #627087;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  margin-left: 4px;
  color: #9aa8bc;
  font-weight: 650;
}

/* --- Bubble --- */
.bubble,
.bubble-peer,
.bubble-self,
.bubble-vip.bubble-peer,
.bubble-teacher.bubble-peer,
.bubble-admin.bubble-peer,
.bubble-vip.bubble-self,
.bubble-teacher.bubble-self,
.bubble-admin.bubble-self {
  max-width: 100%;
  min-height: 36px;
  padding: 8px 16px;
  border-radius: 15px;
  color: #1f2a44;
  background: #f4f7fb;
  border: 1px solid #e2eaf5;
  box-shadow: 0 8px 20px rgba(40, 78, 142, 0.08);
  line-height: 1.62;
  word-break: break-word;
  text-align: left;
}

.bubble-self,
.bubble-vip.bubble-self,
.bubble-teacher.bubble-self,
.bubble-admin.bubble-self {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, #2f7df5, #7560f5);
}

/* --- Message actions --- */
.message-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.message-row.self .message-actions {
  justify-content: flex-end;
}

.message-action {
  min-width: 42px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 0 10px;
  border: 1px solid #cfe0f6;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: #687891;
  font-size: 12px;
  font-weight: 850;
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease, color 160ms ease, background 160ms ease;
}

.message-action:hover {
  color: var(--app-primary);
  border-color: rgba(37, 99, 235, 0.36);
  background: #fff;
  transform: translateY(-1px);
}

.message-action.active {
  color: var(--app-primary);
  border-color: rgba(37, 99, 235, 0.42);
  background: var(--app-primary-soft);
}

.message-action strong {
  font-size: 12px;
}

/* --- Role Badges --- */
.level-badge,
.role-badge {
  display: inline-flex;
  align-items: center;
  height: 18px;
  padding: 0 7px;
  border: 0;
  border-radius: 999px;
  font-size: 11px;
  line-height: 18px;
  font-weight: 850;
  margin-left: 0;
  vertical-align: baseline;
}

.level-badge {
  color: #4672f6;
  background: #e8efff;
}

.vip-badge {
  color: #d97706;
  background: #fff4d6;
}

.teacher-badge {
  color: #0891b2;
  background: #dff7fb;
}

.admin-badge {
  color: #7c3aed;
  background: #f0e8ff;
}

/* Extension panel */
.extension-panel {
  display: flex;
  justify-content: space-around;
  padding: 20px 40px;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  animation: slideUp 250ms ease-out;
}

.panel-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.panel-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eee;
  transition: transform 0.15s;
}

.panel-icon:hover {
  transform: scale(1.05);
}

.panel-label {
  font-size: 12px;
  color: #999;
}

@keyframes slideUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* Input bar */
.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
  z-index: 10;
}

.image-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ddd;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.15s;
}

.image-btn:hover {
  border-color: #95EC69;
}

.msg-input {
  flex: 1;
  height: 38px;
  padding: 0 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: border-color 0.2s;
}

.msg-input::placeholder {
  color: #bbb;
}

.msg-input:focus {
  border-color: #95EC69;
}

.send-btn {
  height: 38px;
  padding: 0 18px;
  border: none;
  border-radius: 8px;
  background: #07c160;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.15s;
  animation: fadeIn 150ms ease;
}

.send-btn:hover {
  opacity: 0.85;
}

.plus-btn {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ddd;
  color: #999;
  font-size: 20px;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.2s, border-color 0.15s;
}

.plus-btn:hover {
  border-color: #95EC69;
  color: #07c160;
}

.plus-btn.active {
  transform: rotate(45deg);
  border-color: #07c160;
  color: #07c160;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>
