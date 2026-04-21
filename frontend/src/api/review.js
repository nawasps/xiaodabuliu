import request from '@/utils/request'

export function getBookReviews(bookId, params) {
  return request.get('/review/book/' + bookId, { params })
}

export function createReview(orderId, data) {
  return request.post('/review/' + orderId, data)
}

export function getUserReviews(userId, params) {
  return request.get('/review/user/' + userId, { params })
}