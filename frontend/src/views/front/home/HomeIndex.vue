<template>
  <div class="home-page">
    <div class="page-container">
      <el-carousel height="300px" :interval="5000">
        <el-carousel-item>
          <div class="banner banner-1">
            <h2>让闲置书籍流转起来</h2>
            <p>校园二手书交易平台，安全便捷</p>
          </div>
        </el-carousel-item>
        <el-carousel-item>
          <div class="banner banner-2">
            <h2>海量书籍，等你来选</h2>
            <p>教材、教辅、文学、科技，应有尽有</p>
          </div>
        </el-carousel-item>
      </el-carousel>

      <section class="section mt-20">
        <div class="section-header flex-between">
          <h2>热门推荐</h2>
          <el-button text @click="$router.push('/search?sort=hot')">查看更多</el-button>
        </div>
        <div class="book-grid">
          <div v-for="book in hotBooks" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="book-cover">
              <el-image :src="book.images?.[0] || '/placeholder.png'" fit="cover" />
            </div>
            <div class="book-info">
              <h3 class="book-title text-ellipsis">{{ book.title }}</h3>
              <p class="book-condition">{{ book.condition }}</p>
              <p class="price">
                <span class="price-symbol">¥</span>
                <span class="price-integer">{{ book.price }}</span>
              </p>
            </div>
          </div>
        </div>
      </section>

      <section class="section mt-20">
        <div class="section-header flex-between">
          <h2>最新上架</h2>
          <el-button text @click="$router.push('/search?sort=latest')">查看更多</el-button>
        </div>
        <div class="book-grid">
          <div v-for="book in latestBooks" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="book-cover">
              <el-image :src="book.images?.[0] || '/placeholder.png'" fit="cover" />
            </div>
            <div class="book-info">
              <h3 class="book-title text-ellipsis">{{ book.title }}</h3>
              <p class="book-condition">{{ book.condition }}</p>
              <p class="price">
                <span class="price-symbol">¥</span>
                <span class="price-integer">{{ book.price }}</span>
              </p>
            </div>
          </div>
        </div>
      </section>

      <section class="section mt-20">
        <div class="section-header flex-between">
          <h2>Feed 流推荐</h2>
        </div>
        <div class="book-grid">
          <div v-for="book in feedBooks" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="book-cover">
              <el-image :src="book.images?.[0] || '/placeholder.png'" fit="cover" />
            </div>
            <div class="book-info">
              <h3 class="book-title text-ellipsis">{{ book.title }}</h3>
              <p class="book-condition">{{ book.condition }}</p>
              <p class="price">
                <span class="price-symbol">¥</span>
                <span class="price-integer">{{ book.price }}</span>
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeData } from '@/api/book'

const hotBooks = ref([])
const latestBooks = ref([])
const feedBooks = ref([])

onMounted(async () => {
  try {
    const res = await getHomeData()
    hotBooks.value = res.data.hotBooks || []
    latestBooks.value = res.data.latestBooks || []
    feedBooks.value = res.data.feedBooks || []
  } catch (error) {
    console.error('Failed to load home data:', error)
  }
})
</script>

<style scoped lang="scss">
.home-page {
  padding: 20px 0;
}

.banner {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  text-align: center;

  h2 {
    font-size: 32px;
    margin-bottom: 10px;
  }

  p {
    font-size: 18px;
  }
}

.banner-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.banner-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.section-header {
  margin-bottom: 20px;

  h2 {
    font-size: 20px;
    font-weight: 600;
  }
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }
}

.book-cover {
  width: 100%;
  height: 180px;
  background: #f5f5f5;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.book-info {
  padding: 12px;
}

.book-title {
  font-size: 14px;
  margin-bottom: 4px;
}

.book-condition {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
</style>
