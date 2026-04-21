import { defineStore } from 'pinia'
import { getCartItems, addToCart, removeFromCart, clearCart } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),
  getters: {
    totalAmount: state => {
      return state.items.reduce((sum, item) => sum + item.price * 1, 0)
    },
    itemCount: state => state.items.length
  },
  actions: {
    async fetchCartItems() {
      this.loading = true
      try {
        const res = await getCartItems()
        this.items = res.data || []
      } finally {
        this.loading = false
      }
    },
    async addItem(bookId, quantity = 1) {
      await addToCart({ bookId, quantity })
      await this.fetchCartItems()
    },
    async removeItem(id) {
      await removeFromCart(id)
      this.items = this.items.filter(item => item.id !== id)
    },
    async clearAll() {
      await clearCart()
      this.items = []
    }
  }
})
