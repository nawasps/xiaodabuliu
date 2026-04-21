<template>
  <div class="user-profile-page">
    <div class="page-container">
      <el-card>
        <template #header>
          <h3>个人资料</h3>
        </template>
        <el-form :model="form" label-width="100px">
          <el-form-item label="头像">
            <el-upload :show-file-list="false">
              <el-avatar :src="form.avatar" :size="80" />
            </el-upload>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="手机号">
            <div style="display: flex; gap: 10px;">
              <el-input v-model="form.phone" style="flex: 1;" />
              <el-button @click="handleSendCode" :disabled="sending">
                {{ sending ? countdown + 's' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="showPhoneCode" label="验证码">
            <el-input v-model="form.phoneCode" placeholder="请输入验证码" style="width: 200px;" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="mt-20">
        <template #header>
          <h3>修改密码</h3>
        </template>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少6位,包含数字和英文字母" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword">修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updatePassword, updateUserInfo } from '@/api/user'
import request from '@/utils/request'
const router = useRouter()
const userStore = useUserStore()
const passwordFormRef = ref()
const sending = ref(false)
const countdown = ref(0)
const showPhoneCode = ref(false)
let timer = null

const form = ref({
  username: '',
  nickname: '',
  phone: '',
  phoneCode: '',
  email: '',
  avatar: ''
})
const originalPhone = ref('')

onMounted(async () => {
  try {
    const res = await getUserInfo()
    form.value = res.data
    originalPhone.value = res.data.phone  // 保存原始手机号
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码至少6位'))
  } else if (!/\d/.test(value)) {
    callback(new Error('密码必须包含数字'))
  } else if (!/[a-zA-Z]/.test(value)) {
    callback(new Error('密码必须包含英文字母'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
})
const handleSendCode = async () => {
  if (sending.value) return
  
  // 修改手机号时，验证码应该发给新手机号
  const phoneToVerify = form.value.phone
  
  if (!phoneToVerify) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneToVerify)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  // 如果是新手机号才发验证码
  if (phoneToVerify !== originalPhone.value) {
    await request.post('/user/sendCode', { phone: phoneToVerify })
    ElMessage.success('验证码已发送')
    showPhoneCode.value = true
  } else {
    ElMessage.warning('新手机号与原手机号相同')
    return
  }
  
  sending.value = true
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      sending.value = false
    }
  }, 1000)
}

const handleSave = async () => {
  // 手机号格式校验
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    if (form.value.phone !== originalPhone.value) {
      if (!form.value.phoneCode) {
        ElMessage.warning('请先获取验证码')
        return
      }
      await request.put('/user/phone', {
        phone: form.value.phone,
        verifyCode: form.value.phoneCode
      })
      originalPhone.value = form.value.phone
    }
    await updateUserInfo(form.value)
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
    showPhoneCode.value = false
    form.value.phoneCode = ''
  } catch (error) {
    const msg = error.response?.data?.message || error.message || '保存失败'
    ElMessage.error(msg)
  }
}

const handleChangePassword = async () => {
  await passwordFormRef.value?.validate()
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (error) {
    const msg = error.response?.data?.message || error.message || '修改失败'
    ElMessage.error(msg)
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.user-profile-page {
  padding: 20px 0;
}
</style>