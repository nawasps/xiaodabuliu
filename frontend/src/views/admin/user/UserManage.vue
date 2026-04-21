<template>
  <div class="user-manage-page">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户" style="width: 300px;" @keyup.enter="loadUsers">
        <template #append>
          <el-button :icon="Search" @click="loadUsers" />
        </template>
      </el-input>
    </div>
    <el-table :data="users" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用分" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleResetPassword(row.id)">重置密码</el-button>
          <el-button size="small" :type="row.status === 'NORMAL' ? 'danger' : 'success'" 
                     @click="handleToggleStatus(row)">
            {{ row.status === 'NORMAL' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="loadUsers"
      layout="total, prev, pager, next"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const keyword = ref('')
const users = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/user/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value }
    })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async (id) => {
  await ElMessageBox.confirm('确定要将该用户密码重置为 123456abc 吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.put(`/admin/user/${id}/resetPassword`)
  ElMessage.success('密码已重置为 123456abc')
  loadUsers()
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 'NORMAL' ? 'DISABLED' : 'NORMAL'
  await request.put(`/admin/user/${row.id}/status`, null, { params: { status: newStatus } })
  ElMessage.success('操作成功')
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.user-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
