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
          class="message-item"
          :class="[msg.senderId === currentUserId ? 'message-self' : 'message-other', 'animate']"
          :style="{ animationDelay: Math.min(idx * 30, 300) + 'ms' }"
        >
          <div class="message-avatar">
            <el-avatar :size="42" class="avatar-img" :class="msg.senderId === currentUserId ? 'avatar-self' : 'avatar-peer'">
              <img v-if="msg.senderAvatar" :src="fixAvatarUrl(msg.senderAvatar)" alt="avatar" />
              <span v-else>{{ msg.senderName?.charAt(0) || 'U' }}</span>
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="bubble" :class="getBubbleClass(msg)">
              <span v-if="msg.senderRole === 4" class="role-badge admin-badge">管理员</span>
              <span v-else-if="msg.senderRole === 3" class="role-badge teacher-badge">教师</span>
              <span v-else-if="msg.senderRole === 2" class="role-badge vip-badge">VIP</span>
              {{ msg.content }}
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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import chatWebSocket from '@/utils/chat-websocket'
import { getPrivateMessages, getRoomMessages, markAsRead, markAllAsRead } from '@/api/chat'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

// State
const messages = ref([])
const inputMessage = ref('')
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(0)
const messageListRef = ref(null)
const connectionStatus = ref(false)
const showPanel = ref(false)

const fixAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
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
  padding: 0 20px;
  height: 56px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
  z-index: 10;
}

.header-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--app-text);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.header-back:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

.header-title {
  flex: 1;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
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

/* --- Message item --- */
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
  opacity: 0;
}

.message-item.animate {
  animation: slideIn 300ms cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.message-self {
  flex-direction: row-reverse;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* --- Avatar --- */
.message-avatar {
  flex-shrink: 0;
}

.avatar-img {
  font-weight: 700;
  font-size: 16px;
}

.avatar-peer {
  border: 2px solid var(--app-primary-soft);
}

.avatar-self {
  border: 2px solid var(--app-primary);
}

/* --- Bubble --- */
.message-body {
  max-width: 60%;
}

.bubble {
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  position: relative;
}

.bubble-peer {
  background: #fff;
  color: var(--app-text);
  border-radius: 16px 16px 16px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.bubble-peer::before {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 4px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 8px 8px 0;
  border-color: transparent #fff transparent transparent;
}

.bubble-self {
  background: var(--app-primary-soft, #e8f0ff);
  color: var(--app-text, #102033);
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.08);
  border: 1px solid rgba(37, 99, 235, 0.12);
}

.bubble-self::after {
  content: '';
  position: absolute;
  right: -8px;
  bottom: 4px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 0 8px 8px;
  border-color: transparent transparent transparent #dce8f8;
}

/* --- Extension panel --- */
.extension-panel {
  display: flex;
  justify-content: space-around;
  padding: 24px 40px;
  background: #fff;
  border-top: 1px solid var(--app-border);
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
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: var(--app-primary-soft);
  transition: transform 0.2s, box-shadow 0.2s;
}

.panel-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.panel-label {
  font-size: 12px;
  color: var(--app-text-muted);
}

@keyframes slideUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* --- Input bar --- */
.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid var(--app-border);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
  z-index: 10;
}

.image-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: var(--app-primary-soft);
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s;
}

.image-btn:hover {
  background: var(--app-primary);
}

.image-btn:hover svg path,
.image-btn:hover svg rect,
.image-btn:hover svg circle {
  stroke: #fff;
}

.msg-input {
  flex: 1;
  height: 40px;
  padding: 0 16px;
  border: 1px solid var(--app-border);
  border-radius: 20px;
  background: var(--app-surface-muted);
  font-size: 14px;
  color: var(--app-text);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.msg-input::placeholder {
  color: var(--app-text-muted);
}

.msg-input:focus {
  border-color: var(--app-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.send-btn {
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 20px;
  background: var(--app-primary, #2563eb);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s, transform 0.15s;
  animation: fadeIn 200ms ease;
}

.send-btn:hover {
  opacity: 0.9;
}

.send-btn:active {
  transform: scale(0.96);
}

.plus-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: var(--app-surface-muted);
  color: var(--app-text-muted);
  font-size: 22px;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.25s ease, background 0.2s;
}

.plus-btn:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

.plus-btn.active {
  transform: rotate(45deg);
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

/* === Role Badges === */
.role-badge {
  display: inline-block;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  font-weight: 700;
  margin-left: 6px;
  vertical-align: middle;
  letter-spacing: 0.3px;
}

.vip-badge {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
}

.teacher-badge {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
}

.admin-badge {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: #fff;
}

/* === Role-Specific Bubbles (peer only) === */
.bubble-vip.bubble-peer {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #fbbf24;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.12);
}

.bubble-vip.bubble-peer::before {
  border-color: transparent #fef3c7 transparent transparent;
}

.bubble-teacher.bubble-peer {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  border: 1px solid #34d399;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.12);
}

.bubble-teacher.bubble-peer::before {
  border-color: transparent #d1fae5 transparent transparent;
}

.bubble-admin.bubble-peer {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #a78bfa;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.12);
}

.bubble-admin.bubble-peer::before {
  border-color: transparent #ede9fe transparent transparent;
}

/* === Role-Specific Bubbles (self) === */
.bubble-vip.bubble-self {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.2);
}

.bubble-vip.bubble-self::after {
  border-color: transparent transparent transparent #d97706;
}

.bubble-teacher.bubble-self {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

.bubble-teacher.bubble-self::after {
  border-color: transparent transparent transparent #059669;
}

.bubble-admin.bubble-self {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
}

.bubble-admin.bubble-self::after {
  border-color: transparent transparent transparent #7c3aed;
}
</style>
