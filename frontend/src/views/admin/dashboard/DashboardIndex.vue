<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon users">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">用户总数</p>
            <p class="stat-value">{{ stats.totalUsers }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon books">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">商品总数</p>
            <p class="stat-value">{{ stats.totalBooks }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orders">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">订单总数</p>
            <p class="stat-value">{{ stats.totalOrders }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon reports">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">待处理举报</p>
            <p class="stat-value">{{ stats.pendingReports }}</p>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({
  totalUsers: 0,
  totalBooks: 0,
  totalOrders: 0,
  pendingReports: 0
})

onMounted(async () => {
  try {
    const res = await request.get('/admin/dashboard/stats')
    stats.value = res.data
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 20px 0;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;

  &.users { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  &.books { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  &.orders { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  &.reports { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
}

.stat-info {
  .stat-label {
    color: #999;
    margin-bottom: 5px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 600;
  }
}
</style>
