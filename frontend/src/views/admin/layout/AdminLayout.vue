<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon><Setting /></el-icon>
        <span>后台管理</span>
      </div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/admin">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/book">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/review">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/report">
          <el-badge :value="pendingReports" :hidden="pendingReports === 0" type="danger">
            <el-icon><Warning /></el-icon>
            <span>举报投诉</span>
          </el-badge>
        </el-menu-item>
        <el-menu-item index="/admin/notice">
          <el-icon><Bell /></el-icon>
          <span>系统公告</span>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tag">
          <el-icon><Collection /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-divider />
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="main-wrapper">
      <header class="topbar">
        <h3>{{ pageTitle }}</h3>
        <div class="user-info">
          <el-badge :value="pendingReports" :hidden="pendingReports === 0" type="danger" class="report-badge">
            <el-button size="small" @click="$router.push('/admin/report')">举报 {{ pendingReports }}</el-button>
          </el-badge>
          <el-button size="small" @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pendingReports = ref(0)

const loadPendingReports = async () => {
  try {
    const res = await request.get('/admin/dashboard/stats')
    pendingReports.value = res.data?.pendingReports || 0
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

onMounted(() => {
  loadPendingReports()
  window.addEventListener('refresh-admin-stats', loadPendingReports)
})

const pageTitle = computed(() => {
  const map = {
    '/admin': '仪表盘',
    '/admin/user': '用户管理',
    '/admin/book': '商品管理',
    '/admin/order': '订单管理',
    '/admin/review': '评价管理',
    '/admin/report': '举报投诉',
    '/admin/notice': '系统公告',
    '/admin/category': '分类管理',
    '/admin/tag': '标签管理'
  }
  return map[route.path] || '管理后台'
})

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出')
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #304156;
  color: #fff;

  .logo {
    padding: 20px;
    font-size: 18px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .el-menu {
    border: none;
    background: transparent;

    .el-menu-item {
      color: #bfcbd9;

      &:hover, &.is-active {
        background: #263445;
        color: #409eff;
      }
    }
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    font-weight: 500;
  }
}

.main-content {
  flex: 1;
  padding: 20px;
  background: #f5f5f5 url('/backgrounds/admin-bg.svg') no-repeat center top;
  background-size: cover;
}

.report-badge {
  margin-right: 10px;
}
</style>
