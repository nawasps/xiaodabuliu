<template>
  <div class="review-manage-page">
    <el-table :data="reviews" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="reviewerNickname" label="评价人" />
      <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分">
        <template #default="{ row }">
          <el-rate v-model="row.rating" disabled show-score />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'HANDLED' ? 'success' : 'warning'">
            {{ row.status === 'HANDLED' ? '已审核' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评价时间" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
          <el-button v-if="row.status !== 'HANDLED'" size="small" type="primary" @click="handleAudit(row)">审核</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="600px">
      <el-descriptions v-if="currentReview" :column="2" border>
        <el-descriptions-item label="评价人">{{ currentReview.reviewerNickname }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.rating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">{{ currentReview.content }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ currentReview.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentReview.status === 'HANDLED' ? 'success' : 'warning'">
            {{ currentReview.status === 'HANDLED' ? '已审核' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider v-if="currentOrder">关联订单</el-divider>

      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag>{{ getOrderStatus(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
      </el-descriptions>

      <el-empty v-if="!currentOrder" description="暂无订单信息" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const reviews = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentReview = ref({})
const currentOrder = ref(null)

const getOrderStatus = (status) => {
  const map = {
    'PENDING_PAY': '待付款',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/review/list', { params: { pageNum: 1, pageSize: 100 } })
    reviews.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  currentReview.value = row
  detailVisible.value = true
  // 获取关联订单
  try {
    const res = await request.get('/admin/review/' + row.id + '/with-order')
    currentOrder.value = res.data?.order || null
  } catch (error) {
    currentOrder.value = null
  }
}

const handleAudit = async (row) => {
  await ElMessageBox.confirm('审核通过该评价？', '审核')
  await request.put('/admin/review/' + row.id + '/audit', null, { params: { status: 'HANDLED' } })
  ElMessage.success('审核成功')
  loadReviews()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('删除该评价？', '提示')
  await request.delete('/admin/review/' + row.id)
  ElMessage.success('已删除')
  loadReviews()
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
.review-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
</style>