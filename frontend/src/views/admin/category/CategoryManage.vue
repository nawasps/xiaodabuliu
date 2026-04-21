<template>
  <div class="category-manage-page">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd(null)">添加一级分类</el-button>
    </div>
    <el-table :data="flatCategories" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleAdd(row)">添加子分类</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="上级分类" v-if="form.parentId !== undefined">
          <el-input v-model="parentName" disabled />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const loading = ref(false)
const formRef = ref()
const isEdit = ref(false)
const currentId = ref(null)
const parentName = ref('')

const form = ref({
  name: '',
  sort: 0,
  status: 'ACTIVE',
  parentId: null
})

const flatCategories = computed(() => {
  const result = []
  const flatten = (list, level = 0) => {
    list.forEach(item => {
      result.push({ ...item, _level: level })
      if (item.children?.length) {
        flatten(item.children, level + 1)
      }
    })
  }
  flatten(categories.value)
  return result
})

const loadCategories = async () => {
  try {
    const res = await request.get('/admin/category/tree')
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const handleAdd = (parent) => {
  isEdit.value = false
  currentId.value = null
  dialogTitle.value = parent ? '添加子分类' : '添加一级分类'
  form.value = {
    name: '',
    sort: 0,
    status: 'ACTIVE',
    parentId: parent?.id || null
  }
  parentName.value = parent?.name || ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑分类'
  form.value = {
    name: row.name,
    sort: row.sort || 0,
    status: row.status || 'ACTIVE',
    parentId: row.parentId
  }
  parentName.value = row.parentName || ''
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除分类"${row.name}"？${row.children?.length ? '（该操作将同时删除所有子分类）' : ''}`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.delete(`/admin/category/${row.id}`)
  ElMessage.success('删除成功')
  loadCategories()
}

const handleSubmit = async () => {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/category/${currentId.value}`, form.value)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/category', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.category-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>