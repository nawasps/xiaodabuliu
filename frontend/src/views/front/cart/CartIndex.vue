<template>
  <div class="cart-page">
    <div class="page-container">
      <h2>购物车</h2>
      <div class="cart-content" v-if="cartStore.items.length > 0">
        <div class="cart-list">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
            <el-checkbox v-model="item.selected" />
            <div class="item-image">
              <el-image :src="item.images?.[0] || '/placeholder.png'" fit="cover" />
            </div>
            <div class="item-info">
              <h3 @click="$router.push(`/book/${item.id}`)">{{ item.title }}</h3>
              <p class="price">¥{{ item.price }}</p>
            </div>
            <div class="item-actions">
              <el-button size="small" @click="handleRemove(item.id)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="cart-summary">
          <p>共 {{ cartStore.items.length }} 件商品</p>
          <p class="total">总计: ¥{{ cartStore.totalAmount }}</p>
          <el-button type="primary" size="large" @click="handleCheckout">结算</el-button>
        </div>
      </div>
      <el-empty v-else description="购物车是空的">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const selectAll = ref(false)

const handleSelectAll = (val) => {
  cartStore.items.forEach(item => {
    item.selected = val
  })
}

const handleRemove = async (id) => {
  await cartStore.removeItem(id)
  ElMessage.success('已删除')
}

const handleCheckout = () => {
  const selectedIds = cartStore.items.filter(item => item.selected).map(item => item.id)
  if (selectedIds.length === 0) {
    ElMessage.warning('请选择商品')
    return
  }
  router.push({ path: '/order/confirm', query: { bookIds: selectedIds.join(',') } })
}
</script>

<style scoped lang="scss">
.cart-page {
  padding: 20px 0;

  h2 {
    margin-bottom: 20px;
  }
}

.cart-content {
  display: flex;
  gap: 20px;
}

.cart-list {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;

  .item-image {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    overflow: hidden;

    .el-image {
      width: 100%;
      height: 100%;
    }
  }

  .item-info {
    flex: 1;

    h3 {
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }

    .price {
      color: #ff4d4f;
      margin-top: 5px;
    }
  }
}

.cart-summary {
  width: 300px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: right;

  .total {
    font-size: 24px;
    color: #ff4d4f;
    margin: 15px 0;
  }
}
</style>
