import request from '@/utils/request'

export function createOrder(data) {
  return request.post('/order', data)
}

export function getOrderList(params) {
  return request.get('/order/list', { params })
}

export function getOrderById(id) {
  return request.get('/order/' + id)
}

export function getSoldOrders() {
  return request.get('/order/sold')
}

export function payOrder(id) {
  return request.put('/order/' + id + '/pay')
}

export function createAlipayPayForm(id) {
  return request.get('/order/' + id + '/pay/alipay/form')
}

export function shipOrder(id, trackingNo) {
  return request.put('/order/' + id + '/ship', { trackingNo })
}

export function receiveOrder(id) {
  return request.put('/order/' + id + '/receive')
}

export function cancelOrder(id, reason) {
  return request.put(`/order/${id}/cancel`, { reason })
}

export function getSellerOrders() {
  return request.get('/order/seller')
}
export function createReview(orderId, data) {
  return request.post('/review/' + orderId, data)
}

export function getBookReviews(bookId, params) {
  return request.get('/review/book/' + bookId, { params })
}
