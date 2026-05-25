<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getCaptcha } from '../api/user'
import { moodTestApi } from '../api'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const captchaImage = ref('')
const loading = ref(false)
const showPassword = ref(false)

const loadCaptcha = async () => {
  try {
    const response = await getCaptcha()
    captchaImage.value = response.data.image
  } catch (error) {
    console.error('加载验证码失败:', error)
  }
}

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码哦~')
    return
  }
  
  if (!form.captcha) {
    ElMessage.warning('请输入验证码哦~')
    return
  }
  
  loading.value = true
  try {
    const response = await login(form)
    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('userId', response.data.data.userId)
      localStorage.setItem('username', response.data.data.username)
      localStorage.setItem('nickname', response.data.data.nickname)
      localStorage.setItem('role', response.data.data.role)
      
      try {
        const baselineRes = await moodTestApi.checkBaselineStatus()
        if (baselineRes.data.code === 200 && !baselineRes.data.data) {
          ElMessage.info('欢迎来到心灵签！让我们先完成一个初始测试~')
          router.push('/register-test')
        } else {
          ElMessage.success('登录成功！欢迎来到心灵签~')
          router.push('/')
        }
      } catch (e) {
        console.error('检查基线状态失败:', e)
        ElMessage.success('登录成功！欢迎来到心灵签~')
        router.push('/')
      }
    } else {
      ElMessage.error(response.data.message || '登录失败')
      loadCaptcha()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})

const goToRegister = () => {
  router.push('/register')
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <span class="logo-emoji">💖</span>
            <h1 class="logo-title">心灵签</h1>
            <p class="logo-subtitle">记录心情，遇见更好的自己</p>
          </div>
        </div>
        
        <div class="login-form">
          <div class="form-item">
            <label class="form-label">
              <span class="label-icon">👤</span>
              <span>用户名/手机号</span>
            </label>
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入你的用户名或手机号"
              @keyup.enter="handleLogin"
            />
          </div>
          
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
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
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
              <span class="label-icon">🔢</span>
              <span>验证码</span>
            </label>
            <div class="captcha-wrapper">
              <input
                v-model="form.captcha"
                type="text"
                class="form-input captcha-input"
                placeholder="请输入验证码"
                maxlength="4"
                @keyup.enter="handleLogin"
              />
              <div class="captcha-image" @click="loadCaptcha">
                <img :src="captchaImage" alt="验证码" v-if="captchaImage" />
                <span v-else class="captcha-loading">加载中...</span>
              </div>
            </div>
          </div>
          
          <button 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">🚀 立即登录</span>
            <span v-else>登录中...</span>
          </button>
          
          <div class="login-footer">
            <span class="forgot-link" @click="goToForgotPassword">
              忘记密码？😢
            </span>
            <span class="register-link" @click="goToRegister">
              还没有账号？👉 立即注册
            </span>
          </div>
        </div>
        
        <div class="login-decor">
          <span class="decor-item">✨</span>
          <span class="decor-item">🌟</span>
          <span class="decor-item">💫</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
}

.login-bg {
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

.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 60px rgba(255, 183, 77, 0.3),
    0 8px 32px rgba(255, 152, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
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

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.captcha-wrapper {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 46px;
  border: 2px solid #FFE0B2;
  border-radius: 12px;
  background: #FFF8E7;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.captcha-image:hover {
  border-color: #FF8F00;
  box-shadow: 0 0 0 3px rgba(255, 143, 0, 0.1);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 12px;
  color: #BDBDBD;
}

.login-btn {
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

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 143, 0, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.register-link {
  font-size: 14px;
  color: #FF8F00;
  cursor: pointer;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #E65100;
}

.forgot-link {
  display: block;
  font-size: 14px;
  color: #9E9E9E;
  cursor: pointer;
  transition: color 0.3s ease;
  margin-bottom: 10px;
}

.forgot-link:hover {
  color: #FF8F00;
}

.login-decor {
  position: absolute;
  top: -20px;
  right: -20px;
  display: flex;
  gap: 15px;
}

.decor-item {
  font-size: 24px;
  animation: float 3s ease-in-out infinite;
}

.decor-item:nth-child(1) { animation-delay: 0s; }
.decor-item:nth-child(2) { animation-delay: 1s; }
.decor-item:nth-child(3) { animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(10deg); }
}
</style>
