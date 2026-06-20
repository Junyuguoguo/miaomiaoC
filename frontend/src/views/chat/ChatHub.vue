<template>
  <div class="chat-hub">
    <!-- Header -->
    <div class="hub-header">
      <button class="header-back" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M12.5 15l-5-5 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <h1 class="header-title">聊天中心</h1>
      <span class="connection-badge" :class="connected ? 'connected' : 'disconnected'">
        <span class="badge-dot"></span>
        {{ connected ? '已连接' : '未连接' }}
      </span>
    </div>

    <div class="hub-body">
      <!-- Left panel -->
      <div class="left-panel">
        <!-- Room section -->
        <div class="room-section">
          <div class="section-header">
            <h3 class="section-title">聊天大厅</h3>
            <button class="join-room-btn" @click="showJoinDialog = true">加入群聊</button>
          </div>
          <div v-if="sortedRooms.length === 0" class="empty-hint">暂无聊天室</div>
          <div
            v-for="(room, idx) in sortedRooms"
            :key="room.id"
            class="room-card"
            :class="{ active: currentRoom?.id === room.id }"
            :style="{ animationDelay: idx * 40 + 'ms' }"
            @click="selectRoom(room)"
            @contextmenu="openContextMenu($event, room)"
          >
            <div class="room-card-left">
              <span class="room-name">{{ room.roomName || room.name }}</span>
              <span class="room-icons">
                <span v-if="isRoomPinned(room.id)" class="room-icon pin-icon" title="已置顶">&#x1F4CC;</span>
                <span v-if="isRoomMuted(room.id)" class="room-icon mute-icon" title="免打扰">&#x1F507;</span>
              </span>
            </div>
            <span v-if="room.roomLevel === 'VIP'" class="vip-tag">VIP</span>
            <span v-if="room.college" class="college-tag">{{ room.college }}</span>
            <span v-else class="college-tag all">全校</span>
          </div>
          <button v-if="isTeacher" class="add-room-btn" @click="showCreateRoom = true">+ 创建聊天室</button>
        </div>

        <!-- Contact section -->
        <div class="contact-section">
          <h3 class="section-title">最近联系人</h3>

          <!-- Search -->
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
              @click="goPrivateChat(user.id, user.username || user.realName)"
            >
              <img class="contact-avatar" :src="fixAvatarUrl(user.avatar)" alt="avatar" />
              <span class="contact-name">{{ user.username || user.realName }}</span>
            </div>
          </div>
          <div v-else-if="searchKeyword.trim() && !searchLoading" class="empty-hint">
            未找到用户
          </div>

          <!-- Contact list -->
          <div v-if="contacts.length === 0 && !searchKeyword.trim()" class="empty-hint">暂无联系人</div>
          <div
            v-for="(contact, idx) in contacts"
            :key="contact.userId || contact.id"
            class="contact-card"
            :style="{ animationDelay: idx * 30 + 'ms' }"
            @click="goPrivateChat(contact.userId || contact.id, contact.username || contact.realName)"
          >
            <img class="contact-avatar" :src="fixAvatarUrl(contact.avatar)" alt="avatar" />
            <div class="contact-info">
              <span class="contact-name">{{ contact.username || contact.realName }}</span>
            </div>
            <span v-if="contact.unreadCount > 0" class="unread-badge">
              {{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}
            </span>
          </div>
        </div>
      </div>

      <!-- Right panel -->
      <div class="right-panel">
        <div class="room-header">
          <div class="room-header-left">
            <span class="room-header-name">{{ currentRoom?.roomName || currentRoom?.name || '综合交流大厅' }}</span>
            <span v-if="currentRoom?.groupNumber" class="group-number-display">
              群号: {{ currentRoom.groupNumber }}
              <button class="copy-gn-btn" @click="copyGroupNumber" title="复制群号">复制</button>
            </span>
          </div>
          <div class="room-header-right">
            <button
              v-if="currentRoom"
              class="header-icon-btn"
              :class="{ active: isRoomPinned(currentRoom.id) }"
              @click="handleTogglePin(currentRoom.id)"
              :title="isRoomPinned(currentRoom.id) ? '取消置顶' : '置顶'"
            >&#x1F4CC;</button>
            <button
              v-if="currentRoom"
              class="header-icon-btn"
              :class="{ active: isRoomMuted(currentRoom.id) }"
              @click="handleToggleMute(currentRoom.id)"
              :title="isRoomMuted(currentRoom.id) ? '取消免打扰' : '免打扰'"
            >&#x1F507;</button>
            <span class="connection-badge room-badge" :class="connected ? 'connected' : 'disconnected'">
              <span class="badge-dot"></span>
              {{ connected ? '已连接' : '未连接' }}
            </span>
          </div>
        </div>

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
              <img
                class="msg-avatar"
                :class="{ clickable: msg.senderId !== currentUserId }"
                :src="fixAvatarUrl(msg.senderAvatar)"
                alt="avatar"
                @click="msg.senderId !== currentUserId && goPrivateChat(msg.senderId, msg.senderName)"
              />
              <div class="msg-body">
                <span v-if="msg.senderId !== currentUserId" class="sender-name">
                  {{ msg.senderName || '匿名用户' }}
                  <span v-if="msg.senderRole === 4" class="role-badge admin-badge">管理员</span>
                  <span v-else-if="msg.senderRole === 3" class="role-badge teacher-badge">教师</span>
                  <span v-else-if="msg.senderRole === 2" class="role-badge vip-badge">VIP</span>
                </span>
                <div class="bubble" :class="getBubbleClass(msg)">
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
    </div>

    <!-- Create room dialog -->
    <el-dialog v-model="showCreateRoom" title="创建聊天室" width="400px">
      <el-form>
        <el-form-item label="房间名称">
          <el-input v-model="newRoom.name" placeholder="如：计算机学院交流群" />
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="newRoom.college" placeholder="留空为全校大厅" clearable>
            <el-option label="计算机学院" value="计算机学院" />
            <el-option label="机械学院" value="机械学院" />
            <el-option label="电子信息学院" value="电子信息学院" />
            <el-option label="经济管理学院" value="经济管理学院" />
            <el-option label="外国语学院" value="外国语学院" />
            <el-option label="理学院" value="理学院" />
            <el-option label="人文社科学院" value="人文社科学院" />
            <el-option label="自动化学院" value="自动化学院" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-radio-group v-model="newRoom.roomLevel">
            <el-radio value="FREE">普通（免费用户自动加入）</el-radio>
            <el-radio value="VIP">VIP专属（需群号加入）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateRoom = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRoom">创建</el-button>
      </template>
    </el-dialog>

    <!-- Join by group number dialog -->
    <el-dialog v-model="showJoinDialog" title="加入群聊" width="400px" @close="joinGroupNumber = ''">
      <div class="join-dialog-body">
        <p class="join-hint">输入群号加入对应的聊天室</p>
        <el-input
          v-model="joinGroupNumber"
          placeholder="请输入6位群号"
          maxlength="6"
          clearable
          @keydown.enter="handleJoinGroup"
        />
      </div>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" @click="handleJoinGroup">搜索并加入</el-button>
      </template>
    </el-dialog>

    <!-- Context menu -->
    <div
      v-if="showContextMenu && contextMenuRoom"
      class="context-menu-overlay"
      @click="closeContextMenu"
      @contextmenu.prevent="closeContextMenu"
    >
      <div
        class="context-menu"
        :style="{ left: contextMenuPos.x + 'px', top: contextMenuPos.y + 'px' }"
      >
        <div class="context-menu-item" @click="handleTogglePin(contextMenuRoom.id); closeContextMenu()">
          {{ isRoomPinned(contextMenuRoom.id) ? '取消置顶' : '置顶' }}
        </div>
        <div class="context-menu-item" @click="handleToggleMute(contextMenuRoom.id); closeContextMenu()">
          {{ isRoomMuted(contextMenuRoom.id) ? '取消免打扰' : '免打扰' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getRoomMessages,
  getPublicRooms,
  getRooms,
  createRoom,
  getContactDetails,
  searchUsers,
  joinRoom,
  joinByGroupNumber,
  togglePinRoom,
  toggleMuteRoom,
  getRoomSettings
} from '@/api/chat'
import chatWebSocket from '@/utils/chat-websocket'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const fixAvatarUrl = (url) => {
  if (!url) return defaultAvatar
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

// State
const messages = ref([])
const inputMessage = ref('')
const messageListRef = ref(null)
const connected = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const contacts = ref([])
let searchTimer = null

// Room state
const rooms = ref([])
const currentRoom = ref(null)
const showCreateRoom = ref(false)
const newRoom = ref({ name: '', college: '', roomLevel: 'FREE' })
let currentRoomSubId = null  // Track current WebSocket subscription ID

// Pin/mute/join state
const roomSettings = ref([])
const showJoinDialog = ref(false)
const joinGroupNumber = ref('')
const contextMenuRoom = ref(null)
const showContextMenu = ref(false)
const contextMenuPos = ref({ x: 0, y: 0 })

const currentUserId = computed(() => userStore.getUserId)
const isTeacher = computed(() => Number(userStore.getUserRoleId) >= 3)

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

// Sorted rooms: pinned first, then by id
const sortedRooms = computed(() => {
  const list = [...rooms.value]
  list.sort((a, b) => {
    const aPinned = isRoomPinned(a.id) ? 1 : 0
    const bPinned = isRoomPinned(b.id) ? 1 : 0
    if (bPinned !== aPinned) return bPinned - aPinned
    return a.id - b.id
  })
  return list
})

const isRoomPinned = (roomId) => {
  const s = roomSettings.value.find(rs => rs.roomId === roomId)
  return s ? Boolean(s.isPinned) : false
}

const isRoomMuted = (roomId) => {
  const s = roomSettings.value.find(rs => rs.roomId === roomId)
  return s ? Boolean(s.isMuted) : false
}

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

// ──────────────── Room management ────────────────

const loadRooms = async () => {
  try {
    const res = await getRooms()
    if (res && res.code === 200) {
      rooms.value = res.data || []
      // Load room settings after rooms are loaded
      await loadRoomSettings()
      // Auto-select first room
      if (rooms.value.length > 0 && !currentRoom.value) {
        await selectRoom(sortedRooms.value[0])
      }
    }
  } catch (e) {
    console.error('加载聊天室列表失败:', e)
  }
}

const selectRoom = async (room) => {
  if (currentRoom.value?.id === room.id) return
  currentRoom.value = room
  messages.value = []

  // Load messages for this room
  try {
    await joinRoom(room.id).catch(() => {})
    const msgRes = await getRoomMessages(room.id, 0, 50)
    if (msgRes && msgRes.code === 200) {
      messages.value = (msgRes.data.content || []).reverse()
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载房间消息失败:', e)
  }

  // Subscribe to the new room via WebSocket
  if (connected.value) {
    currentRoomSubId = chatWebSocket.subscribeRoomMessages(room.id, handleMessageReceived)
  }
}

const handleCreateRoom = async () => {
  const name = newRoom.value.name.trim()
  if (!name) {
    ElMessage.warning('请输入房间名称')
    return
  }
  try {
    const payload = { name: name, roomLevel: newRoom.value.roomLevel || 'FREE' }
    if (newRoom.value.college) payload.college = newRoom.value.college
    const res = await createRoom(payload)
    if (res && res.code === 200) {
      ElMessage.success('创建成功')
      showCreateRoom.value = false
      newRoom.value = { name: '', college: '', roomLevel: 'FREE' }
      await loadRooms()
    } else {
      ElMessage.error(res?.message || '创建失败')
    }
  } catch (e) {
    console.error('创建聊天室失败:', e)
    ElMessage.error('创建失败')
  }
}

// ──────────────── Room settings (pin/mute) ────────────────

const loadRoomSettings = async () => {
  try {
    const res = await getRoomSettings()
    if (res && res.code === 200) {
      roomSettings.value = res.data || []
    }
  } catch (e) {
    console.error('加载房间设置失败:', e)
  }
}

const handleTogglePin = async (roomId) => {
  try {
    const res = await togglePinRoom(roomId)
    if (res && res.code === 200) {
      await loadRoomSettings()
      ElMessage.success(isRoomPinned(roomId) ? '已置顶' : '已取消置顶')
    }
  } catch (e) {
    console.error('置顶操作失败:', e)
    ElMessage.error('操作失败')
  }
}

const handleToggleMute = async (roomId) => {
  try {
    const res = await toggleMuteRoom(roomId)
    if (res && res.code === 200) {
      await loadRoomSettings()
      ElMessage.success(isRoomMuted(roomId) ? '已设为免打扰' : '已取消免打扰')
    }
  } catch (e) {
    console.error('免打扰操作失败:', e)
    ElMessage.error('操作失败')
  }
}

const openContextMenu = (e, room) => {
  e.preventDefault()
  contextMenuRoom.value = room
  contextMenuPos.value = { x: e.clientX, y: e.clientY }
  showContextMenu.value = true
}

const closeContextMenu = () => {
  showContextMenu.value = false
  contextMenuRoom.value = null
}

// ──────────────── Join by group number ────────────────

const handleJoinGroup = async () => {
  const num = joinGroupNumber.value.trim()
  if (!num) {
    ElMessage.warning('请输入群号')
    return
  }
  try {
    const res = await joinByGroupNumber(num)
    if (res && res.code === 200 && res.data) {
      const room = res.data
      // Check if already in room list
      if (!rooms.value.some(r => r.id === room.id)) {
        rooms.value.push(room)
        // Auto join the room via API
        await joinRoom(room.id).catch(() => {})
      }
      showJoinDialog.value = false
      joinGroupNumber.value = ''
      await selectRoom(room)
      ElMessage.success('已加入群聊')
    } else {
      ElMessage.error(res?.message || '群号不存在')
    }
  } catch (e) {
    console.error('加入群聊失败:', e)
    ElMessage.error('加入失败')
  }
}

const copyGroupNumber = async () => {
  const gn = currentRoom.value?.groupNumber
  if (!gn) {
    ElMessage.warning('该房间暂无群号')
    return
  }
  try {
    await navigator.clipboard.writeText(gn)
    ElMessage.success('群号已复制')
  } catch {
    ElMessage.info(`群号: ${gn}`)
  }
}

// ──────────────── Contacts ────────────────

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

// ──────────────── Message handling ────────────────

const handleMessageReceived = (msg) => {
  // Only process messages for the current room
  if (currentRoom.value && msg.roomId && msg.roomId !== currentRoom.value.id) return
  if (msg.id && messages.value.some(m => m.id === msg.id)) return
  // Replace optimistic message if it matches
  const last = messages.value[messages.value.length - 1]
  if (last && last.senderId === currentUserId.value && last.content === msg.content && msg.senderId === currentUserId.value) {
    messages.value[messages.value.length - 1] = msg
    return
  }
  messages.value.push(msg)
  scrollToBottom()
}

// ──────────────── WebSocket ────────────────

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
    // Subscribe to current room if already selected
    if (currentRoom.value) {
      currentRoomSubId = chatWebSocket.subscribeRoomMessages(currentRoom.value.id, handleMessageReceived)
    }
  } catch (e) {
    console.error('WebSocket 连接失败:', e)
    connected.value = false
  }
}

// ──────────────── Send ────────────────

const sendMessage = () => {
  const content = inputMessage.value.trim()
  if (!content) return
  if (!currentRoom.value) {
    ElMessage.warning('请先选择聊天室')
    return
  }

  // Optimistic render
  const optimisticMsg = {
    id: Date.now(),
    senderId: currentUserId.value,
    senderName: userStore.getUserName || '',
    senderAvatar: userStore.getUserAvatar || '',
    senderRole: userStore.getUserRoleId,
    senderCollege: userStore.getUserCollege || '',
    content,
    createTime: new Date().toISOString()
  }
  messages.value.push(optimisticMsg)
  scrollToBottom()

  const sent = chatWebSocket.sendRoomMessage({
    roomId: currentRoom.value.id,
    messageType: 'TEXT',
    content
  })
  if (sent) {
    inputMessage.value = ''
  } else {
    messages.value.pop()
    ElMessage.warning('发送失败，请检查连接状态')
  }
}

// ──────────────── Search (debounced) ────────────────

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

// ──────────────── Navigation ────────────────

const goPrivateChat = (userId, userName) => {
  const query = { userId }
  if (userName) query.userName = userName
  router.push({ path: '/chat/private', query })
}

const goBack = () => {
  const roleId = userStore.getUserRoleId
  if (roleId === 3 || roleId === 4) {
    router.push('/teacher')
  } else {
    router.push('/exam')
  }
}

// ──────────────── Lifecycle ────────────────

watch(() => route.path, (newPath) => {
  if (newPath === '/chat') {
    loadContacts()
  }
})

onMounted(async () => {
  await loadRooms()
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
  height: 50px;
  padding: 0 16px;
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

/* --- Body (left-right split) --- */
.hub-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  height: calc(100vh - 56px);
}

/* --- Left panel --- */
.left-panel {
  width: 280px;
  border-right: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  background: var(--app-surface);
  flex-shrink: 0;
}

.room-section {
  padding: 16px;
  border-bottom: 1px solid var(--app-border);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.room-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.join-room-btn {
  font-size: 12px;
  padding: 4px 10px;
  border: 1px solid var(--app-primary, #2563eb);
  border-radius: 6px;
  background: transparent;
  color: var(--app-primary, #2563eb);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  font-weight: 500;
}

.join-room-btn:hover {
  background: var(--app-primary, #2563eb);
  color: #fff;
}

.room-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: var(--app-radius, 8px);
  cursor: pointer;
  transition: background 0.15s, transform 0.2s, box-shadow 0.2s;
  margin-bottom: 4px;
  animation: fadeInUp 300ms ease both;
  border-left: 3px solid transparent;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.room-card:hover {
  background: var(--app-primary-soft);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.room-card.active {
  background: var(--app-primary-soft, #e8f0ff);
  border-left: 3px solid var(--app-primary, #2563eb);
}

.room-card-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.room-icons {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.room-icon {
  font-size: 12px;
  opacity: 0.6;
  line-height: 1;
}

.room-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.college-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 8px;
  background: linear-gradient(135deg, #D4A843, #B8922E);
  color: #fff;
}

.college-tag.all {
  background: var(--app-surface-muted, #e5e7eb);
  color: var(--app-text-muted);
}

.vip-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  font-weight: 700;
  flex-shrink: 0;
  margin-left: 6px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
  letter-spacing: 0.5px;
}

.add-room-btn {
  display: block;
  width: 100%;
  padding: 8px 0;
  margin-top: 8px;
  border: 1px dashed var(--app-border);
  border-radius: var(--app-radius, 8px);
  background: transparent;
  color: var(--app-primary, #2563eb);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.add-room-btn:hover {
  background: var(--app-primary-soft);
  border-color: var(--app-primary);
}

/* --- Contact section --- */
.contact-section {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.contact-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
  margin-bottom: 10px;
}

/* --- Search --- */
.search-wrap {
  position: relative;
  margin-bottom: 8px;
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
  margin-bottom: 8px;
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

/* --- Right panel --- */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.room-header {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--app-border);
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text);
  flex-shrink: 0;
  gap: 12px;
}

.room-header-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.room-header-name {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-number-display {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 400;
  color: var(--app-text-muted);
  background: var(--app-surface-muted);
  padding: 2px 8px;
  border-radius: 6px;
  flex-shrink: 0;
}

.copy-gn-btn {
  font-size: 11px;
  padding: 1px 6px;
  border: 1px solid var(--app-border);
  border-radius: 4px;
  background: transparent;
  color: var(--app-primary, #2563eb);
  cursor: pointer;
  transition: background 0.15s;
}

.copy-gn-btn:hover {
  background: var(--app-primary-soft);
}

.room-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--app-border);
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.5;
  transition: opacity 0.15s, background 0.15s, border-color 0.15s;
}

.header-icon-btn:hover {
  opacity: 0.8;
  background: var(--app-primary-soft);
}

.header-icon-btn.active {
  opacity: 1;
  border-color: var(--app-primary, #2563eb);
  background: var(--app-primary-soft);
}

.room-badge {
  font-size: 11px;
  padding: 2px 10px;
}

/* --- Message list --- */
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

.msg-avatar.clickable {
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}

.msg-avatar.clickable:hover {
  opacity: 0.8;
  transform: scale(1.08);
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

/* === Bubbles (QQ-style: rounded, no tails) === */
.bubble {
  display: inline-block;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  text-align: left;
  position: relative;
  border-radius: 12px;
  max-width: 100%;
}

.bubble-peer {
  background: #fff;
  color: #333;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.bubble-self {
  background: #95EC69;
  color: #333;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

/* Input bar */
.input-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 10px;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
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
}

.send-btn:hover:not(:disabled) {
  opacity: 0.85;
}

.send-disabled {
  background: #ccc;
  color: #999;
  cursor: not-allowed;
}

/* === Role Badges (subtle, QQ-style) === */
.role-badge {
  display: inline-block;
  font-size: 10px;
  padding: 0 5px;
  border-radius: 3px;
  font-weight: 500;
  margin-left: 5px;
  vertical-align: middle;
  border-left: 2px solid;
  background: transparent;
}

.vip-badge {
  color: #d48806;
  border-left-color: #faad14;
}

.teacher-badge {
  color: #0891b2;
  border-left-color: #06b6d4;
}

.admin-badge {
  color: #7c3aed;
  border-left-color: #8b5cf6;
}

/* === Role Bubbles (peer: tinted, self: keep green) === */
.bubble-vip.bubble-peer {
  background: #fffbe6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.bubble-teacher.bubble-peer {
  background: #e6fffb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.bubble-admin.bubble-peer {
  background: #f5f0ff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* Self bubbles keep the same green for all roles (QQ style) */
.bubble-vip.bubble-self,
.bubble-teacher.bubble-self,
.bubble-admin.bubble-self {
  background: #95EC69;
  color: #333;
}

/* --- Context menu --- */
.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid var(--app-border);
  padding: 4px 0;
  min-width: 120px;
  z-index: 1001;
  animation: contextFadeIn 150ms ease;
}

@keyframes contextFadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.context-menu-item {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--app-text);
  cursor: pointer;
  transition: background 0.1s;
}

.context-menu-item:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

/* --- Join dialog --- */
.join-dialog-body {
  padding: 8px 0;
}

.join-hint {
  font-size: 13px;
  color: var(--app-text-muted);
  margin-bottom: 12px;
}

/* --- Mobile responsive --- */
@media (max-width: 768px) {
  .left-panel {
    display: none;
  }

  .right-panel {
    width: 100%;
  }

  .room-header {
    padding: 0 12px;
  }

  .message-list {
    padding: 12px;
  }

  .input-bar {
    padding: 10px 12px;
  }
}
</style>
