<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendCode } from '../api/user'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  code: '',
  email: '',
  nickname: ''
})

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)

const sendVerificationCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请输入手机号哦~')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('手机号格式不正确哦~')
    return
  }
  
  codeLoading.value = true
  try {
    const response = await sendCode({ phone: form.phone, type: 'REGISTER' })
    if (response.data.code === 200) {
      ElMessage({
        type: 'success',
        message: '验证码: ' + response.data.data,
        duration: 0,
        showClose: true,
        title: '📱 验证码（测试版）'
      })
      startCountdown()
    } else {
      ElMessage.error(response.data.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  } finally {
    codeLoading.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleRegister = async () => {
  if (!form.username) {
    ElMessage.warning('请输入用户名哦~')
    return
  }
  if (!form.password) {
    ElMessage.warning('请输入密码哦~')
    return
  }
  if (!form.confirmPassword) {
    ElMessage.warning('请确认密码哦~')
    return
  }
  if (!form.phone) {
    ElMessage.warning('请输入手机号哦~')
    return
  }
  if (!form.code) {
    ElMessage.warning('请输入验证码哦~')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致，请检查~')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码至少需要6位哦~')
    return
  }
  
  loading.value = true
  try {
    const response = await register(form)
    if (response.data.code === 200) {
      ElMessage.success('注册成功！快去登录吧~')
      router.push('/login')
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <div class="register-bg"></div>
    <div class="register-wrapper">
      <div class="register-card">
        <div class="register-header">
          <div class="logo">
            <span class="logo-emoji">🌟</span>
            <h1 class="logo-title">心灵签</h1>
            <p class="logo-subtitle">开启你的心灵之旅</p>
          </div>
        </div>
        
        <div class="register-form">
          <div class="form-row">
            <div class="form-item">
              <label class="form-label">
                <span class="label-icon">👤</span>
                <span>用户名</span>
              </label>
              <input
                v-model="form.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名（3-20位）"
              />
            </div>
            
            <div class="form-item">
              <label class="form-label">
                <span class="label-icon">💬</span>
                <span>昵称</span>
              </label>
              <input
                v-model="form.nickname"
                type="text"
                class="form-input"
                placeholder="给自己起个可爱的昵称吧~"
              />
            </div>
          </div>
          
          <div class="form-item">
            <label class="form-label">
              <span class="label-icon">📱</span>
              <span>手机号</span>
            </label>
            <input
              v-model="form.phone"
              type="tel"
              class="form-input"
              placeholder="请输入手机号"
            />
          </div>
          
          <div class="form-item">
            <label class="form-label">
              <span class="label-icon">📧</span>
              <span>验证码</span>
            </label>
            <div class="code-input-wrapper">
              <input
                v-model="form.code"
                type="text"
                class="form-input code-input"
                placeholder="请输入验证码"
                maxlength="6"
              />
              <button 
                class="code-btn"
                :disabled="countdown > 0 || codeLoading"
                @click="sendVerificationCode"
              >
                <span v-if="countdown > 0">{{ countdown }}秒后重发</span>
                <span v-else-if="codeLoading">发送中...</span>
                <span v-else>📤 获取验证码</span>
              </button>
            </div>
          </div>
          
          <div class="form-item">
            <label class="form-label">
              <span class="label-icon">✉️</span>
              <span>邮箱（选填）</span>
            </label>
            <input
              v-model="form.email"
              type="email"
              class="form-input"
              placeholder="请输入邮箱地址"
            />
          </div>
          
          <div class="form-row">
            <div class="form-item">
              <label class="form-label">
                <span class="label-icon">🔐</span>
                <span>密码</span>
              </label>
              <div class="password-input-wrapper">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input password-input"
                  placeholder="请输入密码（至少6位）"
                />
                <span 
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  {{ showPassword ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
            
            <div class="form-item">
              <label class="form-label">
                <span class="label-icon">✅</span>
                <span>确认密码</span>
              </label>
              <div class="password-input-wrapper">
                <input
                  v-model="form.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="form-input password-input"
                  placeholder="请再次输入密码"
                />
                <span 
                  class="password-toggle"
                  @click="showConfirmPassword = !showConfirmPassword"
                >
                  {{ showConfirmPassword ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
          </div>
          
          <button 
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            <span v-if="!loading">🎉 立即注册</span>
            <span v-else>注册中...</span>
          </button>
          
          <div class="register-footer">
            <span class="login-link" @click="goToLogin">
              已有账号？👈 立即登录
            </span>
          </div>
        </div>
        
        <div class="register-decor">
          <span class="decor-item">🌈</span>
          <span class="decor-item">🌺</span>
          <span class="decor-item">🌸</span>
        </div>
        
        <div class="register-decor-bottom">
          <span class="decor-item">⭐</span>
          <span class="decor-item">💖</span>
          <span class="decor-item">✨</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
}

.register-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(255, 183, 77, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 215, 0, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(255, 152, 0, 0.15) 0%, transparent 40%);
  pointer-events: none;
}

.register-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 520px;
  padding: 20px;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 60px rgba(255, 183, 77, 0.3),
    0 8px 32px rgba(255, 152, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-emoji {
  font-size: 56px;
  margin-bottom: 12px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.logo-title {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #FF8F00 0%, #FFC107 50%, #FFEB3B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.logo-subtitle {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #5D4037;
}

.label-icon {
  font-size: 16px;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #FFE0B2;
  border-radius: 12px;
  font-size: 15px;
  background: #FFF8E7;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #FF8F00;
  box-shadow: 0 0 0 3px rgba(255, 143, 0, 0.1);
}

.form-input::placeholder {
  color: #BDBDBD;
}

.password-input-wrapper {
  position: relative;
}

.password-input {
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.password-toggle:hover {
  transform: translateY(-50%) scale(1.1);
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-btn {
  padding: 14px 18px;
  border: 2px solid #FFB74D;
  border-radius: 12px;
  font-size: 14px;
  background: #FFF8E7;
  color: #FF8F00;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.code-btn:hover:not(:disabled) {
  background: #FFE0B2;
}

.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.register-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #FF8F00 0%, #FFC107 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 143, 0, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
  margin-top: 15px;
}

.login-link {
  font-size: 14px;
  color: #FF8F00;
  cursor: pointer;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #E65100;
}

.register-decor {
  position: absolute;
  top: -25px;
  right: -25px;
  display: flex;
  gap: 20px;
}

.register-decor-bottom {
  position: absolute;
  bottom: -25px;
  left: -25px;
  display: flex;
  gap: 20px;
}

.decor-item {
  font-size: 32px;
  animation: float 3s ease-in-out infinite;
}

.register-decor .decor-item:nth-child(1) { animation-delay: 0s; }
.register-decor .decor-item:nth-child(2) { animation-delay: 1s; }
.register-decor .decor-item:nth-child(3) { animation-delay: 2s; }

.register-decor-bottom .decor-item:nth-child(1) { animation-delay: 0.5s; }
.register-decor-bottom .decor-item:nth-child(2) { animation-delay: 1.5s; }
.register-decor-bottom .decor-item:nth-child(3) { animation-delay: 2.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(10deg); }
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .register-card {
    padding: 30px 20px;
  }
}
</style>