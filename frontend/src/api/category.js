import request from '@/utils/request'

export function getCategoryTree() {
  return request.get('/category/tree')
}

export function getParentCategories() {
  return request.get('/category/parents')
}

export function getChildCategories(id) {
  return request.get(`/category/${id}/children`)
}