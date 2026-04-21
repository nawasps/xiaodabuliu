<template>
  <div class="register-page">
    <div class="register-card">
      <h2>注册</h2>
      <div class="register-mode">
        <el-radio-group v-model="registerType" size="large">
          <el-radio-button label="PHONE">手机号注册</el-radio-button>
          <el-radio-button label="EMAIL">邮箱注册</el-radio-button>
        </el-radio-group>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 (4-20位)" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码 (至少6位,包含数字和英文字母)" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>
        <el-form-item v-if="registerType === 'PHONE'" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱（选填）" size="large" />
        </el-form-item>
        <el-form-item v-if="registerType === 'EMAIL'" prop="verifyCode">
          <el-input v-model="form.verifyCode" placeholder="请输入邮箱验证码" size="large">
            <template #append>
              <el-button :disabled="codeSending || countdown > 0" @click="handleSendEmailCode">
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register, sendEmailVerifyCode } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const registerType = ref('PHONE')
let countdownTimer = null
const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  nickname: '',
  verifyCode: ''
})

const validatePhoneByMode = (rule, value, callback) => {
  if (registerType.value !== 'PHONE') {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请输入手机号'))
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
    return
  }
  callback()
}

const validateEmailByMode = (rule, value, callback) => {
  if (registerType.value !== 'EMAIL' && !value) {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请输入邮箱'))
    return
  }
  if (!emailPattern.test(value)) {
    callback(new Error('请输入正确的邮箱'))
    return
  }
  callback()
}

const validateEmailCode = (rule, value, callback) => {
  if (registerType.value !== 'EMAIL') {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请输入邮箱验证码'))
    return
  }
  callback()
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
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
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4-20个字符', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhoneByMode, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmailByMode, trigger: 'blur' }
  ],
  verifyCode: [
    { validator: validateEmailCode, trigger: 'blur' }
  ]
})

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleSendEmailCode = async () => {
  if (!form.email || !emailPattern.test(form.email)) {
    ElMessage.warning('请先输入正确的邮箱')
    return
  }
  codeSending.value = true
  try {
    await sendEmailVerifyCode(form.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (error) {
    const msg = error.response?.data?.message || error.message || '发送失败'
    ElMessage.error(msg)
  } finally {
    codeSending.value = false
  }
}

const handleRegister = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      phone: registerType.value === 'PHONE' ? form.phone : null,
      email: form.email,
      nickname: form.nickname,
      verifyCode: registerType.value === 'EMAIL' ? form.verifyCode : null,
      registerType: registerType.value
    }
    const res = await register(payload)
    userStore.setToken(res.data.token)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (error) {
    const msg = error.response?.data?.message || error.message || '注册失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);

  h2 {
    text-align: center;
    margin-bottom: 30px;
    font-size: 24px;
  }
}

.register-mode {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;

  a {
    color: #409eff;
    margin-left: 5px;
  }
}
</style>
