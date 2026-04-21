<template>
  <div class="book-detail-page">
    <div class="page-container" v-if="book">
      <div class="book-header">
        <div class="book-images">
          <el-image :src="book.coverImage || book.images?.[0] || '/placeholder.png'" fit="contain" />
          <div class="image-list" v-if="book.images?.length > 1">
            <div v-for="(img, index) in book.images" :key="index" class="image-item">
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>
        <div class="book-info">
          <h1>{{ book.title }}</h1>
          <div class="seller-info">
            <el-avatar :src="book.sellerAvatar" />
            <span>{{ book.sellerNickname }}</span>
            <span class="credit">信用: {{ book.sellerCreditScore }}</span>
             <el-button size="small" @click="handleSendMessage">实时聊天</el-button>
          </div>
          <div class="price-section">
            <span class="current-price">
              <span class="price-symbol">¥</span>
              <span class="price-integer">{{ book.price }}</span>
            </span>
            <span class="original-price" v-if="book.originalPrice">
              原价: ¥{{ book.originalPrice }}
            </span>
          </div>
          <div class="book-meta">
            <span>分类: {{ book.categoryName }}</span>
            <span>新旧程度: {{ book.condition }}</span>
            <span>浏览: {{ book.viewCount }}</span>
            <span>收藏: {{ book.favoriteCount }}</span>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="handleBuy">立即购买</el-button>
            <el-button size="large" @click="handleAddToCart">加入购物车</el-button>
            <el-button :icon="book.isFavorite ? 'Star' : 'StarFilled'" @click="handleFavorite">
              {{ book.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button @click="handleReport">举报</el-button>
<el-button @click="handleFeedback">意见反馈</el-button>
          </div>
        </div>
      </div>
      <div class="book-content mt-20">
        <el-tabs>
          <el-tab-pane label="商品详情">
            <div class="description">{{ book.description }}</div>
          </el-tab-pane>
          <el-tab-pane label="评价">
            <div class="reviews">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <span>{{ review.reviewerNickname }}</span>
                  <el-rate v-model="review.rating" disabled />
                </div>
                <p>{{ review.content }}</p>
              </div>
              <el-empty v-if="reviews.length === 0" description="暂无评价" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookById } from '@/api/book'
import { addToCart } from '@/api/cart'
import { getBookReviews } from '@/api/review'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const book = ref(null)
const reviews = ref([])
const favoriteLoading = ref(false)

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getBookById(id)
    book.value = res.data
    const reviewRes = await getBookReviews(id)
    reviews.value = reviewRes.data?.records || []
  } catch (error) {
    console.error('Failed to load book:', error)
  }
})

const handleBuy = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push({ path: '/order/confirm', query: { bookIds: book.value.id } })
}

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await addToCart({ bookId: book.value.id, quantity: 1 })
    ElMessage.success('已加入购物车')
    cartStore.fetchCartItems()
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (favoriteLoading.value) {
    return
  }
  favoriteLoading.value = true
  try {
    if (book.value.isFavorite) {
      await request.delete(`/favorite/${book.value.id}`)
      book.value.isFavorite = false
      ElMessage.success('已取消收藏')
    } else {
      await request.post(`/favorite/${book.value.id}`)
      book.value.isFavorite = true
      ElMessage.success('已收藏')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const handleReport = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const { value: reason } = await ElMessageBox.prompt('请输入举报原因', '举报书籍', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  if (!reason) {
    ElMessage.warning('请输入举报原因')
    return
  }
  try {
    await request.post('/report', {
      type: 'REPORT',
      reportedUserId: book.value.userId,
      bookId: book.value.id,
      reason: reason
    })
    ElMessage.success('举报成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '举报失败')
  }
}

const handleFeedback = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const { value: content } = await ElMessageBox.prompt('请输入您的意见或建议', '意见反馈', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  if (!content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  try {
    await request.post('/report', {
      type: 'FEEDBACK',
      reason: 'FEEDBACK',
      description: content
    })
    ElMessage.success('反馈成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '反馈失败')
  }
}

const handleSendMessage = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push({ path: `/chat/${book.value.userId}`, query: { name: book.value.sellerNickname } })
}
</script>

<style scoped lang="scss">
.book-detail-page {
  padding: 20px 0;
}

.book-header {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}

.book-images {
  width: 400px;

  .el-image {
    width: 100%;
    height: 400px;
    border-radius: 8px;
  }

  .image-list {
    display: flex;
    gap: 10px;
    margin-top: 10px;
  }

  .image-item {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    overflow: hidden;

    .el-image {
      width: 100%;
      height: 100%;
    }
  }
}

.book-info {
  flex: 1;

  h1 {
    font-size: 24px;
    margin-bottom: 20px;
  }
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;

  .credit {
    color: #f56c6c;
  }
}

.price-section {
  margin-bottom: 20px;

  .current-price {
    color: #ff4d4f;
    font-size: 28px;
    font-weight: 600;
  }

  .original-price {
    margin-left: 20px;
    color: #999;
    text-decoration: line-through;
  }
}

.book-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.book-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.description {
  line-height: 1.8;
  white-space: pre-wrap;
}

.review-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;

  .review-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }
}
</style>
