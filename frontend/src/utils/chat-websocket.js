/**
 * WebSocket 聊天客户端 (纯原生 WebSocket + 轻量 STOMP 协议)
 *
 * 不依赖任何外部库，直接通过浏览器原生 WebSocket 连接
 * Spring SockJS 端点: /ws-chat/websocket
 */

// ────────────────────────── STOMP 帧编解码 ──────────────────────────

/**
 * 编码 STOMP 帧
 * @param {string} command - STOMP 命令 (CONNECT, SUBSCRIBE, SEND, DISCONNECT ...)
 * @param {Object} headers - 键值对头
 * @param {string} [body=''] - 消息体
 * @returns {string} 完整的 STOMP 帧字符串
 */
function encodeFrame(command, headers = {}, body = '') {
    let frame = command + '\n'
    for (const key of Object.keys(headers)) {
        frame += key + ':' + headers[key] + '\n'
    }
    frame += '\n' + body + '\0'
    return frame
}

/**
 * 解码接收到的原始数据，返回 STOMP 帧数组
 * 一次 onmessage 可能包含多个帧，也可能包含心跳 (\n)
 * @param {string} data - 原始字符串
 * @returns {Array<{command: string, headers: Object, body: string}>}
 */
function decodeFrames(data) {
    const frames = []
    // 心跳帧就是单独的 \n，先去掉
    const cleaned = data.replace(/^\n+|\n+$/g, '')
    if (!cleaned) return frames

    // 用 \0 分割多个帧
    const rawFrames = cleaned.split('\0')
    for (const raw of rawFrames) {
        const trimmed = raw.replace(/^\n+|\n+$/g, '')
        if (!trimmed) continue

        const lines = trimmed.split('\n')
        const command = lines[0].trim()
        const headers = {}
        let bodyStart = -1

        for (let i = 1; i < lines.length; i++) {
            if (lines[i].trim() === '') {
                bodyStart = i + 1
                break
            }
            const colonIndex = lines[i].indexOf(':')
            if (colonIndex !== -1) {
                const key = lines[i].substring(0, colonIndex).trim()
                const value = lines[i].substring(colonIndex + 1).trim()
                headers[key] = value
            }
        }

        const body = bodyStart >= 0 ? lines.slice(bodyStart).join('\n') : ''
        frames.push({ command, headers, body })
    }
    return frames
}

// ────────────────────────── ChatWebSocketClient ──────────────────────────

class ChatWebSocketClient {
    constructor() {
        this._ws = null
        this.isConnected = false
        this.messageHandlers = new Map()   // subscriptionId -> handler
        this.errorHandlers = []
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectDelay = 3000
        this._token = null
        this._userId = null
        this._subscriptionCounter = 0
        this._pendingConnectResolve = null
        this._pendingConnectReject = null
        this._heartbeatTimer = null
        this._serverHeartbeatTimer = null
        this._heartbeatInterval = 20000  // 20s
        this._closing = false            // 主动断开标记
    }

    /**
     * 将 HTTP(S) 基础地址转为 WS(S) 地址
     * @param {string} url
     * @returns {string}
     */
    _toWsUrl(url) {
        if (url.startsWith('https://')) {
            return 'wss://' + url.slice(8)
        }
        if (url.startsWith('http://')) {
            return 'ws://' + url.slice(7)
        }
        return url
    }

    /**
     * 连接 WebSocket
     * @param {string} token - SHA-256 Token
     * @param {number} userId - 当前用户 ID
     * @returns {Promise<void>}
     */
    connect(token, userId) {
        if (this.isConnected) {
            console.warn('WebSocket 已经连接')
            return Promise.resolve()
        }

        // 如果有上一次残留的连接，先清理
        if (this._ws) {
            this._cleanup()
        }

        this._token = token
        this._userId = userId
        this._closing = false

        const rawBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
        const baseUrl = rawBaseUrl.replace(/\/+$/, '')
        const wsBase = this._toWsUrl(baseUrl)

        // SockJS raw WebSocket 端点: /ws-chat/websocket
        const url = `${wsBase}/ws-chat/websocket?token=${encodeURIComponent(token)}&userId=${userId}`

        return new Promise((resolve, reject) => {
            this._pendingConnectResolve = resolve
            this._pendingConnectReject = reject

            try {
                this._ws = new WebSocket(url)
            } catch (err) {
                this._pendingConnectResolve = null
                this._pendingConnectReject = null
                reject(new Error('WebSocket 创建失败: ' + err.message))
                return
            }

            this._ws.onopen = () => {
                console.log('WebSocket 原始连接已建立，发送 STOMP CONNECT...')
                this._sendConnectFrame()
                this._startServerHeartbeatWatch()
            }

            this._ws.onmessage = (event) => {
                this._onMessage(event.data)
            }

            this._ws.onerror = (event) => {
                console.error('WebSocket 错误:', event)
                this.isConnected = false
                const rejectFn = this._pendingConnectReject
                this._pendingConnectResolve = null
                this._pendingConnectReject = null
                if (rejectFn) {
                    rejectFn(new Error('WebSocket 连接失败'))
                }
                this._handleReconnect()
            }

            this._ws.onclose = (event) => {
                console.log('WebSocket 已断开', event.code, event.reason)
                this.isConnected = false
                this._stopHeartbeats()
                const rejectFn = this._pendingConnectReject
                this._pendingConnectResolve = null
                this._pendingConnectReject = null
                if (rejectFn) {
                    rejectFn(new Error('WebSocket 连接已关闭'))
                }
                if (!this._closing) {
                    this._handleReconnect()
                }
            }
        })
    }

    /**
     * 断开连接
     */
    disconnect() {
        this._closing = true
        if (this._ws) {
            // 发送 STOMP DISCONNECT 帧
            try {
                this._ws.send(encodeFrame('DISCONNECT', {}))
            } catch (_) { /* ignore */ }
            this._ws.close()
        }
        this._cleanup()
        console.log('WebSocket 已断开')
    }

    /**
     * 清理内部状态
     */
    _cleanup() {
        this._stopHeartbeats()
        this._ws = null
        this.isConnected = false
        this.messageHandlers.clear()
        this._pendingConnectResolve = null
        this._pendingConnectReject = null
    }

    /**
     * 发送 STOMP CONNECT 帧
     */
    _sendConnectFrame() {
        const frame = encodeFrame('CONNECT', {
            'accept-version': '1.1,1.2',
            'heart-beat': `${this._heartbeatInterval},${this._heartbeatInterval}`,
        })
        this._ws.send(frame)
    }

    /**
     * 处理收到的原始消息
     * @param {string} data
     */
    _onMessage(data) {
        // 心跳: 服务器发来的单个 \n
        if (data === '\n') {
            this._resetServerHeartbeatWatch()
            // 回复心跳
            this._sendHeartbeat()
            return
        }

        const frames = decodeFrames(data)
        for (const frame of frames) {
            switch (frame.command) {
                case 'CONNECTED':
                    this._onConnected(frame)
                    break
                case 'MESSAGE':
                    this._onStompMessage(frame)
                    break
                case 'ERROR':
                    this._onStompError(frame)
                    break
                case 'RECEIPT':
                    // 可选处理
                    break
                default:
                    break
            }
        }
    }

    /**
     * 收到 CONNECTED 帧
     */
    _onConnected(frame) {
        this.isConnected = true
        this.reconnectAttempts = 0
        console.log('STOMP 连接成功', frame.headers)

        // 启动客户端心跳
        this._startClientHeartbeat()

        // 订阅错误队列
        this._subscribeToErrors()

        // 解析 connect promise
        if (this._pendingConnectResolve) {
            this._pendingConnectResolve()
            this._pendingConnectResolve = null
            this._pendingConnectReject = null
        }
    }

    /**
     * 收到 MESSAGE 帧，分发给对应的 handler
     */
    _onStompMessage(frame) {
        const subId = frame.headers['subscription']
        const handler = this.messageHandlers.get(subId)
        if (!handler) return

        try {
            const data = JSON.parse(frame.body)
            if (data.code === 200) {
                handler(data.data)
            } else {
                console.error('接收消息失败:', data.message)
            }
        } catch (error) {
            console.error('解析消息失败:', error)
        }
    }

    /**
     * 收到 ERROR 帧
     */
    _onStompError(frame) {
        const message = frame.headers['message'] || 'STOMP 协议错误'
        console.error('STOMP 错误:', message, frame.body || '')
        this.isConnected = false

        // 触发 errorHandlers
        this.errorHandlers.forEach(handler => {
            try {
                handler({ message, body: frame.body })
            } catch (_) { /* ignore */ }
        })

        const rejectFn = this._pendingConnectReject
        this._pendingConnectResolve = null
        this._pendingConnectReject = null
        if (rejectFn) {
            rejectFn(new Error(message))
        }

        this._handleReconnect()
    }

    /**
     * 发送客户端心跳
     */
    _sendHeartbeat() {
        if (this._ws && this._ws.readyState === WebSocket.OPEN) {
            this._ws.send('\n')
        }
    }

    /**
     * 启动客户端定时心跳
     */
    _startClientHeartbeat() {
        this._stopHeartbeats()
        this._heartbeatTimer = setInterval(() => {
            this._sendHeartbeat()
        }, this._heartbeatInterval)
    }

    /**
     * 启动服务器心跳超时监控
     * 如果在 2 倍心跳周期内没有收到任何数据，判定连接断开
     */
    _startServerHeartbeatWatch() {
        this._resetServerHeartbeatWatch()
    }

    _resetServerHeartbeatWatch() {
        if (this._serverHeartbeatTimer) {
            clearTimeout(this._serverHeartbeatTimer)
        }
        this._serverHeartbeatTimer = setTimeout(() => {
            console.warn('服务器心跳超时，关闭连接')
            if (this._ws) {
                this._ws.close()
            }
        }, this._heartbeatInterval * 2.5)
    }

    _stopHeartbeats() {
        if (this._heartbeatTimer) {
            clearInterval(this._heartbeatTimer)
            this._heartbeatTimer = null
        }
        if (this._serverHeartbeatTimer) {
            clearTimeout(this._serverHeartbeatTimer)
            this._serverHeartbeatTimer = null
        }
    }

    // ──────────────── 订阅 / 发送 ────────────────

    /**
     * 内部订阅方法：发送 SUBSCRIBE 帧，返回 subscriptionId
     * @param {string} destination
     * @param {function} handler
     * @returns {string} subscriptionId
     */
    _subscribe(destination, handler) {
        const subId = 'sub-' + (++this._subscriptionCounter)
        this.messageHandlers.set(subId, handler)

        const frame = encodeFrame('SUBSCRIBE', {
            id: subId,
            destination: destination,
        })
        this._ws.send(frame)

        console.log(`已订阅: ${destination} (${subId})`)
        return subId
    }

    /**
     * 订阅单聊消息
     * @param {number} userId - 当前用户 ID
     * @param {function} handler - 消息处理函数
     */
    subscribePrivateMessages(userId, handler) {
        if (!this.isConnected || !this._ws) {
            console.warn('WebSocket 未连接，无法订阅消息')
            return
        }
        this._subscribe(`/user/${userId}/queue/messages`, handler)
    }

    /**
     * 订阅群聊消息
     * @param {number} roomId - 房间 ID
     * @param {function} handler - 消息处理函数
     */
    subscribeRoomMessages(roomId, handler) {
        if (!this.isConnected || !this._ws) {
            console.warn('WebSocket 未连接，无法订阅消息')
            return
        }
        this._subscribe(`/topic/room/${roomId}`, handler)
    }

    /**
     * 订阅错误消息
     */
    _subscribeToErrors() {
        this._subscribe('/user/queue/errors', (data) => {
            console.error('WebSocket 错误:', data.message)
            this.errorHandlers.forEach(handler => handler(data))
        })
    }

    /**
     * 发送单聊消息
     * @param {object} messageData - { receiverId, messageType, content }
     * @returns {boolean}
     */
    sendPrivateMessage(messageData) {
        return this._sendTo('/app/chat/private', messageData)
    }

    /**
     * 发送群聊消息
     * @param {object} messageData - { roomId, messageType, content }
     * @returns {boolean}
     */
    sendRoomMessage(messageData) {
        return this._sendTo('/app/chat/room', messageData)
    }

    /**
     * 标记消息为已读
     * @param {Array<number>} messageIds
     * @returns {boolean}
     */
    markAsRead(messageIds) {
        return this._sendTo('/app/chat/read', messageIds)
    }

    /**
     * 撤回消息
     * @param {number} messageId
     * @returns {boolean}
     */
    recallMessage(messageId) {
        return this._sendTo('/app/chat/recall', { messageId })
    }

    /**
     * 内部发送方法
     * @param {string} destination
     * @param {object|Array} body
     * @returns {boolean}
     */
    _sendTo(destination, body) {
        if (!this.isConnected || !this._ws) {
            console.warn('WebSocket 未连接，无法发送消息')
            return false
        }

        const frame = encodeFrame('SEND', {
            destination: destination,
            'content-type': 'application/json',
        }, JSON.stringify(body))
        this._ws.send(frame)
        return true
    }

    // ──────────────── 错误处理 & 重连 ────────────────

    /**
     * 注册错误处理器
     * @param {function} handler
     */
    onError(handler) {
        this.errorHandlers.push(handler)
    }

    /**
     * 处理重连逻辑（指数退避）
     */
    _handleReconnect() {
        if (this._closing) return

        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('达到最大重连次数，停止重连')
            return
        }

        if (!this._token || !this._userId) return

        this.reconnectAttempts++
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1)

        console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})，延迟 ${delay}ms`)

        setTimeout(() => {
            this.isConnected = false
            this.connect(this._token, this._userId).catch(() => {
                this._handleReconnect()
            })
        }, delay)
    }

    /**
     * 获取连接状态
     * @returns {boolean}
     */
    getConnectionStatus() {
        return this.isConnected
    }
}

// 创建单例实例
const chatWebSocket = new ChatWebSocketClient()

export default chatWebSocket
