<template>
  <div class="order-detail-page">
    <div class="page-container" v-if="order">
      <el-card>
        <template #header>
          <div class="flex-between">
            <span>订单号: {{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
        </template>
        <div class="order-info">
          <h3>订单信息</h3>
          <p>收货人: {{ order.receiverName }}</p>
          <p>联系电话: {{ order.receiverPhone }}</p>
          <p>收货地址: {{ order.receiverAddress }}</p>
          <p v-if="order.trackingNo">快递单号: {{ order.trackingNo }}</p>
        </div>
        <div class="order-items">
          <h3>商品清单</h3>
          <div v-for="item in order.items" :key="item.id" class="item">
            <el-image :src="item.bookImage || '/placeholder.png'" fit="cover" />
            <span>{{ item.bookTitle }}</span>
            <span class="price">¥{{ item.price }}</span>
          </div>
        </div>
        <div class="order-footer">
          <span>总价: ¥{{ order.totalAmount }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderById } from '@/api/order'

const route = useRoute()
const order = ref(null)

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

const getStatusType = (status) => {
  const map = {
    'PENDING_PAY': 'warning',
    'PAID': '',
    'SHIPPED': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getOrderById(id)
    order.value = res.data
  } catch (error) {
    console.error('Failed to load order:', error)
  }
})
</script>

<style scoped lang="scss">
.order-detail-page {
  padding: 20px 0;
}

.order-info, .order-items {
  margin-bottom: 20px;

  h3 {
    margin-bottom: 15px;
  }

  p {
    margin: 8px 0;
    color: #666;
  }
}

.item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;

  .el-image {
    width: 50px;
    height: 50px;
    border-radius: 4px;
  }

  .price {
    margin-left: auto;
    color: #ff4d4f;
  }
}

.order-footer {
  text-align: right;
  font-size: 20px;
  color: #ff4d4f;
  font-weight: 600;
}
</style>
