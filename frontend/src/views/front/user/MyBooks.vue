<template>
  <div class="my-books-page">
    <div class="page-container">
      <div class="flex-between mb-20">
        <h2>我的发布</h2>
        <el-button type="primary" @click="$router.push('/user/publish')">发布新书</el-button>
      </div>
      <el-table :data="books" v-loading="loading">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="condition" label="新旧程度" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" />
        <el-table-column label="操作" width="250">
  <template #default="{ row }">
    <el-button size="small" @click="$router.push(`/book/${row.id}`)">查看</el-button>
    <el-button size="small" type="primary" @click="$router.push(`/user/publish/${row.id}`)">编辑</el-button>
    <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
  </template>
</el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBooks, deleteBook } from '@/api/book'

const books = ref([])
const loading = ref(false)

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'ON_SALE': 'success', 'SOLD': 'warning', 'RESERVED': '', 'OFFLINE': 'info' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待审核', 'ON_SALE': '在售', 'SOLD': '已售', 'RESERVED': '已预订', 'OFFLINE': '下架' }
  return map[status] || status
}

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await getMyBooks({ pageNum: 1, pageSize: 100 })
    books.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该书籍？')
  await deleteBook(row.id)
  ElMessage.success('已删除')
  loadBooks()
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped lang="scss">
.my-books-page {
  padding: 20px 0;
}
</style>
