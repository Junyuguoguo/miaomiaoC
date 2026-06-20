import request from '@/utils/request'

/**
 * 获取单聊历史消息
 */
export function getPrivateMessages(userId2, page = 0, size = 20) {
    return request({
        url: `/api/chat/private/${userId2}`,
        method: 'get',
        params: { page, size }
    })
}

/**
 * 获取群聊历史消息
 */
export function getRoomMessages(roomId, page = 0, size = 20) {
    return request({
        url: `/api/chat/room/${roomId}`,
        method: 'get',
        params: { page, size }
    })
}

/**
 * 获取最近联系人列表（仅ID）
 */
export function getRecentContacts() {
    return request({
        url: '/api/chat/contacts',
        method: 'get'
    })
}

/**
 * 获取联系人详细信息（含用户名、头像、未读数）
 */
export function getContactDetails() {
    return request({
        url: '/api/chat/contacts/detail',
        method: 'get'
    })
}

/**
 * 获取未读消息数
 */
export function getUnreadCount() {
    return request({
        url: '/api/chat/unread-count',
        method: 'get'
    })
}

/**
 * 标记消息为已读
 */
export function markAsRead(messageIds) {
    return request({
        url: '/api/chat/read',
        method: 'post',
        data: messageIds
    })
}

/**
 * 标记某个联系人的所有消息为已读
 */
export function markAllAsRead(contactId) {
    return request({
        url: `/api/chat/read-all/${contactId}`,
        method: 'post'
    })
}

/**
 * 获取公开聊天室列表
 */
export function getPublicRooms() {
    return request({
        url: '/api/chat/rooms',
        method: 'get'
    })
}

/**
 * 获取我加入的房间列表
 */
export function getMyRooms() {
    return request({
        url: '/api/chat/rooms/my',
        method: 'get'
    })
}

/**
 * 加入聊天室
 */
export function joinRoom(roomId) {
    return request({
        url: `/api/chat/rooms/${roomId}/join`,
        method: 'post'
    })
}

/**
 * 退出聊天室
 */
export function leaveRoom(roomId) {
    return request({
        url: `/api/chat/rooms/${roomId}/leave`,
        method: 'post'
    })
}

/**
 * 搜索用户
 */
export function searchUsers(keyword) {
    return request({
        url: '/api/chat/users/search',
        method: 'get',
        params: { keyword }
    })
}

/**
 * 获取聊天室列表
 */
export function getRooms() {
    return request({ url: '/api/chat/rooms', method: 'get' })
}

/**
 * 创建聊天室
 */
export function createRoom(data) {
    return request({ url: '/api/chat/rooms', method: 'post', data })
}

/**
 * 删除聊天室
 */
export function deleteRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}`, method: 'delete' })
}

export function updateRoom(roomId, data) {
    return request({ url: `/api/chat/rooms/${roomId}`, method: 'put', data })
}

/**
 * 通过群号加入群聊
 */
export function joinByGroupNumber(groupNumber) {
    return request({ url: `/api/chat/rooms/join/${groupNumber}`, method: 'get' })
}

/**
 * 切换房间置顶状态
 */
export function togglePinRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}/pin`, method: 'post' })
}

/**
 * 切换房间免打扰状态
 */
export function toggleMuteRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}/mute`, method: 'post' })
}

/**
 * 获取用户房间设置（置顶/免打扰）
 */
export function getRoomSettings() {
    return request({ url: '/api/chat/rooms/settings', method: 'get' })
}
