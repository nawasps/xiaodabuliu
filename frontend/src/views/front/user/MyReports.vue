<template>
  <div class="my-reports-page">
    <div class="page-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的举报" name="report">
          <el-table :data="reports" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bookTitle" label="关联商品" show-overflow-tooltip />
            <el-table-column prop="reason" label="举报原因" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
                  {{ row.status === 'PENDING' ? '处理中' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleResult" label="处理结果" />
            <el-table-column prop="createTime" label="举报时间" width="180" />
          </el-table>
          <el-empty v-if="reports.length === 0" description="暂无举报记录" />
        </el-tab-pane>
        <el-tab-pane label="我的反馈" name="feedback">
          <el-table :data="feedbacks" v-loading="loadingFeedback">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="description" label="反馈内容" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
                  {{ row.status === 'PENDING' ? '处理中' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleResult" label="处理结果" />
            <el-table-column prop="createTime" label="反馈时间" width="180" />
          </el-table>
          <el-empty v-if="feedbacks.length === 0" description="暂无反馈记录" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('report')
const reports = ref([])
const feedbacks = ref([])
const loading = ref(false)
const loadingFeedback = ref(false)

const loadReports = async () => {
  loading.value = true
  try {
    const res = await request.get('/report/my')
    reports.value = (res.data?.records || []).filter(r => r.type === 'REPORT')
  } catch (error) {
    console.error('Failed to load reports:', error)
  } finally {
    loading.value = false
  }
}

const loadFeedbacks = async () => {
  loadingFeedback.value = true
  try {
    const res = await request.get('/report/my')
    feedbacks.value = (res.data?.records || []).filter(r => r.type === 'FEEDBACK')
  } catch (error) {
    console.error('Failed to load feedbacks:', error)
  } finally {
    loadingFeedback.value = false
  }
}

onMounted(() => {
  loadReports()
  loadFeedbacks()
})
</script>

<style scoped lang="scss">
.my-reports-page {
  padding: 20px 0;
}

h2 {
  margin-bottom: 20px;
}
</style>