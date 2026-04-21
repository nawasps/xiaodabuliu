import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

let client = null

export function connectChatSocket(onConnected, onError) {
  if (client && client.connected) {
    if (onConnected) {
      onConnected(client)
    }
    return client
  }

  const token = localStorage.getItem('token')
  client = new Client({
    webSocketFactory: () => new SockJS('/api/ws'),
    reconnectDelay: 5000,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    onConnect: () => {
      if (onConnected) {
        onConnected(client)
      }
    },
    onStompError: frame => {
      if (onError) {
        onError(new Error(frame.headers.message || 'WebSocket错误'))
      }
    },
    onWebSocketError: error => {
      if (onError) {
        onError(error)
      }
    }
  })

  client.activate()
  return client
}

export function disconnectChatSocket() {
  if (client) {
    client.deactivate()
    client = null
  }
}

export function getChatSocketClient() {
  return client
}
