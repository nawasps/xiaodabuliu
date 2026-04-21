import { defineStore } from 'pinia'
import { getUnreadCountByType } from '@/api/message'

export const useMessageStore = defineStore('message', {
  state: () => ({
    unreadCount: {
      SYSTEM: 0,
      ORDER: 0,
      PRIVATE: 0
    }
  }),
  actions: {
    async fetchUnreadCount() {
      try {
        const res = await getUnreadCountByType()
        if (res.data) {
          this.unreadCount = res.data
        }
      } catch {
        this.unreadCount = { SYSTEM: 0, ORDER: 0, PRIVATE: 0 }
      }
    }
  }
})