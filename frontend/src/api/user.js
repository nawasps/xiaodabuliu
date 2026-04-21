import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function logout() {
  return request.post('/user/logout')
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function sendVerifyCode(phone) {
  return request.post('/user/sendCode', { phone })
}

export function resetPassword(data) {
  return request.post('/user/resetPassword', data)
}

export function updatePhone(data) {
  return request.put('/user/phone', data)
}