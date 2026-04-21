import request from '@/utils/request'

export function getCartItems() {
  return request.get('/cart')
}

export function addToCart(data) {
  return request.post('/cart', data)
}

export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/${id}`, null, { params: { quantity } })
}

export function removeFromCart(id) {
  return request.delete(`/cart/${id}`)
}

export function clearCart() {
  return request.delete('/cart')
}