<template>
  <div class="report-manage-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="举报管理" name="report">
        <el-table :data="reports" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="reportUsername" label="举报人" />
          <el-table-column prop="reportedUsername" label="被举报人" />
          <el-table-column prop="bookTitle" label="关联商品" show-overflow-tooltip />
          <el-table-column prop="reason" label="举报原因" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" v-if="row.status === 'PENDING'" @click="handleReport(row)">处理</el-button>
              <el-button size="small" :disabled="!row.bookId" @click="goToBookDetail(row.bookId)">商品详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="用户反馈" name="feedback">
        <el-table :data="feedbacks" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="reportUsername" label="反馈人" />
          <el-table-column prop="description" label="反馈内容" show-overflow-tooltip />
          <el-table-column prop="bookTitle" label="关联商品" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="createTime" label="时间" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" :disabled="!row.bookId" @click="goToBookDetail(row.bookId)">商品详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const activeTab = ref('report')
const reports = ref([])
const feedbacks = ref([])
const loading = ref(false)

const goToBookDetail = (bookId) => {
  if (!bookId) {
    ElMessage.warning('该反馈未关联商品')
    return
  }
  router.push(`/book/${bookId}`)
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/report/list', { params: { pageNum: 1, pageSize: 100 } })
    reports.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to load reports:', error)
  } finally {
    loading.value = false
  }
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/report/feedback/list', { params: { pageNum: 1, pageSize: 100 } })
    feedbacks.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to load feedbacks:', error)
  } finally {
    loading.value = false
  }
}

const handleReport = async (row) => {
  const { value: handleResult } = await ElMessageBox.prompt('请输入处理结果', '处理举报', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await request.put(`/admin/report/${row.id}/handle`, { handleResult: handleResult })
  ElMessage.success('处理成功')
  
  // 通知 AdminLayout 刷新未处理举报数量
  window.dispatchEvent(new Event('refresh-admin-stats'))
  
  loadReports()
}

onMounted(() => {
  loadReports()
  loadFeedbacks()
})
</script>

<style scoped lang="scss">
.report-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
</style>
