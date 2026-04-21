完整替换为以下内容：

<template>
  <div class="tag-manage-page">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">添加标签</el-button>
    </div>
    <el-table :data="tags" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="color" label="颜色">
        <template #default="{ row }">
          <span :style="{ color: row.color, fontWeight: 'bold' }">{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="hot" label="热度">
        <template #default="{ row }">
          <el-tag v-if="row.hot === 1" type="danger" size="small">热门</el-tag>
          <el-tag v-else size="small">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
          <span style="margin-left: 10px">{{ form.color }}</span>
        </el-form-item>
        <el-form-item label="热门">
          <el-switch v-model="form.hot" :active-value="1" :inactive-value="0" />
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

const tags = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  name: '',
  color: '#409EFF',
  hot: 0
})

const loadTags = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/tag/list')
    tags.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  dialogTitle.value = '添加标签'
  form.name = ''
  form.color = '#409EFF'
  form.hot = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑标签'
  form.name = row.name
  form.color = row.color || '#409EFF'
  form.hot = row.hot || 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/tag/${currentId.value}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/tag', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadTags()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该标签？')
  await request.delete(`/admin/tag/${row.id}`)
  ElMessage.success('已删除')
  loadTags()
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped lang="scss">
.tag-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>