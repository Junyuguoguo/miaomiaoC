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
              <span class="message-time">{{ formatMessageTime(msg.createTime) }}</span>
            </div>
            <div v-if="msg.recalled" class="bubble" :class="[getBubbleClass(msg), 'bubble-recalled']">
              <span class="recalled-text">{{ msg.senderId === currentUserId ? '你' : msg.senderName || '对方' }}撤回了一条消息</span>
              <button v-if="msg.senderId === currentUserId" type="button" class="re-edit-btn" @click="reEditMessage(msg)">重新编辑</button>
            </div>
            <div v-else class="bubble" :class="[getBubbleClass(msg), { 'code-bubble-shell': shouldRenderCode(msg) }]">
              <!-- 代码消息 -->
              <div v-if="shouldRenderCode(msg)" class="bubble-code">
                <div class="code-header">
                  <div class="code-title">
                    <span class="code-file-icon">&lt;/&gt;</span>
                    <span class="code-filename">{{ getCodeTitle(msg) }}</span>
                    <span class="code-language">{{ getCodeLanguage(msg) }}</span>
                  </div>
                  <a v-if="isDownloadableCode(msg)" :href="fixFileUrl(msg.content)" download class="code-download">下载</a>
                </div>
                <pre class="code-preview"><code>{{ getCodeDisplayContent(msg) || '代码文件已上传，点击下载查看完整内容。' }}</code></pre>
              </div>
              <!-- 图片消息 -->
              <div v-else-if="msg.messageType === 'IMAGE'" class="bubble-image">
                <img :src="fixFileUrl(msg.content)" alt="图片" @click="previewImageUrl = fixFileUrl(msg.content)" />
              </div>
              <!-- 普通文件消息 -->
              <div v-else-if="msg.messageType === 'FILE'" class="bubble-file">
                <div class="file-icon">&#128196;</div>
                <div class="file-info">
                  <span class="file-name">{{ msg.fileName || '文件' }}</span>
                  <span class="file-size">{{ formatFileSize(msg.fileSize) }}</span>
                </div>
                <a :href="fixFileUrl(msg.content)" download class="file-download-btn">下载</a>
              </div>
              <!-- 文本消息 -->
              <template v-else>{{ msg.content }}</template>
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
              <button
                v-if="msg.senderId === currentUserId && !msg.recalled && canRecall(msg)"
                type="button"
                class="message-action recall-action"
                @click="handleRecall(msg)"
              >撤回</button>
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
      <input ref="fileInputRef" type="file" style="display:none" accept="image/*,.c,.cpp,.h,.hpp,.java,.py,.js,.ts,.html,.css,.json,.xml,.sql,.sh,.go,.rs,.txt" @change="handleFileUpload" />
      <textarea
        ref="messageInputRef"
        v-model="inputMessage"
        class="msg-input"
        rows="1"
        placeholder="输入消息，粘贴代码后 Shift+Enter 换行..."
        @keydown.enter.exact.prevent="sendMessage"
        @focus="showPanel = false"
      ></textarea>
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

    <!-- Image preview overlay -->
    <div v-if="previewImageUrl" class="image-preview-overlay" @click.self="previewImageUrl = ''">
      <img :src="previewImageUrl" class="image-preview-full" @wheel.prevent="handlePreviewWheel" />
      <button class="image-preview-close" @click="previewImageUrl = ''">&times;</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import chatWebSocket from '@/utils/chat-websocket'
import { getPrivateMessages, getRoomMessages, markAsRead, markAllAsRead, uploadChatFile, recallMessage } from '@/api/chat'
import request from '@/utils/request'
import {
  getCodeDisplayContent,
  getCodeLanguage,
  getCodeTitle,
  isDownloadableCode,
  looksLikeCode,
  readCodeFilePreview,
  shouldRenderCodeMessage
} from '@/utils/chat-code'
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
const fileInputRef = ref(null)
const uploading = ref(false)
const previewImageUrl = ref('')

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const fixAvatarUrl = (url) => {
  if (!url) return defaultAvatar
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

const fixFileUrl = (url) => {
  if (!url) return ''
  return url
}

const shouldRenderCode = (msg) => shouldRenderCodeMessage(msg)

const formatFileSize = (bytes) => {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handlePreviewWheel = (e) => {
  const img = e.currentTarget
  const scale = parseFloat(img.dataset.scale || '1')
  const delta = e.deltaY > 0 ? -0.15 : 0.15
  const next = Math.min(Math.max(0.3, scale + delta), 3)
  img.dataset.scale = next
  img.style.transform = `scale(${next})`
}

onMounted(async () => {
  await loadMessages(0)
  if (chatType.value === 'private' && route.query.userId) {
    markAllAsRead(Number(route.query.userId)).catch(console.error)
  }
  await connectWebSocket()
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && previewImageUrl.value) previewImageUrl.value = ''
  })
})

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
  const messageType = looksLikeCode(content) ? 'CODE' : 'TEXT'
  const codeContent = messageType === 'CODE' ? content : ''
  const messageData = { messageType, content, codeContent }

  // Optimistic render — show message immediately
  const optimisticMsg = {
    id: Date.now(),
    senderId: currentUserId.value,
    senderName: userStore.getUserName || '',
    senderAvatar: userStore.getUserAvatar || '',
    messageType,
    content,
    codeContent,
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
  // Handle recall event: update existing message in-place
  if (message.recalled) {
    const idx = messages.value.findIndex(m => m.id === message.id)
    if (idx !== -1) {
      messages.value[idx] = { ...messages.value[idx], ...message }
      return
    }
  }

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

// ──────────────── Recall ────────────────

const canRecall = (msg) => {
  if (!msg.createTime) return false
  const diff = Date.now() - new Date(msg.createTime).getTime()
  return diff < 2 * 60 * 1000
}

const handleRecall = async (msg) => {
  try {
    const res = await recallMessage(msg.id)
    if (res && res.code === 200) {
      const idx = messages.value.findIndex(m => m.id === msg.id)
      if (idx !== -1) {
        messages.value[idx] = { ...messages.value[idx], recalled: true }
      }
      ElMessage.success('消息已撤回')
    } else {
      ElMessage.error(res?.message || '撤回失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '撤回失败，可能已超过2分钟')
  }
}

const reEditMessage = (msg) => {
  inputMessage.value = msg.content || ''
  nextTick(() => messageInputRef.value?.focus?.())
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// Extension panel actions
const onPanelAction = (type) => {
  if (type === 'album' || type === 'camera') {
    fileInputRef.value?.click()
  } else {
    ElMessage.info('功能开发中')
  }
  showPanel.value = false
}

const handleFileUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  e.target.value = ''
  uploading.value = true
  try {
    const localCodeContent = await readCodeFilePreview(file)
    const res = await uploadChatFile(file)
    if (res && res.code === 200) {
      const { url, messageType, fileName, fileSize, codeContent } = res.data
      sendFileMessage(messageType, url, fileName, fileSize, codeContent || localCodeContent)
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const sendFileMessage = (messageType, url, fileName, fileSize, codeContent = '') => {
  if (!chatWebSocket.isConnected) {
    ElMessage.warning('未连接到服务器')
    return
  }
  const messageData = { messageType, content: url, fileName, fileSize, codeContent }

  // Optimistic render
  const optimisticMsg = {
    id: Date.now(),
    senderId: currentUserId.value,
    senderName: userStore.getUserName || '',
    senderAvatar: userStore.getUserAvatar || '',
    content: url,
    messageType,
    fileName,
    fileSize,
    codeContent,
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
  if (!sent) {
    messages.value.pop()
    ElMessage.error('消息发送失败')
  }
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

/* --- Recalled bubble --- */
.bubble-recalled {
  background: #f0f0f0 !important;
  border: 1px dashed #d0d5dd !important;
  color: #94a3b8 !important;
  box-shadow: none !important;
}

.recalled-text {
  font-size: 13px;
  font-style: italic;
}

.re-edit-btn {
  margin-left: 8px;
  padding: 2px 8px;
  border: 1px solid var(--app-primary);
  border-radius: 4px;
  background: transparent;
  color: var(--app-primary);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.15s;
}

.re-edit-btn:hover {
  background: var(--app-primary-soft);
}

.recall-action {
  color: #e74c3c;
  border-color: #f5c6cb;
}

.recall-action:hover {
  color: #c0392b;
  border-color: #e74c3c;
  background: #fdf0ef;
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
  align-items: flex-end;
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
  min-height: 38px;
  max-height: 112px;
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  line-height: 20px;
  font-family: inherit;
  color: #333;
  outline: none;
  resize: none;
  overflow-y: auto;
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

/* --- Image bubble --- */
.bubble-image img {
  max-width: 240px;
  max-height: 240px;
  border-radius: 8px;
  cursor: pointer;
  display: block;
}

.bubble-image img:hover {
  opacity: 0.9;
}

/* --- Code bubble --- */
.bubble.code-bubble-shell,
.bubble-peer.code-bubble-shell,
.bubble-self.code-bubble-shell,
.bubble-vip.bubble-peer.code-bubble-shell,
.bubble-teacher.bubble-peer.code-bubble-shell,
.bubble-admin.bubble-peer.code-bubble-shell,
.bubble-vip.bubble-self.code-bubble-shell,
.bubble-teacher.bubble-self.code-bubble-shell,
.bubble-admin.bubble-self.code-bubble-shell {
  width: min(720px, 100%);
  padding: 0;
  color: #1f2a44;
  background: transparent;
  border: 0;
  box-shadow: none;
}

.bubble-code {
  width: 100%;
  min-width: min(320px, 100%);
  overflow: hidden;
  border: 1px solid #d6e1f2;
  border-radius: 8px;
  background: #101827;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.16);
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 12px;
  margin: 0;
  background: #f8fbff;
  border-bottom: 1px solid #dbe6f5;
}

.code-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.code-file-icon {
  flex-shrink: 0;
  color: #2563eb;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  font-weight: 900;
}

.code-filename {
  min-width: 0;
  overflow: hidden;
  color: #24324a;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-language {
  flex-shrink: 0;
  padding: 2px 7px;
  border-radius: 999px;
  background: #eaf1ff;
  color: #3156d4;
  font-size: 11px;
  font-weight: 800;
}

.code-download {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--app-primary);
  text-decoration: none;
  font-weight: 800;
}

.code-download:hover {
  text-decoration: underline;
}

.code-preview {
  margin: 0;
  padding: 14px;
  background: #101827;
  color: #e8edf7;
  border-radius: 0;
  font-size: 13px;
  line-height: 1.65;
  overflow-x: auto;
  max-height: 300px;
  overflow-y: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  tab-size: 2;
}

.code-preview code {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

@media (max-width: 768px) {
  .bubble.code-bubble-shell,
  .bubble-peer.code-bubble-shell,
  .bubble-self.code-bubble-shell {
    width: min(100%, calc(100vw - 88px));
  }

  .bubble-code {
    min-width: 0;
  }

  .code-header {
    align-items: flex-start;
  }

  .code-preview {
    max-height: 240px;
    font-size: 12px;
  }
}

/* --- File bubble --- */
.bubble-file {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 200px;
}

.file-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #1f2a44;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 11px;
  color: #94a3b8;
}

.file-download-btn {
  padding: 4px 12px;
  border-radius: 6px;
  background: var(--app-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  flex-shrink: 0;
}

.file-download-btn:hover {
  opacity: 0.85;
}

.image-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-out;
}

.image-preview-full {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 4px;
  cursor: default;
  transition: transform 0.2s ease;
  transform-origin: center center;
}

.image-preview-close {
  position: fixed;
  top: 20px;
  right: 28px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview-close:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
