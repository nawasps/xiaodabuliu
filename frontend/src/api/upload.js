import request from '@/utils/request'

export function uploadBookImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/book-image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
