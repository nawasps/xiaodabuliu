<template>
  <div class="favorites-page">
    <div class="page-container">
      <h2>我的收藏</h2>
      <div class="book-grid" v-if="books.length > 0">
        <div v-for="book in books" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
          <div class="book-cover">
            <el-image :src="book.coverImage || book.images?.[0] || '/placeholder.png'" fit="cover" />
          </div>
          <div class="book-info">
            <h3 class="text-ellipsis">{{ book.title }}</h3>
            <p class="price">¥{{ book.price }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无收藏" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const books = ref([])

const loadFavorites = async () => {
  try {
    const res = await request.get('/favorite')
    books.value = res.data || []
  } catch (error) {
    console.error('Failed to load favorites:', error)
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped lang="scss">
.favorites-page {
  padding: 20px 0;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.book-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.book-cover {
  height: 180px;
  background: #f5f5f5;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.book-info {
  padding: 12px;

  h3 {
    font-size: 14px;
    margin-bottom: 8px;
  }

  .price {
    color: #ff4d4f;
    font-weight: 600;
  }
}
</style>
