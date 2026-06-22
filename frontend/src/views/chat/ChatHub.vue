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

    <div class="hub-body" :class="{ 'panel-collapsed': !showInfoPanel }">
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
            <button
              class="info-toggle-btn"
              :class="{ active: showInfoPanel }"
              @click="toggleInfoPanel"
            >
              <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
                <path v-if="showInfoPanel" d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path v-else d="M10 3l-5 5 5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>群组信息</span>
            </button>
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
                <div class="message-meta">
                  <span class="sender-name">{{ getSenderName(msg) }}</span>
                  <span v-if="msg.senderRole === 4" class="role-badge admin-badge">管理员</span>
                  <span v-else-if="msg.senderRole === 3" class="role-badge teacher-badge">教师</span>
                  <span v-else-if="msg.senderRole === 2" class="role-badge vip-badge">VIP学生</span>
                  <span class="message-time">{{ formatMessageTime(msg.createTime) }}</span>
                </div>
                <div class="bubble" :class="getBubbleClass(msg)">
                  <div v-if="msg.messageType === 'IMAGE'" class="bubble-image"><img :src="msg.content" alt="图片" @click="window.open(msg.content, '_blank')" /></div>
                  <div v-else-if="msg.messageType === 'CODE'" class="bubble-code"><div class="code-header"><span class="code-filename">{{ msg.fileName || 'code' }}</span><a :href="msg.content" download class="code-download">下载</a></div><pre class="code-preview"><code>{{ msg.codeContent || '加载中...' }}</code></pre></div>
                  <div v-else-if="msg.messageType === 'FILE'" class="bubble-file"><div class="file-icon">&#128196;</div><div class="file-info"><span class="file-name">{{ msg.fileName || '文件' }}</span><span class="file-size">{{ formatFileSize(msg.fileSize) }}</span></div><a :href="msg.content" download class="file-download-btn">下载</a></div>
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
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- Input bar -->
        <div class="input-bar">
          <button class="upload-btn" @click="$refs.fileInput.click()" title="发送文件">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="var(--app-primary)" stroke-width="1.8"/><circle cx="8.5" cy="8.5" r="1.5" fill="var(--app-primary)"/><path d="M21 15l-5-5L5 21" stroke="var(--app-primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <input ref="fileInput" type="file" style="display:none" accept="image/*,.c,.cpp,.h,.hpp,.java,.py,.js,.ts,.html,.css,.json,.xml,.sql,.sh,.go,.rs,.txt" @change="handleFileUpload" />
          <input
            ref="messageInputRef"
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

      <aside v-if="showInfoPanel" class="info-panel" aria-label="群组信息">
        <section class="info-card room-profile-card">
          <div class="info-card-header">
            <h3>群组信息</h3>
            <button type="button" class="panel-icon-btn" title="打开群设置" @click="openRoomSettings">设置</button>
          </div>
          <div class="room-avatar-large" aria-hidden="true">•••</div>
          <h2>{{ roomDisplayName }}</h2>
          <p>
            群号：{{ currentRoom?.groupNumber || '暂无' }}
            <button v-if="currentRoom?.groupNumber" class="copy-inline-btn" type="button" @click="copyGroupNumber">复制</button>
          </p>
          <div class="room-profile-stats">
            <div>
              <strong>{{ roomMemberCount }}</strong>
              <span>群成员</span>
            </div>
            <div>
              <strong>{{ connected ? '在线' : '离线' }}</strong>
              <span>连接状态</span>
            </div>
            <div>
              <strong>{{ currentRoom?.roomLevel === 'VIP' ? 'VIP' : '公开' }}</strong>
              <span>群类型</span>
            </div>
          </div>
        </section>

        <section class="info-card">
          <div class="info-card-header">
            <h3>群成员</h3>
            <button type="button" class="info-link-btn" @click="openMembersDialog">查看更多</button>
          </div>
          <div class="member-preview">
            <img
              v-for="contact in memberPreview"
              :key="contact.userId || contact.id"
              :src="fixAvatarUrl(contact.avatar)"
              :alt="contact.nickname || contact.username || '成员头像'"
            />
            <span v-if="memberPreview.length === 0" class="empty-inline">暂无成员预览</span>
          </div>
          <div class="member-legend">
            <span><i class="legend-dot admin"></i>管理员</span>
            <span><i class="legend-dot teacher"></i>教师</span>
            <span><i class="legend-dot vip"></i>VIP学生</span>
          </div>
        </section>

        <section class="info-card">
          <div class="info-card-header">
            <h3>群公告</h3>
            <button type="button" class="info-link-btn" @click="openNoticeDialog">
              {{ canManageRoom ? '编辑' : '查看' }}
            </button>
          </div>
          <div class="notice-box">
            <span class="notice-pin">置顶</span>
            <p>{{ currentNoticeText }}</p>
            <small>发布于 {{ currentNoticeTime }}</small>
          </div>
        </section>

        <section class="info-card">
          <div class="info-card-header">
            <h3>群设置</h3>
            <button type="button" class="info-link-btn" @click="openRoomSettings">打开设置</button>
          </div>
          <button
            v-if="currentRoom"
            type="button"
            class="setting-row"
            @click="handleTogglePin(currentRoom.id)"
          >
            <span>置顶聊天</span>
            <i :class="{ active: isRoomPinned(currentRoom.id) }"></i>
          </button>
          <button
            v-if="currentRoom"
            type="button"
            class="setting-row"
            @click="handleToggleMute(currentRoom.id)"
          >
            <span>消息免打扰</span>
            <i :class="{ active: isRoomMuted(currentRoom.id) }"></i>
          </button>
        </section>
      </aside>
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

    <!-- Member preview dialog -->
    <el-dialog v-model="showMembersDialog" title="群成员" width="560px" class="chat-soft-dialog">
      <div class="member-dialog-list">
        <div
          v-for="member in memberDialogList"
          :key="member.userId || member.id"
          class="member-dialog-item"
        >
          <img :src="fixAvatarUrl(member.avatar)" :alt="member.nickname || member.username || '成员头像'" />
          <div>
            <strong>{{ member.nickname || member.username || '匿名成员' }}</strong>
            <span>{{ getMemberRoleText(member) }}</span>
          </div>
        </div>
        <p v-if="memberDialogList.length === 0" class="dialog-empty-text">暂无可展示成员</p>
      </div>
      <template #footer>
        <el-button @click="showMembersDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Announcement dialog -->
    <el-dialog
      v-model="showNoticeDialog"
      :title="canManageRoom ? '编辑群公告' : '查看群公告'"
      width="520px"
      class="chat-soft-dialog"
    >
      <div class="notice-dialog-body">
        <el-input
          v-model="noticeDraft"
          type="textarea"
          :rows="5"
          maxlength="120"
          show-word-limit
          :disabled="!canManageRoom"
          placeholder="请输入群公告"
        />
        <p class="permission-hint">
          {{ canManageRoom ? '公告将保存到服务器，所有成员可见。' : '只有教师和管理员可以编辑群公告。' }}
        </p>
      </div>
      <template #footer>
        <el-button @click="showNoticeDialog = false">{{ canManageRoom ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canManageRoom" type="primary" @click="saveNoticeDraft">保存公告</el-button>
      </template>
    </el-dialog>

    <!-- Room settings dialog -->
    <el-dialog v-model="showRoomSettingsDialog" title="群设置" width="560px" class="chat-soft-dialog">
      <div class="settings-dialog-body">
        <section class="settings-dialog-section">
          <h4>我的聊天设置</h4>
          <button
            v-if="currentRoom"
            type="button"
            class="setting-row dialog-setting-row"
            @click="handleTogglePin(currentRoom.id)"
          >
            <span>置顶聊天</span>
            <i :class="{ active: isRoomPinned(currentRoom.id) }"></i>
          </button>
          <button
            v-if="currentRoom"
            type="button"
            class="setting-row dialog-setting-row"
            @click="handleToggleMute(currentRoom.id)"
          >
            <span>消息免打扰</span>
            <i :class="{ active: isRoomMuted(currentRoom.id) }"></i>
          </button>
        </section>

        <section class="settings-dialog-section">
          <div class="settings-section-title">
            <h4>群资料</h4>
            <span v-if="!canManageRoom">仅教师/管理员可修改</span>
          </div>
          <el-form label-position="top">
            <el-form-item label="群名称">
              <el-input v-model="roomForm.name" :disabled="!canManageRoom" placeholder="请输入群名称" />
            </el-form-item>
            <el-form-item label="所属学院">
              <el-select v-model="roomForm.college" :disabled="!canManageRoom" placeholder="全校大厅" clearable>
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
            <el-form-item label="群类型">
              <el-radio-group v-model="roomForm.roomLevel" :disabled="!canManageRoom">
                <el-radio value="FREE">公开群</el-radio>
                <el-radio value="VIP">VIP群</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </section>
      </div>
      <template #footer>
        <el-button @click="showRoomSettingsDialog = false">{{ canManageRoom ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canManageRoom" type="primary" @click="saveRoomSettings">保存设置</el-button>
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
  getRoomSettings,
  updateRoom,
  getRoomMembers,
  updateRoomNotice,
  uploadChatFile
} from '@/api/chat'
import chatWebSocket from '@/utils/chat-websocket'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const NOTICE_STORAGE_KEY = 'miaomiao_chat_room_notices'
const defaultNotice = '请文明交流，讨论题目思路时尽量说明题号、语言和报错信息。'

const fixAvatarUrl = (url) => {
  if (!url) return defaultAvatar
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

// State
const messages = ref([])
const inputMessage = ref('')
const messageListRef = ref(null)
const messageInputRef = ref(null)
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
const likedMessageKeys = ref([])
const showMembersDialog = ref(false)
const showNoticeDialog = ref(false)
const showRoomSettingsDialog = ref(false)
const noticeDraft = ref('')
const noticeStore = ref({})
const noticeTimeStore = ref({})
const roomForm = ref({ name: '', college: '', roomLevel: 'FREE' })
const roomMembers = ref([])
const uploading = ref(false)
const INFO_PANEL_KEY = 'miaomiao_chat_info_panel_visible'
const showInfoPanel = ref(localStorage.getItem(INFO_PANEL_KEY) === 'true')

const toggleInfoPanel = () => {
  showInfoPanel.value = !showInfoPanel.value
  localStorage.setItem(INFO_PANEL_KEY, String(showInfoPanel.value))
}

const currentUserId = computed(() => userStore.getUserId)
const isTeacher = computed(() => Number(userStore.getUserRoleId) >= 3)
const canManageRoom = computed(() => Number(userStore.getUserRoleId) >= 3)
const roomDisplayName = computed(() => currentRoom.value?.roomName || currentRoom.value?.name || '综合交流大厅')
const roomMemberCount = computed(() => {
  if (roomMembers.value.length > 0) return roomMembers.value.length
  const explicit = currentRoom.value?.memberCount || currentRoom.value?.userCount || currentRoom.value?.onlineCount
  return explicit || 0
})
const memberPreview = computed(() => roomMembers.value.slice(0, 6))
const memberDialogList = computed(() => roomMembers.value.slice(0, 24))
const currentRoomNoticeKey = computed(() => String(currentRoom.value?.id || 'default'))
const currentNoticeText = computed(() => {
  const backendNotice = currentRoom.value?.notice
  if (backendNotice) return backendNotice
  return noticeStore.value[currentRoomNoticeKey.value] || defaultNotice
})
const currentNoticeTime = computed(() => {
  const backendTime = currentRoom.value?.noticeUpdatedAt
  if (backendTime) return dayjs(backendTime).format('YYYY-MM-DD HH:mm')
  return noticeTimeStore.value[currentRoomNoticeKey.value] || dayjs().format('YYYY-MM-DD HH:mm')
})

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

const formatFileSize = (bytes) => {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleFileUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  e.target.value = ''
  uploading.value = true
  try {
    const res = await uploadChatFile(file)
    if (res && res.code === 200) {
      const { url, messageType, fileName, fileSize } = res.data
      sendFileMessage(messageType, url, fileName, fileSize)
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const sendFileMessage = (messageType, url, fileName, fileSize) => {
  if (!currentRoom.value) {
    ElMessage.warning('请先选择聊天室')
    return
  }
  const optimisticMsg = {
    id: Date.now(),
    senderId: currentUserId.value,
    senderName: userStore.getUserName || '',
    senderAvatar: userStore.getUserAvatar || '',
    senderRole: userStore.getUserRoleId,
    content: url,
    messageType,
    fileName,
    fileSize,
    createTime: new Date().toISOString()
  }
  messages.value.push(optimisticMsg)
  scrollToBottom()
  const sent = chatWebSocket.sendRoomMessage({
    roomId: currentRoom.value.id,
    messageType,
    content: url,
    fileName,
    fileSize
  })
  if (!sent) {
    messages.value.pop()
    ElMessage.warning('发送失败，请检查连接状态')
  }
}

const getMemberRoleText = (member) => {
  const role = Number(member.roleId || member.role_id || member.role)
  if (role === 4) return '管理员'
  if (role === 3) return '教师'
  if (role === 2) return 'VIP学生'
  return member.roleName || '学生'
}

const loadNoticeStore = () => {
  try {
    const raw = localStorage.getItem(NOTICE_STORAGE_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw)
    noticeStore.value = parsed.notices || {}
    noticeTimeStore.value = parsed.times || {}
  } catch (e) {
    console.error('读取群公告缓存失败:', e)
  }
}

const persistNoticeStore = () => {
  localStorage.setItem(NOTICE_STORAGE_KEY, JSON.stringify({
    notices: noticeStore.value,
    times: noticeTimeStore.value
  }))
}

const openMembersDialog = () => {
  showMembersDialog.value = true
}

const openNoticeDialog = () => {
  noticeDraft.value = currentNoticeText.value
  showNoticeDialog.value = true
}

const saveNoticeDraft = async () => {
  if (!canManageRoom.value) {
    ElMessage.warning('只有教师和管理员可以编辑群公告')
    return
  }
  const content = noticeDraft.value.trim()
  if (!content) {
    ElMessage.warning('公告内容不能为空')
    return
  }
  const key = currentRoomNoticeKey.value

  // Save to backend
  if (currentRoom.value?.id) {
    try {
      const res = await updateRoomNotice(currentRoom.value.id, content)
      if (res && res.code === 200) {
        // Update local room data
        currentRoom.value = { ...currentRoom.value, notice: content, noticeUpdatedAt: new Date().toISOString() }
        rooms.value = rooms.value.map(r => r.id === currentRoom.value.id ? { ...r, notice: content } : r)
      }
    } catch (e) {
      console.error('保存公告到后端失败:', e)
    }
  }

  // Also save to localStorage as fallback
  noticeStore.value = { ...noticeStore.value, [key]: content }
  noticeTimeStore.value = { ...noticeTimeStore.value, [key]: dayjs().format('YYYY-MM-DD HH:mm') }
  persistNoticeStore()
  showNoticeDialog.value = false
  ElMessage.success('群公告已更新')
}

const openRoomSettings = () => {
  if (!currentRoom.value) {
    ElMessage.warning('请先选择群聊')
    return
  }
  roomForm.value = {
    name: currentRoom.value.roomName || currentRoom.value.name || '',
    college: currentRoom.value.college || '',
    roomLevel: currentRoom.value.roomLevel || 'FREE'
  }
  showRoomSettingsDialog.value = true
}

const saveRoomSettings = async () => {
  if (!canManageRoom.value) {
    ElMessage.warning('只有教师和管理员可以修改群资料')
    return
  }
  if (!currentRoom.value?.id) {
    ElMessage.warning('请先选择群聊')
    return
  }
  const name = roomForm.value.name.trim()
  if (!name) {
    ElMessage.warning('群名称不能为空')
    return
  }
  const payload = {
    name,
    college: roomForm.value.college || '',
    roomLevel: roomForm.value.roomLevel || 'FREE'
  }
  try {
    const res = await updateRoom(currentRoom.value.id, payload)
    if (res && res.code === 200) {
      const nextRoom = {
        ...currentRoom.value,
        ...(res.data || {}),
        name: payload.name,
        roomName: payload.name,
        college: payload.college,
        roomLevel: payload.roomLevel
      }
      currentRoom.value = nextRoom
      rooms.value = rooms.value.map(room => room.id === nextRoom.id ? { ...room, ...nextRoom } : room)
      showRoomSettingsDialog.value = false
      ElMessage.success('群设置已保存')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    console.error('保存群设置失败:', e)
    ElMessage.error('保存失败')
  }
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

  // Load room members
  await loadRoomMembers(room.id)

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

const loadRoomMembers = async (roomId) => {
  try {
    const res = await getRoomMembers(roomId)
    if (res && res.code === 200) {
      roomMembers.value = res.data || []
    }
  } catch (e) {
    console.error('加载群成员失败:', e)
    roomMembers.value = []
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
  loadNoticeStore()
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

.info-toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 6px;
  background: #fff;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.info-toggle-btn:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary);
  border-color: rgba(37, 99, 235, 0.3);
}

.info-toggle-btn.active {
  background: var(--app-primary-soft);
  color: var(--app-primary);
  border-color: rgba(37, 99, 235, 0.4);
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

/* Student visual system chat refresh */
.chat-hub {
  background:
      radial-gradient(circle at 18% 8%, rgba(37, 99, 235, 0.10), transparent 30%),
      radial-gradient(circle at 88% 0%, rgba(124, 92, 255, 0.09), transparent 26%),
      linear-gradient(180deg, #f8fbff 0%, #f3f7fe 48%, #edf4fb 100%);
}

.hub-header {
  height: 64px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.84);
  border-bottom: 1px solid rgba(207, 220, 240, 0.88);
  box-shadow: 0 12px 28px rgba(40, 78, 142, 0.06);
  backdrop-filter: blur(18px);
}

.header-back {
  border: 1px solid rgba(207, 220, 240, 0.88);
  background: #fff;
  color: var(--app-primary);
}

.header-title {
  font-size: 20px;
  font-weight: 900;
}

.hub-body {
  display: grid;
  grid-template-columns: 310px minmax(0, 1fr) 330px;
  gap: 18px;
  height: calc(100vh - 64px);
  padding: 18px;
}

.hub-body.panel-collapsed {
  grid-template-columns: 310px minmax(0, 1fr);
}

.left-panel,
.right-panel,
.info-panel {
  min-height: 0;
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 14px 34px rgba(40, 78, 142, 0.08);
  overflow: hidden;
}

.left-panel {
  width: auto;
}

.room-section,
.contact-section {
  padding: 18px;
}

.section-title,
.room-section .section-title,
.contact-section .section-title {
  color: var(--app-text);
  font-weight: 850;
}

.join-room-btn,
.add-room-btn {
  border-radius: 8px;
  font-weight: 800;
}

.room-card,
.contact-card {
  border-radius: 8px;
  border: 1px solid transparent;
}

.room-card.active {
  border-left: 0;
  border-color: rgba(37, 99, 235, 0.28);
  background: linear-gradient(135deg, rgba(232, 240, 255, 0.96), rgba(244, 239, 255, 0.84));
}

.search-input {
  height: 42px;
  background: #f7faff;
  border-color: rgba(207, 220, 240, 0.88);
}

.right-panel {
  flex: initial;
}

.room-header {
  height: 64px;
  padding: 0 22px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(207, 220, 240, 0.88);
}

.room-header-name {
  font-size: 18px;
  font-weight: 900;
}

.message-list {
  padding: 22px;
  background:
      linear-gradient(180deg, rgba(248, 251, 255, 0.72), rgba(255, 255, 255, 0.88));
}

.bubble {
  border-radius: 8px;
  box-shadow: 0 10px 22px rgba(40, 78, 142, 0.08);
}

.bubble-self {
  color: #fff;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
}

.bubble-peer {
  color: var(--app-text);
  background: #fff;
  border: 1px solid rgba(218, 229, 245, 0.9);
}

.bubble-vip.bubble-self,
.bubble-teacher.bubble-self,
.bubble-admin.bubble-self {
  color: #fff;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
}

.input-bar {
  min-height: 74px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.92);
  border-top: 1px solid rgba(207, 220, 240, 0.88);
}

.msg-input {
  height: 46px;
  border-color: rgba(207, 220, 240, 0.92);
  border-radius: 8px;
  background: #f8fbff;
}

.msg-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.10);
}

.send-btn {
  height: 46px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
  font-weight: 850;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.22);
}

.send-disabled {
  background: #d4deec;
  box-shadow: none;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 14px;
  overflow-y: auto;
}

.info-card {
  padding: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
}

.info-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.info-card-header h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 16px;
  font-weight: 900;
}

.panel-icon-btn,
.info-link-btn,
.copy-inline-btn {
  border: 0;
  background: transparent;
  color: var(--app-primary);
  cursor: pointer;
  font-weight: 800;
}

.room-profile-card {
  text-align: center;
}

.room-avatar-large {
  width: 82px;
  height: 82px;
  display: grid;
  place-items: center;
  margin: 0 auto 14px;
  color: #fff;
  border-radius: 26px;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
  box-shadow: 0 18px 34px rgba(37, 99, 235, 0.24);
  font-size: 24px;
  font-weight: 900;
}

.room-profile-card h2 {
  margin: 0 0 6px;
  color: var(--app-text);
  font-size: 20px;
  font-weight: 900;
}

.room-profile-card p {
  margin: 0 0 16px;
  color: var(--app-text-muted);
  font-size: 13px;
}

.room-profile-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.room-profile-stats div {
  min-width: 0;
  padding: 10px 6px;
  background: #f7faff;
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
}

.room-profile-stats strong {
  display: block;
  color: var(--app-text);
  font-size: 15px;
  font-weight: 900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-profile-stats span {
  color: var(--app-text-muted);
  font-size: 11px;
  font-weight: 750;
}

.member-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.member-preview img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 8px 18px rgba(40, 78, 142, 0.12);
}

.empty-inline {
  color: var(--app-text-muted);
  font-size: 13px;
}

.member-legend {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
  color: var(--app-text-muted);
  font-size: 12px;
  font-weight: 700;
}

.legend-dot {
  width: 7px;
  height: 7px;
  display: inline-block;
  margin-right: 4px;
  border-radius: 50%;
  background: #94a3b8;
}

.legend-dot.admin {
  background: #7c3aed;
}

.legend-dot.teacher {
  background: #06b6d4;
}

.legend-dot.vip {
  background: #f59e0b;
}

.notice-box {
  padding: 14px;
  background: #f8fbff;
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
}

.notice-pin {
  display: inline-flex;
  padding: 2px 8px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 850;
}

.notice-box p {
  margin: 10px 0;
  color: var(--app-text);
  line-height: 1.65;
}

.notice-box small {
  color: var(--app-text-muted);
}

.setting-row {
  width: 100%;
  min-height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
  color: var(--app-text);
  background: transparent;
  border: 0;
  cursor: pointer;
  font-weight: 750;
}

.setting-row i {
  width: 42px;
  height: 24px;
  position: relative;
  border-radius: 999px;
  background: #d7e1ef;
  transition: background-color 160ms ease;
}

.setting-row i::after {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 3px 8px rgba(40, 78, 142, 0.20);
  transition: transform 160ms ease;
}

.setting-row i.active {
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
}

.setting-row i.active::after {
  transform: translateX(18px);
}

/* QQ-inspired message composition */
.message-row {
  gap: 10px;
  margin-bottom: 18px;
}

.message-row.self {
  flex-direction: row-reverse;
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

.sender-name,
.message-row.self .sender-name {
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

.msg-avatar {
  width: 42px;
  height: 42px;
  box-shadow: 0 8px 18px rgba(40, 78, 142, 0.12);
}

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
}

.bubble-self,
.bubble-vip.bubble-self,
.bubble-teacher.bubble-self,
.bubble-admin.bubble-self {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, #2f7df5, #7560f5);
}

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

/* Right rail dialogs and editable settings */
.chat-soft-dialog :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(207, 220, 240, 0.96);
  box-shadow: 0 22px 60px rgba(40, 78, 142, 0.18);
}

.member-dialog-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
}

.member-dialog-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
  background: #f8fbff;
}

.member-dialog-item img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 8px 18px rgba(40, 78, 142, 0.10);
}

.member-dialog-item strong {
  display: block;
  color: var(--app-text);
  font-weight: 900;
}

.member-dialog-item span,
.dialog-empty-text,
.permission-hint {
  color: var(--app-text-muted);
  font-size: 13px;
}

.permission-hint {
  margin: 10px 0 0;
}

.settings-dialog-body {
  display: grid;
  gap: 14px;
}

.settings-dialog-section {
  padding: 14px;
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
  background: #f8fbff;
}

.settings-dialog-section h4,
.settings-section-title h4 {
  margin: 0 0 12px;
  color: var(--app-text);
  font-size: 15px;
  font-weight: 900;
}

.settings-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.settings-section-title span {
  color: #d97706;
  font-size: 12px;
  font-weight: 850;
}

.dialog-setting-row {
  min-height: 46px;
  padding: 0 4px;
}

.panel-icon-btn {
  padding: 4px 0;
}

@media (max-width: 1180px) {
  .hub-body {
    grid-template-columns: 300px minmax(0, 1fr);
  }

  .info-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .hub-body {
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .left-panel,
  .info-panel {
    display: none;
  }

  .right-panel {
    width: 100%;
  }

  .hub-header {
    padding: 0 14px;
  }

  .msg-body {
    max-width: calc(100% - 56px);
  }

  .member-dialog-list {
    grid-template-columns: 1fr;
  }
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.15s;
}

.upload-btn:hover {
  border-color: var(--app-primary);
}

.bubble-image img {
  max-width: 240px;
  max-height: 240px;
  border-radius: 8px;
  cursor: pointer;
  display: block;
}

.bubble-image img:hover { opacity: 0.9; }

.bubble-code { min-width: 260px; max-width: 400px; }

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 6px;
  margin-bottom: 6px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}

.code-filename { font-size: 12px; font-weight: 700; color: #64748b; }

.code-download { font-size: 11px; color: var(--app-primary); text-decoration: none; font-weight: 700; }
.code-download:hover { text-decoration: underline; }

.code-preview {
  margin: 0;
  padding: 8px;
  background: #1e293b;
  color: #e2e8f0;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.code-preview code { white-space: pre; }

.bubble-file {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 200px;
}

.file-icon { font-size: 28px; flex-shrink: 0; }
.file-info { flex: 1; min-width: 0; }

.file-name {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #1f2a44;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size { font-size: 11px; color: #94a3b8; }

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

.file-download-btn:hover { opacity: 0.85; }
</style>
