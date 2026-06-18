# 在线交流功能使用指南

## 📋 功能概述

在线交流功能支持学生之间的实时聊天，包括：
- **单聊**：一对一私密聊天
- **群聊**：多人聊天室/频道
- **离线消息**：未读消息统计和已读状态
- **历史消息**：分页加载聊天记录

## ️ 技术架构

### 后端技术
- **WebSocket + STOMP**：实现实时双向通信
- **SockJS**：提供 WebSocket fallback（兼容不支持 WebSocket 的浏览器）
- **MySQL**：持久化存储聊天消息
- **Redis**：缓存未读数、减少数据库压力

### 前端技术
- **Vue 3**：响应式 UI
- **sockjs-client + stompjs**：WebSocket 客户端
- **Element Plus**：UI 组件库

##  性能优化策略

### 1. 并发处理
- **WebSocket 长连接**：避免频繁 HTTP 请求
- **心跳机制**：保持连接活跃，自动检测断线
- **指数退避重连**：断线后智能重连，避免服务器压力

### 2. 数据库优化
- **分页查询**：每次只加载 20 条消息，避免一次性加载大量数据
- **索引优化**：
  - `idx_sender_receiver`：加速单聊查询
  - `idx_room`：加速群聊查询
  - `idx_unread`：加速未读消息统计
- **批量更新**：标记已读时使用批量 UPDATE

### 3. Redis 缓存
- **未读数缓存**：使用 Redis 原子操作 `INCR` 统计未读数
- **缓存过期**：5 分钟过期，平衡实时性和性能
- **清除策略**：发送新消息时清除相关缓存

### 4. 消息推送优化
- **定向推送**：单聊只推送给接收者，不广播
- **房间隔离**：群聊只推送给房间内成员
- **异步处理**：消息入库和推送异步执行

## 🚀 部署步骤

### 1. 数据库初始化

执行 SQL 脚本创建表结构：

```bash
mysql -u root -p miaomiaoc < deploy/sql/chat_module.sql
```

这将创建 3 张表：
- `chat_message`：聊天消息表
- `chat_room`：聊天房间表
- `chat_room_member`：房间成员关系表

### 2. 安装前端依赖

```bash
cd frontend
npm install
```

新增依赖：
- `sockjs-client@^1.6.1`
- `stompjs@^2.3.3`

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端会自动创建数据库表（JPA `ddl-auto: update`）。

### 4. 启动前端

```bash
cd frontend
npm run dev
```

## 📱 使用方法

### 方式一：通过路由跳转

在 Vue 组件中跳转到聊天页面：

```javascript
import { useRouter } from 'vue-router'

const router = useRouter()

// 单聊
router.push({
  path: '/chat',
  query: {
    type: 'private',
    userId: 123,
    userName: '张三'
  }
})

// 群聊
router.push({
  path: '/chat',
  query: {
    type: 'room',
    roomId: 456,
    roomName: '学习交流区'
  }
})
```

### 方式二：直接使用 WebSocket API

#### 1. 连接 WebSocket

```javascript
import chatWebSocket from '@/utils/chat-websocket'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 连接（需要 JWT Token）
await chatWebSocket.connect(userStore.token)
```

#### 2. 订阅消息

```javascript
// 订阅单聊消息
chatWebSocket.subscribePrivateMessages(currentUserId, (message) => {
  console.log('收到消息:', message)
  // 更新 UI
})

// 订阅群聊消息
chatWebSocket.subscribeRoomMessages(roomId, (message) => {
  console.log('收到群消息:', message)
  // 更新 UI
})
```

#### 3. 发送消息

```javascript
// 发送单聊消息
chatWebSocket.sendPrivateMessage({
  receiverId: 123,
  messageType: 'TEXT',
  content: '你好！'
})

// 发送群聊消息
chatWebSocket.sendRoomMessage({
  roomId: 456,
  messageType: 'TEXT',
  content: '大家好！'
})
```

#### 4. 标记已读

```javascript
// 通过 WebSocket 标记
chatWebSocket.markAsRead([1, 2, 3])

// 或通过 HTTP API
import { markAsRead } from '@/api/chat'
await markAsRead([1, 2, 3])
```

#### 5. 断开连接

```javascript
chatWebSocket.disconnect()
```

### 方式三：使用 HTTP API 查询历史消息

```javascript
import { 
  getPrivateMessages, 
  getRoomMessages,
  getUnreadCount,
  getRecentContacts
} from '@/api/chat'

// 获取单聊历史
const response = await getPrivateMessages(userId2, page, size)
console.log(response.data.content) // 消息列表

// 获取群聊历史
const response = await getRoomMessages(roomId, page, size)

// 获取未读数
const count = await getUnreadCount()

// 获取最近联系人
const contacts = await getRecentContacts()
```

## 🔧 API 接口文档

### WebSocket 端点

**连接地址**：`ws://localhost:8080/ws-chat?token={JWT_TOKEN}`

#### 发送消息

| 目的地 | 说明 | 参数 |
|--------|------|------|
| `/app/chat/private` | 发送单聊消息 | `{ receiverId, messageType, content }` |
| `/app/chat/room` | 发送群聊消息 | `{ roomId, messageType, content }` |
| `/app/chat/read` | 标记消息已读 | `[messageId1, messageId2, ...]` |

#### 订阅消息

| 目的地 | 说明 |
|--------|------|
| `/user/{userId}/queue/messages` | 订阅单聊消息 |
| `/topic/room/{roomId}` | 订阅群聊消息 |
| `/user/queue/errors` | 订阅错误消息 |

### HTTP API

#### 获取单聊历史

```
GET /api/chat/private/{userId2}?page=0&size=20
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "senderId": 123,
        "senderName": "张三",
        "receiverId": 456,
        "messageType": "TEXT",
        "content": "你好",
        "isRead": false,
        "createTime": "2026-06-18T10:30:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  }
}
```

#### 获取群聊历史

```
GET /api/chat/room/{roomId}?page=0&size=20
```

#### 获取未读消息数

```
GET /api/chat/unread-count
```

**响应**：
```json
{
  "code": 200,
  "data": 5
}
```

#### 获取最近联系人

```
GET /api/chat/contacts
```

**响应**：
```json
{
  "code": 200,
  "data": [123, 456, 789]
}
```

#### 标记消息已读

```
POST /api/chat/read
Content-Type: application/json

[1, 2, 3]
```

#### 标记所有消息已读

```
POST /api/chat/read-all/{contactId}
```

## ️ 安全考虑

### 1. 身份验证
- WebSocket 握手时需要携带 JWT Token
- 通过 `WebSocketHandshakeInterceptor` 验证 Token 并提取用户ID
- 所有消息都关联发送者ID，防止伪造

### 2. 权限控制
- 单聊：只能与自己或好友聊天（可扩展好友系统）
- 群聊：只能向已加入的房间发送消息（需验证房间成员关系）

### 3. 内容过滤
- 建议添加敏感词过滤
- 限制消息长度（如 500 字符）
- 限制发送频率（防刷屏）

### 4. XSS 防护
- 前端渲染消息时使用 `textContent` 而非 `innerHTML`
- 后端存储前对内容进行转义

## 📊 监控与优化

### 1. 性能监控指标

- **WebSocket 连接数**：监控同时在线用户数
- **消息吞吐量**：每秒发送/接收消息数
- **数据库查询时间**：历史消息加载速度
- **Redis 命中率**：缓存有效性

### 2. 优化建议

#### 短期优化
- ✅ 已实现：分页加载、Redis 缓存、索引优化
- 待实现：消息压缩、批量写入

#### 中期优化
- 引入消息队列（RabbitMQ/Kafka）缓冲高并发消息
- 使用 Elasticsearch 存储历史消息，支持全文搜索
- 添加消息撤回功能

#### 长期优化
- 分布式 WebSocket 集群（使用 Redis Pub/Sub 同步消息）
- CDN 加速静态资源
- 数据库读写分离

## ❓ 常见问题

### Q1: WebSocket 连接失败怎么办？

**检查项**：
1. 后端服务是否正常运行
2. Token 是否正确传递
3. 防火墙是否允许 WebSocket 端口
4. 浏览器控制台是否有 CORS 错误

**解决**：
- 确保 `CorsConfig` 允许 WebSocket 源
- 检查 `WebSocketHandshakeInterceptor` 的 Token 解析逻辑

### Q2: 消息发送成功但对方没收到？

**可能原因**：
1. 对方未订阅对应的消息队列
2. WebSocket 连接已断开
3. 消息推送目的地错误

**排查**：
- 检查前端是否正确调用 `subscribePrivateMessages` 或 `subscribeRoomMessages`
- 查看浏览器控制台是否有 WebSocket 错误
- 后端日志查看消息是否成功推送

### Q3: 历史消息加载慢？

**优化方案**：
1. 减小每页大小（从 20 改为 10）
2. 添加数据库索引（已实现）
3. 使用 Redis 缓存热点消息
4. 前端虚拟滚动（只渲染可见区域的消息）

### Q4: 如何支持图片和文件发送？

**扩展步骤**：
1. 修改 `ChatMessage.messageType` 支持 `IMAGE`、`FILE`
2. 添加文件上传接口（返回文件 URL）
3. 消息内容存储文件 URL 而非二进制数据
4. 前端根据 `messageType` 渲染不同的内容（图片预览、文件下载链接）

### Q5: 如何实现离线消息？

**当前实现**：
- 消息已持久化到 MySQL
- 用户登录后通过 HTTP API 拉取未读消息

**改进方案**：
- 记录用户最后在线时间
- 登录时查询该时间之后的所有消息
- 推送未读消息通知

## 📝 后续开发计划

- [ ] 添加好友系统
- [ ] 支持图片、文件发送
- [ ] 消息撤回功能
- [ ] 消息搜索功能
- [ ] 表情包支持
- [ ] 语音消息
- [ ] 视频通话（WebRTC）
- [ ] 消息加密（端到端加密）

---

**最后更新**: 2026-06-18  
**维护者**: Yuhuang Sen
