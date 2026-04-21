<template>
  <div class="order-pay-page">
    <div class="page-container">
      <div class="pay-card" v-if="order">
        <h2>订单支付</h2>
        <div class="order-info">
          <p>订单号: {{ order.orderNo }}</p>
          <p>订单金额: <span class="amount">¥{{ order.totalAmount }}</span></p>
        </div>
        <div class="pay-methods">
          <h3>选择支付方式</h3>
          <el-radio-group v-model="payMethod">
            <el-radio label="wechat">
              <span>微信支付</span>
            </el-radio>
            <el-radio label="alipay">
              <span>支付宝</span>
            </el-radio>
            <el-radio label="balance">
              <span>余额支付</span>
            </el-radio>
          </el-radio-group>
        </div>
        <div class="pay-actions">
          <el-button size="large" @click="$router.push('/order/list')">返回</el-button>
          <el-button type="primary" size="large" @click="handlePay" :loading="loading">确认支付</el-button>
        </div>
        <div class="mock-notice">
          <el-icon><Warning /></el-icon>
          <span>这是模拟支付环境，无需真实付款</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderById, payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const payMethod = ref('wechat')
const loading = ref(false)

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getOrderById(id)
    order.value = res.data
  } catch (error) {
    console.error('Failed to load order:', error)
  }
})

const handlePay = async () => {
  loading.value = true
  try {
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    router.push('/order/list')
  } catch (error) {
    ElMessage.error(error.message || '支付失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.order-pay-page {
  padding: 40px 0;
}

.pay-card {
  max-width: 500px;
  margin: 0 auto;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  text-align: center;

  h2 {
    margin-bottom: 30px;
  }
}

.order-info {
  margin-bottom: 30px;

  p {
    margin: 10px 0;
  }

  .amount {
    font-size: 24px;
    color: #ff4d4f;
    font-weight: 600;
  }
}

.pay-methods {
  margin-bottom: 30px;

  h3 {
    margin-bottom: 15px;
  }

  .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
}

.pay-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.mock-notice {
  margin-top: 20px;
  padding: 10px;
  background: #fff7e6;
  color: #fa8c16;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>
