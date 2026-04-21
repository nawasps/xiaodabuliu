完整修正后的 OrderList.vue
请用以下完整代码替换整个文件：

<template>
  <div class="order-list-page">
    <div class="page-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我买到的" name="bought" />
        <el-tab-pane label="我卖出的" name="sold" />
      </el-tabs>

      <div class="order-list" v-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span>订单号: {{ order.orderNo }}</span>
            <span class="status" :class="order.status">{{ getStatusText(order.status) }}</span>
          </div>
          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item">
              <el-image :src="item.bookImage || '/placeholder.png'" fit="cover" />
              <span>{{ item.bookTitle }}</span>
              <span class="price">¥{{ item.price }}</span>
            </div>
          </div>
          <div class="order-footer">
            <span>总价: ¥{{ order.totalAmount }}</span>
            <div class="actions">
              <template v-if="activeTab === 'bought'">
                <el-button v-if="order.status === 'PENDING_PAY'" size="small" type="primary" @click="goToPay(order.id)">去支付</el-button>
                <el-button v-if="order.status === 'PENDING_PAY'" size="small" @click="handleCancel(order.id)">取消</el-button>
                <el-button v-if="order.status === 'PAID'" size="small" disabled>等待卖家发货</el-button>
                <el-button v-if="order.status === 'SHIPPED'" size="small" type="primary" @click="handleReceive(order.id)">确认收货</el-button>
                <template v-if="order.status === 'COMPLETED'">
                  <el-button size="small" type="warning" @click="handleReview(order)">评价</el-button>
                  <el-button size="small" @click="$router.push(`/book/${order.items?.[0]?.bookId}`)">查看书籍</el-button>
                </template>
                <template v-if="order.status === 'CANCELLED'">
                  <el-tag type="info" size="small">已取消</el-tag>
                  <span v-if="order.cancelReason" class="cancel-reason">{{ order.cancelReason }}</span>
                </template>
              </template>

              <template v-if="activeTab === 'sold'">
                <el-button v-if="order.status === 'PENDING_PAY'" size="small" disabled>等待买家付款</el-button>
                <el-button v-if="order.status === 'PAID'" size="small" type="primary" @click="handleShip(order)">发货</el-button>
                <el-button v-if="order.status === 'SHIPPED'" size="small" disabled>等待买家确认</el-button>
                <el-button v-if="order.status === 'COMPLETED'" size="small" disabled>已完成</el-button>
                <el-button v-if="order.status === 'CANCELLED'" size="small" disabled>已取消</el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无订单" />
    </div>

  <el-dialog v-model="shipDialogVisible" title="发货" width="400px" append-to-body>
      <el-form>
        <el-form-item label="快递单号">
          <el-input v-model="trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="评价" width="500px" append-to-body>
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, cancelOrder, receiveOrder, shipOrder, createReview, getSellerOrders } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('bought')
const orders = ref([])
const shipDialogVisible = ref(false)
const currentOrder = ref(null)
const trackingNo = ref('')
const reviewDialogVisible = ref(false)
const reviewFormRef = ref()
const reviewForm = reactive({
  orderId: null,
  bookId: null,
  rating: 5,
  content: ''
})
const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const map = {
    'PENDING_PAY': '待付款',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const goToPay = (id) => {
  router.push('/order/pay/' + id)
}

const loadOrders = async () => {
  try {
    let res
    if (activeTab.value === 'bought') {
      res = await getOrderList({})
      orders.value = res.data?.records || []
    } else {
      res = await getSellerOrders()
      orders.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load orders:', error)
  }
}
const handleTabChange = () => {
  loadOrders()
}

const handleCancel = async (id) => {
  await ElMessageBox.confirm('确定取消该订单？', '提示')
  await cancelOrder(id, '用户取消')
  ElMessage.success('已取消')
  loadOrders()
}

const handleShip = (order) => {
  currentOrder.value = order
  trackingNo.value = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  if (!trackingNo.value) {
    ElMessage.warning('请输入快递单号')
    return
  }
  await shipOrder(currentOrder.value.id, trackingNo.value)
  ElMessage.success('已发货')
  shipDialogVisible.value = false
  loadOrders()
}

const handleReceive = async (id) => {
  await ElMessageBox.confirm('确认收货？', '提示')
  await receiveOrder(id)
  ElMessage.success('已确认收货')
  loadOrders()
}

const handleReview = (order) => {
  reviewForm.orderId = order.id
  reviewForm.bookId = order.items?.[0]?.bookId
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  await reviewFormRef.value?.validate()
  try {
    await createReview(reviewForm.orderId, {
      rating: reviewForm.rating,
      content: reviewForm.content,
      bookId: reviewForm.bookId
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '评价失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.order-list-page {
  padding: 20px 0;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.order-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}
.order-items .order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}
.order-items .el-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}
.order-items .price {
  margin-left: auto;
  color: #ff4d4f;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}
.order-footer .price {
  font-size: 18px;
  color: #ff4d4f;
}
.cancel-reason {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}
</style>