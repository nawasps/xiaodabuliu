<template>
  <div class="front-layout">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon><Reading /></el-icon>
          <span>校园二手书</span>
        </div>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索书籍..."
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
        <div class="nav-actions">
          <el-button :icon="ShoppingCart" @click="$router.push('/cart')">
            购物车
            <el-badge v-if="cartStore.itemCount > 0" :value="cartStore.itemCount" />
          </el-button>
          <template v-if="userStore.isLoggedIn">
           <el-badge :value="totalUnread" :max="99" :hidden="totalUnread === 0">
  <el-button :icon="Message" @click="$router.push('/message')">消息</el-button>
</el-badge>

            <el-dropdown @command="handleUserCommand">
              <el-button :icon="User">
                {{ userStore.userInfo?.nickname || '用户' }}
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
  <el-dropdown-item command="my-books">我的发布</el-dropdown-item>
  <el-dropdown-item command="my-orders">我的订单</el-dropdown-item>
  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
  <el-dropdown-item command="my-reports">我的举报</el-dropdown-item>
  <el-dropdown-item command="message">消息中心</el-dropdown-item>
  <el-dropdown-item v-if="userStore.isAdmin" divided command="admin">管理后台</el-dropdown-item>
  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
</el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <div class="footer-content">
        <p>校园二手书交易平台 - 让闲置书籍流转起来</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ShoppingCart, User, Reading, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useMessageStore } from '@/stores/message'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const messageStore = useMessageStore()
const keyword = ref('')

if (userStore.isLoggedIn) {
  userStore.fetchUserInfo()
  cartStore.fetchCartItems()
  messageStore.fetchUnreadCount()
}
const totalUnread = computed(() => {
  const count = messageStore.unreadCount
  return (count.SYSTEM || 0) + (count.ORDER || 0) + (count.PRIVATE || 0)
})
const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'my-books':
      router.push('/user/my-books')
      break
    case 'my-orders':
      router.push('/order/list')
      break
    case 'favorites':
  router.push('/user/favorites')
  break
case 'my-reports':
  router.push('/user/reports')
  break
case 'message':
  router.push('/message')
  break
case 'admin':
  router.push('/admin')
  break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}
</script>

<style scoped lang="scss">
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
}

.search-bar {
  flex: 1;
  max-width: 500px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-content {
  flex: 1;
}

.footer {
  background: #f5f5f5;
  padding: 20px;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #999;
}
</style>