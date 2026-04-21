<template>
  <div class="order-confirm-page">
    <div class="page-container">
      <h2>确认订单</h2>
      <div class="order-form">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="receiverPhone">
            <el-input v-model="form.receiverPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input v-model="form.receiverAddress" type="textarea" placeholder="请输入详细地址" />
          </el-form-item>
        </el-form>
      </div>
      <div class="order-items mt-20">
        <h3>商品清单</h3>
        <div v-for="item in items" :key="item.id" class="order-item">
          <el-image :src="item.images?.[0] || '/placeholder.png'" fit="cover" />
          <div class="item-info">
            <h4>{{ item.title }}</h4>
            <p class="price">¥{{ item.price }}</p>
          </div>
        </div>
      </div>
      <div class="order-footer mt-20">
        <div class="total">
          <span>共 {{ items.length }} 件商品，总计:</span>
          <span class="amount">¥{{ totalAmount }}</span>
        </div>
        <el-button type="primary" size="large" @click="handleSubmit" :loading="loading">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'
import { getBookById } from '@/api/book'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const formRef = ref()
const loading = ref(false)
const form = ref({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: ''
})

const rules = {
 receiverName: [
    { required: true, message: '请输入收货人', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const items = ref([])

onMounted(async () => {
  const bookIds = route.query.bookIds?.split(',') || []
  const cartItemIds = route.query.cartIds?.split(',') || []

  if (bookIds.length > 0) {
    for (const id of bookIds) {
      try {
        const res = await getBookById(id)
        items.value.push(res.data)
      } catch (error) {
        console.error('Failed to load book:', error)
      }
    }
  }
})

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + item.price, 0).toFixed(2)
})

const handleSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await createOrder({
      bookIds: items.value.map(item => item.id),
      ...form.value
    })
    await cartStore.fetchCartItems()
    ElMessage.success('订单创建成功')
    router.push(`/order/pay/${res.data.id}`)
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.order-confirm-page {
  padding: 20px 0;
}

.order-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.order-items {
  background: #fff;
  padding: 20px;
  border-radius: 8px;

  h3 {
    margin-bottom: 15px;
  }
}

.order-item {
  display: flex;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;

  .el-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
  }

  .item-info {
    flex: 1;

    h4 {
      margin-bottom: 5px;
    }

    .price {
      color: #ff4d4f;
    }
  }
}

.order-footer {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;

  .amount {
    font-size: 24px;
    color: #ff4d4f;
    font-weight: 600;
  }
}
</style>
