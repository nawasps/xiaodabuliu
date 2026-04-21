import request from '@/utils/request'

export function getSearchHistory() {
  return request.get('/search/history')
}

export function addSearchHistory(keyword) {
  return request.post('/search/history', { keyword })
}

export function deleteSearchHistory(keyword) {
  return request.delete('/search/history', { data: { keyword } })
}

export function clearSearchHistory() {
  return request.delete('/search/history')
}

export function getHotSearch() {
  return request.get('/search/hot')
}

export function getSearchRecommendations(keyword) {
  return request.get('/search/recommend', { params: { keyword } })
}