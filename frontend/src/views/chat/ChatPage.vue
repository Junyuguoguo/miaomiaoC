<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <el-button @click="$router.back()" icon="ArrowLeft">返回</el-button>
      <h3>{{ chatTitle }}</h3>
      <el-tag v-if="connectionStatus" type="success" size="small">已连接</el-tag>
      <el-tag v-else type="danger" size="small">未连接</el-tag>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <div 
        v-for="msg in messages" 
        :key="msg.id"
        :class="['message-item', msg.senderId === currentUserId ? 'message-self' : 'message-other']"
      >
        <div class="message-avatar">
          <el-avatar :size="40">{{ msg.senderName?.charAt(0) || 'U' }}</el-avatar>
        </div>
        <div class="message-content">
          <div class="message-info">
            <span class="sender-name">{{ msg.senderName }}</span>
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <div class="message-text">{{ msg.content }}</div>
        </div>
      </div>
      
      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMoreMessages" :loading="loadingMore">加载更多</el-button>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入消息..."
        @keyup.enter.ctrl="sendMessage"
      />
      <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import chatWebSocket from '@/utils/chat-websocket'
import { getPrivateMessages, markAsRead } from '@/api/chat'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(0)
const messageListRef = ref(null)
const connectionStatus = ref(false)

// 计算属性
const currentUserId = computed(() => userStore.userInfo?.id)
const chatTitle = computed(() => {
  const type = route.query.type
  if (type === 'private') {
    return `与 ${route.query.userName || '用户'} 的聊天`
  } else if (type === 'room') {
    return route.query.roomName || '群聊'
  }
  return '聊天'
})

// 加载历史消息
const loadMessages = async (page = 0) => {
  try {
    const type = route.query.type
    let response
    
    if (type === 'private') {
      const userId2 = route.query.userId
      response = await getPrivateMessages(userId2, page, 20)
    } else if (type === 'room') {
      const roomId = route.query.roomId
      response = await getRoomMessages(roomId, page, 20)
    }
    
    if (response.code === 200) {
      const newMessages = response.data.content
      if (page === 0) {
        messages.value = newMessages.reverse() // 反转使最新消息在底部
      } else {
        messages.value = [...newMessages.reverse(), ...messages.value]
      }
      
      hasMore.value = !response.data.last
      currentPage.value = page
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

// 加载更多消息
const loadMoreMessages = async () => {
  loadingMore.value = true
  try {
    await loadMessages(currentPage.value + 1)
  } finally {
    loadingMore.value = false
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  
  const messageData = {
    messageType: 'TEXT',
    content: inputMessage.value.trim()
  }
  
  // 根据聊天类型添加不同参数
  const type = route.query.type
  if (type === 'private') {
    messageData.receiverId = route.query.userId
    chatWebSocket.sendPrivateMessage(messageData)
  } else if (type === 'room') {
    messageData.roomId = route.query.roomId
    chatWebSocket.sendRoomMessage(messageData)
  }
  
  // 清空输入框
  inputMessage.value = ''
}

// 处理接收到的消息
const handleMessageReceived = (message) => {
  messages.value.push(message)
  nextTick(() => {
    scrollToBottom()
  })
  
  // 标记为已读
  if (message.senderId !== currentUserId.value) {
    markAsRead([message.id]).catch(console.error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

// 连接 WebSocket
const connectWebSocket = async () => {
  try {
    await chatWebSocket.connect(userStore.token)
    connectionStatus.value = true
    
    // 订阅消息
    if (currentUserId.value) {
      chatWebSocket.subscribePrivateMessages(currentUserId.value, handleMessageReceived)
      
      // 如果是群聊，订阅房间消息
      if (route.query.type === 'room' && route.query.roomId) {
        chatWebSocket.subscribeRoomMessages(route.query.roomId, handleMessageReceived)
      }
    }
    
    // 监听错误
    chatWebSocket.onError((error) => {
      ElMessage.error(error.message || '消息发送失败')
    })
  } catch (error) {
    console.error('WebSocket 连接失败:', error)
    ElMessage.error('实时消息连接失败')
  }
}

// 生命周期
onMounted(async () => {
  // 加载历史消息
  await loadMessages(0)
  
  // 连接 WebSocket
  await connectWebSocket()
})

onUnmounted(() => {
  // 断开 WebSocket 连接（可选，如果希望保持连接可以注释掉）
  // chatWebSocket.disconnect()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.chat-header h3 {
  margin: 0;
  flex: 1;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
  margin: 0 12px;
}

.message-self .message-content {
  text-align: right;
}

.message-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
  color: #999;
}

.message-text {
  background-color: white;
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message-self .message-text {
  background-color: #409EFF;
  color: white;
}

.load-more {
  text-align: center;
  padding: 16px 0;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
}

.chat-input :deep(.el-textarea__inner) {
  resize: none;
}
</style>
