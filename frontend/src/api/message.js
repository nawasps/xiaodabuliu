import request from '@/utils/request'

export function getMessageList(params) {
  return request.get('/message/list', { params })
}

export function getUnreadCount() {
  return request.get('/message/unread')
}

export function markAsRead(id) {
  return request.put(`/message/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/message/readAll')
}

export function sendPrivateMessage(data) {
  return request.post('/message/private', data)
}

export function getPrivateMessages(userId, params) {
  return request.get(`/message/private/${userId}`, { params })
}
export function getUnreadCountByType() {
  return request.get('/message/unreadByType')
}

export function markAsReadByType(type) {
  return request.put(`/message/readByType/${type}`)
}