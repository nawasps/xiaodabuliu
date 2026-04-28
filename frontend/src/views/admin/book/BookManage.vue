<template>
  <div class="book-manage-page">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品" style="width: 300px;" @keyup.enter="loadBooks" />
      <el-select v-model="status" placeholder="商品状态" style="width: 150px;" @change="loadBooks">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="在售" value="ON_SALE" />
        <el-option label="已售" value="SOLD" />
        <el-option label="下架" value="OFFLINE" />
      </el-select>
      <el-button :icon="Search" @click="loadBooks">搜索</el-button>
    </div>
    <el-table :data="books" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" />
      <el-table-column prop="price" label="价格">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="condition" label="新旧程度" />
      <el-table-column prop="sellerNickname" label="卖家" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="goToBookDetail(row)">详情</el-button>
          <el-button size="small" @click="handleAudit(row)">审核</el-button>
          <el-button size="small" type="danger" @click="handleOffline(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="loadBooks"
      layout="total, prev, pager, next"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const keyword = ref('')
const status = ref('')
const books = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'ON_SALE': 'success', 'SOLD': 'warning', 'OFFLINE': 'info', 'RESERVED': '' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待审核', 'ON_SALE': '在售', 'SOLD': '已售', 'OFFLINE': '下架', 'RESERVED': '已预订' }
  return map[status] || status
}

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/book/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value, status: status.value }
    })
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

const goToBookDetail = (row) => {
  router.push(`/book/${row.id}`)
}

const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm('审核通过该商品？点击取消将驳回并下架。', '商品审核', {
      confirmButtonText: '通过',
      cancelButtonText: '驳回',
      distinguishCancelAndClose: true,
      type: 'warning'
    })
    await request.put(`/admin/book/${row.id}/audit`, null, { params: { status: 'ON_SALE' } })
    ElMessage.success('审核通过')
    window.dispatchEvent(new Event('refresh-admin-stats'))
    loadBooks()
  } catch (error) {
    if (error === 'cancel') {
      await request.put(`/admin/book/${row.id}/audit`, null, { params: { status: 'OFFLINE' } })
      ElMessage.success('已驳回并下架')
      window.dispatchEvent(new Event('refresh-admin-stats'))
      loadBooks()
    }
  }
}

const handleOffline = async (row) => {
  await ElMessageBox.confirm('确定下架该商品？', '下架')
  await request.put(`/admin/book/${row.id}/offline`)
  ElMessage.success('已下架')
  window.dispatchEvent(new Event('refresh-admin-stats'))
  loadBooks()
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped lang="scss">
.book-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
