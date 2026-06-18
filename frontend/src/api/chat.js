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
 * 获取最近联系人列表
 */
export function getRecentContacts() {
    return request({
        url: '/api/chat/contacts',
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
