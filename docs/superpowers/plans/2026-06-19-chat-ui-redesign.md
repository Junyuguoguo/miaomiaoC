# Chat UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the miaomiaoC chat pages (ChatPage.vue and ChatHub.vue) with WeChat-inspired bubble styling, glassmorphism, and rich CSS animations.

**Architecture:** Full rewrite of template + style sections in two Vue 3 SFCs. Script logic preserved with minimal enhancements (formatTime, animate class toggling). No new files, no backend changes.

**Tech Stack:** Vue 3 Composition API, Element Plus, CSS `@keyframes`, `backdrop-filter`, project CSS variables

## Global Constraints

- Use project CSS variables from `main.css`: `--app-primary`, `--app-primary-dark`, `--app-primary-soft`, `--app-border`, `--app-text`, `--app-text-muted`, `--app-surface`, `--app-surface-muted`, `--app-bg`, `--app-radius`, `--app-shadow-sm`, `--app-danger`
- Preserve ALL existing script logic (WebSocket, API calls, routing, state management)
- Element Plus components kept where useful (`el-avatar`), replaced with native HTML where not needed
- `px` units (codebase convention)
- No new npm dependencies

---

### Task 1: ChatPage.vue — Redesign Private/Group Chat

**Files:**
- Rewrite: `frontend/src/views/chat/ChatPage.vue` (template + style + minor script enhancement)

**Interfaces:**
- Consumes: `useRoute()` query params (`userId`, `userName`, `roomId`, `roomName`), `useUserStore()` for token/userId
- Produces: unchanged — navigates back to `/chat`, connects WebSocket, loads/sends messages via existing API

- [ ] **Step 1: Replace the entire ChatPage.vue**

Open `frontend/src/views/chat/ChatPage.vue` and replace its entire content with:

```vue
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
              {{ msg.senderName?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="bubble" :class="msg.senderId === currentUserId ? 'bubble-self' : 'bubble-peer'">
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
import { getPrivateMessages, getRoomMessages, markAsRead } from '@/api/chat'
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

// Computed
const currentUserId = computed(() => userStore.getUserId)
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
  const messageData = { messageType: 'TEXT', content: inputMessage.value.trim() }
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
    ElMessage.error('消息发送失败')
  }
}

// Receive message
const handleMessageReceived = (message) => {
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
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-dark));
  color: #fff;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.2);
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
  border-color: transparent transparent transparent var(--app-primary-dark);
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
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-dark));
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
</style>
```

- [ ] **Step 2: Visual verification**

Run the dev server:
```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/frontend && npm run dev
```

Navigate to a private chat page (e.g., click a contact from ChatHub). Verify:
- Messages have white bubbles (peer) with left tail and blue gradient bubbles (self) with right tail
- Time separators appear between messages with >5min gap
- Header has glassmorphism effect with connection dot (green/red)
- Input bar has glassmorphism, focus turns border blue with shadow
- Type text → "+" becomes blue "发送" button
- Click "+" → extension panel slides up with 3 icons
- Click "发送" → message sends, bubble animates in

- [ ] **Step 3: Commit**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC
git add frontend/src/views/chat/ChatPage.vue
git commit -m "feat(chat): redesign ChatPage with WeChat-style bubbles and animations"
```

---

### Task 2: ChatHub.vue — Redesign Chat Lobby

**Files:**
- Rewrite: `frontend/src/views/chat/ChatHub.vue` (template + style + minor script enhancement)

**Interfaces:**
- Consumes: `useRouter()` for navigation, `useUserStore()` for token/userId, chat API functions, `chatWebSocket` singleton
- Produces: navigates to `/chat/private?userId=X` and `/chat/room?roomId=X`, connects WebSocket, loads/sends messages

- [ ] **Step 1: Replace the entire ChatHub.vue**

Open `frontend/src/views/chat/ChatHub.vue` and replace its entire content with:

```vue
<template>
  <div class="chat-hub">
    <!-- Header -->
    <div class="hub-header">
      <button class="header-back" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M12.5 15l-5-5 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <h1 class="header-title">综合交流大厅</h1>
      <span class="connection-badge" :class="connected ? 'connected' : 'disconnected'">
        <span class="badge-dot"></span>
        {{ connected ? '已连接' : '未连接' }}
      </span>
    </div>

    <div class="hub-body">
      <!-- Message area -->
      <div class="message-area">
        <div ref="messageListRef" class="message-list">
          <div v-if="messages.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="var(--app-text-muted)" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </div>
            <p class="empty-text">暂无消息，快来发送第一条吧</p>
          </div>

          <template v-for="(msg, idx) in messages" :key="msg.id">
            <div v-if="shouldShowTime(idx)" class="time-separator">
              <span class="time-label">{{ formatTimeFull(msg.createTime) }}</span>
            </div>

            <div
              class="message-row"
              :class="msg.senderId === currentUserId ? 'self' : ''"
              :style="{ animationDelay: Math.min(idx * 40, 400) + 'ms' }"
            >
              <img class="msg-avatar" :src="msg.senderAvatar || defaultAvatar" alt="avatar" />
              <div class="msg-body">
                <span v-if="msg.senderId !== currentUserId" class="sender-name">{{ msg.senderName || '匿名用户' }}</span>
                <div class="bubble" :class="msg.senderId === currentUserId ? 'bubble-self' : 'bubble-peer'">
                  {{ msg.content }}
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- Input bar -->
        <div class="input-bar">
          <input
            v-model="inputMessage"
            class="msg-input"
            placeholder="输入消息，按 Enter 发送..."
            @keydown.enter.exact.prevent="sendMessage"
          />
          <button
            v-if="inputMessage.trim()"
            class="send-btn"
            @click="sendMessage"
          >发送</button>
          <button
            v-else
            class="send-btn send-disabled"
            disabled
          >发送</button>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="sidebar">
        <!-- Search -->
        <div class="sidebar-section">
          <h3 class="section-title">搜索用户</h3>
          <div class="search-wrap">
            <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="输入用户名搜索..."
              @input="onSearchInput"
            />
          </div>
          <div v-if="searchResults.length > 0" class="search-results">
            <div
              v-for="user in searchResults"
              :key="user.id"
              class="contact-card"
              @click="goPrivateChat(user.id)"
            >
              <img class="contact-avatar" :src="user.avatar || defaultAvatar" alt="avatar" />
              <span class="contact-name">{{ user.username || user.realName }}</span>
            </div>
          </div>
          <div v-else-if="searchKeyword.trim() && !searchLoading" class="empty-hint">
            未找到用户
          </div>
        </div>

        <!-- Recent contacts -->
        <div class="sidebar-section">
          <h3 class="section-title">最近联系人</h3>
          <div v-if="contacts.length === 0" class="empty-hint">暂无联系人</div>
          <div
            v-for="(contact, idx) in contacts"
            :key="contact.userId || contact.id"
            class="contact-card"
            :style="{ animationDelay: idx * 30 + 'ms' }"
            @click="goPrivateChat(contact.userId || contact.id)"
          >
            <img class="contact-avatar" :src="contact.avatar || defaultAvatar" alt="avatar" />
            <div class="contact-info">
              <span class="contact-name">{{ contact.username || contact.realName }}</span>
            </div>
            <span v-if="contact.unreadCount > 0" class="unread-badge">
              {{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getRoomMessages, getPublicRooms, getContactDetails, searchUsers, joinRoom } from '@/api/chat'
import chatWebSocket from '@/utils/chat-websocket'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// State
const messages = ref([])
const inputMessage = ref('')
const messageListRef = ref(null)
const publicRoomId = ref(null)
const connected = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const contacts = ref([])
let searchTimer = null

const currentUserId = computed(() => userStore.getUserId)

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

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// Load contacts
const loadContacts = async () => {
  try {
    const res = await getContactDetails()
    if (res && res.code === 200) {
      contacts.value = res.data || []
    }
  } catch (e) {
    console.error('加载联系人失败:', e)
  }
}

// Handle incoming message
const handleMessageReceived = (msg) => {
  if (msg.id && messages.value.some(m => m.id === msg.id)) return
  messages.value.push(msg)
  scrollToBottom()
}

// WebSocket
const connectAndSubscribe = async () => {
  const token = userStore.token
  const userId = currentUserId.value
  if (!token || !userId) return
  try {
    await chatWebSocket.connect(token, userId)
    connected.value = true
    chatWebSocket.onError((err) => {
      console.error('聊天错误:', err)
    })
    if (publicRoomId.value) {
      chatWebSocket.subscribeRoomMessages(publicRoomId.value, handleMessageReceived)
    }
  } catch (e) {
    console.error('WebSocket 连接失败:', e)
    connected.value = false
  }
}

// Send
const sendMessage = () => {
  const content = inputMessage.value.trim()
  if (!content) return
  if (!publicRoomId.value) {
    ElMessage.warning('聊天室未就绪')
    return
  }
  const sent = chatWebSocket.sendRoomMessage({
    roomId: publicRoomId.value,
    messageType: 'TEXT',
    content
  })
  if (sent) {
    inputMessage.value = ''
  } else {
    ElMessage.warning('发送失败，请检查连接状态')
  }
}

// Search (debounced)
const onSearchInput = () => {
  clearTimeout(searchTimer)
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    return
  }
  searchLoading.value = true
  searchTimer = setTimeout(async () => {
    try {
      const res = await searchUsers(keyword)
      if (res && res.code === 200) {
        searchResults.value = res.data || []
      }
    } catch (e) {
      console.error('搜索用户失败:', e)
    } finally {
      searchLoading.value = false
    }
  }, 300)
}

const goPrivateChat = (userId) => {
  router.push({ path: '/chat/private', query: { userId } })
}

const goBack = () => {
  router.push('/exam')
}

// Init
onMounted(async () => {
  try {
    const res = await getPublicRooms()
    if (res && res.code === 200 && res.data && res.data.length > 0) {
      publicRoomId.value = res.data[0].id
      await joinRoom(publicRoomId.value).catch(() => {})
      const msgRes = await getRoomMessages(publicRoomId.value, 0, 50)
      if (msgRes && msgRes.code === 200) {
        messages.value = (msgRes.data.content || []).reverse()
        scrollToBottom()
      }
    } else {
      ElMessage.info('暂无可用聊天室')
    }
  } catch (e) {
    console.error('加载聊天室失败:', e)
  }
  connectAndSubscribe()
  loadContacts()
})

onUnmounted(() => {
  chatWebSocket.disconnect()
  connected.value = false
  clearTimeout(searchTimer)
})
</script>

<style scoped>
.chat-hub {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, var(--app-bg) 100%);
  font-family: inherit;
}

/* --- Header --- */
.hub-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 20px;
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
  font-size: 17px;
  font-weight: 600;
  margin-left: 12px;
  flex: 1;
  color: var(--app-text);
}

.connection-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 14px;
  font-weight: 500;
}

.connection-badge.connected {
  background: #e6f7ee;
  color: #16a34a;
}

.connection-badge.disconnected {
  background: #fef2f2;
  color: #dc2626;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.connection-badge.connected .badge-dot {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* --- Body --- */
.hub-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* --- Message area --- */
.message-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  scroll-behavior: smooth;
}

/* --- Empty state --- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--app-text-muted);
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.4;
}

.empty-text {
  font-size: 14px;
}

.empty-hint {
  text-align: center;
  color: var(--app-text-muted);
  padding: 16px 0;
  font-size: 13px;
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
  margin-bottom: 14px;
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
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.msg-body {
  max-width: 65%;
  margin: 0 10px;
}

.sender-name {
  display: block;
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 4px;
  font-weight: 500;
}

.message-row.self .sender-name {
  display: none;
}

/* --- Bubble --- */
.bubble {
  display: inline-block;
  padding: 10px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  text-align: left;
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
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-dark));
  color: #fff;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.2);
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
  border-color: transparent transparent transparent var(--app-primary-dark);
}

/* --- Input bar --- */
.input-bar {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  gap: 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid var(--app-border);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
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
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-dark));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s, transform 0.15s;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:active:not(:disabled) {
  transform: scale(0.96);
}

.send-disabled {
  background: var(--app-surface-muted);
  color: var(--app-text-muted);
  cursor: not-allowed;
}

/* --- Sidebar --- */
.sidebar {
  width: 260px;
  background: var(--app-surface);
  border-left: 1px solid var(--app-border);
  overflow-y: auto;
  flex-shrink: 0;
  padding: 16px;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}

.sidebar-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
  margin-bottom: 10px;
}

/* --- Search --- */
.search-wrap {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--app-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 12px 0 32px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface-muted);
  font-size: 13px;
  color: var(--app-text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input::placeholder {
  color: var(--app-text-muted);
}

.search-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}

.search-results {
  margin-top: 8px;
}

/* --- Contact card --- */
.contact-card {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: var(--app-radius);
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}

.contact-card:hover {
  background: var(--app-primary-soft);
  transform: translateY(-1px);
}

.contact-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
}

.contact-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--app-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}
</style>
```

- [ ] **Step 2: Visual verification**

In the browser (`npm run dev`), navigate to `/chat`. Verify:
- Public room messages have bubble tails and stagger animation on load
- Sender names appear above peer bubbles, hidden on own messages
- Time separators show between messages with >5min gap
- Header has glassmorphism with pulsing connection badge
- Input bar has glassmorphism, focus shadow effect
- Right sidebar: search input focus has blue border + shadow
- Contact cards hover → lift + blue background
- Unread badges are red capsules
- Resize to <768px → sidebar hides

- [ ] **Step 3: Commit**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC
git add frontend/src/views/chat/ChatHub.vue
git commit -m "feat(chat): redesign ChatHub with WeChat-style bubbles, glassmorphism, and animations"
```

---

### Task 3: Final Verification

- [ ] **Step 1: Full visual regression**

1. Navigate to `/chat` — ChatHub loads with stagger animation on messages
2. Type and send a message → bubble slides in from bottom
3. Click a contact → navigates to private chat
4. Private chat loads with animated bubbles
5. Type text → "+" becomes "发送"
6. Click "+" → extension panel slides up
7. Go back to ChatHub → contacts have hover effect
8. Search for a user → results appear with contact cards

- [ ] **Step 2: Final commit**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC
git add -A
git commit -m "feat(chat): complete chat UI redesign with WeChat-style animations"
```
