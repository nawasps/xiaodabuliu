import request from '@/utils/request'

export function getBookList(params) {
  return request.get('/book/list', { params })
}

export function getBookById(id) {
  return request.get(`/book/${id}`)
}

export function getHomeData() {
  return request.get('/book/home')
}

export function getFeedBooks(params) {
  return request.get('/book/feed', { params })
}

export function publishBook(data) {
  return request.post('/book', data)
}

export function updateBook(id, data) {
  return request.put(`/book/${id}`, data)
}

export function deleteBook(id) {
  return request.delete(`/book/${id}`)
}

export function updateBookStatus(id, status) {
  return request.put(`/book/${id}/status`, null, { params: { status } })
}

export function getMyBooks(params) {
  return request.get('/book/my', { params })
}