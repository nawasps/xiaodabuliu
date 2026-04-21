完整替换为以下内容：

<template>
  <div class="notice-manage-page">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">发布公告</el-button>
    </div>
    <el-table :data="notices" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" rows="4" placeholder="请输入公告内容" />
        </el-form-item>
       <el-form-item label="优先级">
  <el-select v-model="form.priority" placeholder="请选择优先级">
    <el-option label="高" :value="1" />
    <el-option label="中" :value="2" />
    <el-option label="低" :value="3" />
  </el-select>
</el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const notices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  title: '',
  content: '',
  priority: 'MEDIUM',
  status: 'ACTIVE'
})

const getPriorityType = (priority) => {
  const map = { 'HIGH': 'danger', 'MEDIUM': 'warning', 'LOW': 'info' }
  return map[priority] || 'info'
}

const loadNotices = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/notice/list', { params: { pageNum: 1, pageSize: 100 } })
    notices.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  dialogTitle.value = '发布公告'
  form.title = ''
  form.content = ''
  form.priority = 'MEDIUM'
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑公告'
  form.title = row.title
  form.content = row.content
  form.priority = row.priority || 'MEDIUM'
  form.status = row.status || 'ACTIVE'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title?.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.content?.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/notice/${currentId.value}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/notice', form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadNotices()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该公告？')
  await request.delete(`/admin/notice/${row.id}`)
  ElMessage.success('已删除')
  loadNotices()
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped lang="scss">
.notice-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>