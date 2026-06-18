import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

/**
 * WebSocket 聊天客户端
 * 
 * 使用 STOMP over SockJS 实现实时消息通信
 */
class ChatWebSocketClient {
    constructor() {
        this.stompClient = null
        this.isConnected = false
        this.messageHandlers = new Map() // 存储消息处理器
        this.errorHandlers = [] // 存储错误处理器
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectDelay = 3000 // 重连延迟（毫秒）
    }

    /**
     * 连接 WebSocket
     * @param {string} token - JWT Token
     */
    connect(token) {
        if (this.isConnected) {
            console.warn('WebSocket 已经连接')
            return Promise.resolve()
        }

        return new Promise((resolve, reject) => {
            try {
                // 创建 SockJS 连接
                const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/ws-chat?token=${token}`)
                
                // 创建 STOMP 客户端
                this.stompClient = Stomp.over(socket)
                
                // 禁用调试日志（生产环境）
                this.stompClient.debug = null
                
                // 配置心跳
                this.stompClient.heartbeat.outgoing = 20000 // 客户端发送心跳间隔
                this.stompClient.heartbeat.incoming = 20000  // 服务端发送心跳间隔

                // 连接到服务器
                this.stompClient.connect(
                    {},
                    () => {
                        this.isConnected = true
                        this.reconnectAttempts = 0
                        console.log('WebSocket 连接成功')
                        
                        // 订阅错误队列
                        this.subscribeToErrors()
                        
                        resolve()
                    },
                    (error) => {
                        console.error('WebSocket 连接失败:', error)
                        this.isConnected = false
                        this.handleReconnect(token)
                        reject(error)
                    }
                )
            } catch (error) {
                console.error('WebSocket 初始化失败:', error)
                reject(error)
            }
        })
    }

    /**
     * 断开连接
     */
    disconnect() {
        if (this.stompClient && this.isConnected) {
            this.stompClient.disconnect(() => {
                this.isConnected = false
                this.messageHandlers.clear()
                console.log('WebSocket 已断开')
            })
        }
    }

    /**
     * 订阅单聊消息
     * @param {number} userId - 当前用户ID
     * @param {function} handler - 消息处理函数
     */
    subscribePrivateMessages(userId, handler) {
        if (!this.isConnected) {
            console.warn('WebSocket 未连接，无法订阅消息')
            return
        }

        const destination = `/user/${userId}/queue/messages`
        
        this.stompClient.subscribe(destination, (message) => {
            try {
                const data = JSON.parse(message.body)
                if (data.code === 200) {
                    handler(data.data)
                } else {
                    console.error('接收消息失败:', data.message)
                }
            } catch (error) {
                console.error('解析消息失败:', error)
            }
        })

        console.log(`已订阅单聊消息: ${destination}`)
    }

    /**
     * 订阅群聊消息
     * @param {number} roomId - 房间ID
     * @param {function} handler - 消息处理函数
     */
    subscribeRoomMessages(roomId, handler) {
        if (!this.isConnected) {
            console.warn('WebSocket 未连接，无法订阅消息')
            return
        }

        const destination = `/topic/room/${roomId}`
        
        this.stompClient.subscribe(destination, (message) => {
            try {
                const data = JSON.parse(message.body)
                if (data.code === 200) {
                    handler(data.data)
                } else {
                    console.error('接收消息失败:', data.message)
                }
            } catch (error) {
                console.error('解析消息失败:', error)
            }
        })

        console.log(`已订阅群聊消息: ${destination}`)
    }

    /**
     * 订阅错误消息
     */
    subscribeToErrors() {
        this.stompClient.subscribe('/user/queue/errors', (message) => {
            try {
                const data = JSON.parse(message.body)
                console.error('WebSocket 错误:', data.message)
                
                // 调用所有注册的错误处理器
                this.errorHandlers.forEach(handler => handler(data))
            } catch (error) {
                console.error('解析错误消息失败:', error)
            }
        })
    }

    /**
     * 发送单聊消息
     * @param {object} messageData - 消息数据 { receiverId, messageType, content }
     */
    sendPrivateMessage(messageData) {
        if (!this.isConnected) {
            console.warn('WebSocket 未连接，无法发送消息')
            return false
        }

        this.stompClient.send('/app/chat/private', {}, JSON.stringify(messageData))
        return true
    }

    /**
     * 发送群聊消息
     * @param {object} messageData - 消息数据 { roomId, messageType, content }
     */
    sendRoomMessage(messageData) {
        if (!this.isConnected) {
            console.warn('WebSocket 未连接，无法发送消息')
            return false
        }

        this.stompClient.send('/app/chat/room', {}, JSON.stringify(messageData))
        return true
    }

    /**
     * 标记消息为已读
     * @param {Array<number>} messageIds - 消息ID列表
     */
    markAsRead(messageIds) {
        if (!this.isConnected) {
            console.warn('WebSocket 未连接，无法标记已读')
            return false
        }

        this.stompClient.send('/app/chat/read', {}, JSON.stringify(messageIds))
        return true
    }

    /**
     * 注册错误处理器
     * @param {function} handler - 错误处理函数
     */
    onError(handler) {
        this.errorHandlers.push(handler)
    }

    /**
     * 处理重连逻辑
     */
    handleReconnect(token) {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('达到最大重连次数，停止重连')
            return
        }

        this.reconnectAttempts++
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1) // 指数退避
        
        console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})，延迟 ${delay}ms`)
        
        setTimeout(() => {
            this.connect(token).catch(() => {
                this.handleReconnect(token)
            })
        }, delay)
    }

    /**
     * 获取连接状态
     */
    getConnectionStatus() {
        return this.isConnected
    }
}

// 创建单例实例
const chatWebSocket = new ChatWebSocketClient()

export default chatWebSocket
