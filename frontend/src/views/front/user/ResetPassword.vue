<template>
  <div class="reset-page">
    <h2>找回密码</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="验证码" prop="verifyCode">
        <el-input v-model="form.verifyCode" placeholder="请输入验证码">
          <template #append>
            <el-button @click="sendCode" :disabled="sending">
              {{ sending ? countdown + 's' : '获取' }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleReset">重置密码</el-button>
        <el-button @click="$router.push('/login')">返回</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-top: 20px; color: #666; font-size: 14px;">
      <p>重置后密码将统一设置为：<strong>123456abc</strong></p>
      <p>请使用新密码登录后及时修改密码</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const form = reactive({ phone: '', verifyCode: '' })
const sending = ref(false)
const countdown = ref(0)
let timer = null

const rules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

const sendCode = async () => {
  if (sending.value) return
  if (!form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  await request.post('/user/sendCode', { phone: form.phone })
  ElMessage.success('验证码已发送')
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

const handleReset = async () => {
  await formRef.value.validate()
  try {
    await request.post('/user/resetPassword', {
      phone: form.phone,
      verifyCode: form.verifyCode
    })
    ElMessage.success('密码重置成功，新密码为：123456abc')
    router.push('/login')
  } catch (error) {
    const msg = error.response?.data?.message || error.message || '重置失败'
    ElMessage.error(msg)
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.reset-page {
  padding: 40px;
  max-width: 400px;
  margin: 0 auto;
}
</style>