<template>
  <div class="membership-container">
    <BackButton />
    
    <div class="membership-header">
      <div class="header-icon">👑</div>
      <h1>开通会员</h1>
      <p class="subtitle">成为心灵签会员，享受专属权益</p>
    </div>

    <div class="member-benefits">
      <div class="section-header">
        <h2>会员特权</h2>
      </div>
      <div class="benefit-grid">
        <div class="benefit-card">
          <div class="benefit-icon-wrapper">🌸</div>
          <div class="benefit-info">
            <span class="benefit-title">梅花易数今日运势</span>
            <span class="benefit-desc">每日专属运势测算</span>
          </div>
        </div>
        <div class="benefit-card">
          <div class="benefit-icon-wrapper">📊</div>
          <div class="benefit-info">
            <span class="benefit-title">进阶心理健康报告</span>
            <span class="benefit-desc">深度心理数据分析</span>
          </div>
        </div>
        <div class="benefit-card">
          <div class="benefit-icon-wrapper">💬</div>
          <div class="benefit-info">
            <span class="benefit-title">专业心理咨询服务</span>
            <span class="benefit-desc">一对一专属服务</span>
          </div>
        </div>
        <div class="benefit-card">
          <div class="benefit-icon-wrapper">🎁</div>
          <div class="benefit-info">
            <span class="benefit-title">更多会员专属功能</span>
            <span class="benefit-desc">持续更新中...</span>
          </div>
        </div>
      </div>
    </div>

    <div class="membership-price">
      <div class="price-card">
        <div class="price-tag">
          <span class="currency">¥</span>
          <span class="amount">50</span>
          <span class="period">/年</span>
        </div>
        <p class="price-desc">超值年费会员</p>
      </div>
    </div>

    <div class="payment-section">
      <div class="payment-card">
        <div class="payment-info">
          <span class="payment-label">支付方式</span>
          <span class="payment-balance">当前余额: ¥{{ balance?.toFixed(2) || '0.00' }}</span>
        </div>

        <button 
          class="subscribe-btn" 
          :disabled="isProcessing || !canSubscribe"
          @click="handleSubscribe"
        >
          {{ isProcessing ? '开通中...' : '立即开通' }}
        </button>

        <p v-if="!canSubscribe && balance < 50" class="balance-warning">
          <span class="warning-icon">⚠️</span>
          余额不足，请先充值后再开通会员
        </p>
      </div>
    </div>

    <div v-if="showSuccess" class="success-modal">
      <div class="success-content">
        <div class="success-icon">🎉</div>
        <h2>开通成功!</h2>
        <p>您已成为心灵签会员</p>
        <p class="expire-date">有效期至: {{ expireDate }}</p>
        <button class="success-btn" @click="goToMeihuaTest">立即体验梅花易数</button>
        <button class="back-btn" @click="goHome">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { membershipApi, userApi } from '../api'
import BackButton from '../components/BackButton.vue'

const router = useRouter()

const balance = ref(0)
const isProcessing = ref(false)
const showSuccess = ref(false)
const expireDate = ref('')

const canSubscribe = computed(() => {
  return balance.value >= 50
})

const loadUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.data.code === 200) {
      balance.value = response.data.data.balance || 0
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleSubscribe = async () => {
  if (!canSubscribe.value) {
    alert('余额不足，请先充值')
    return
  }

  isProcessing.value = true

  try {
    const response = await membershipApi.subscribe('balance')
    if (response.data.code === 200) {
      balance.value -= 50
      expireDate.value = new Date(response.data.data.expireDate).toLocaleDateString('zh-CN')
      showSuccess.value = true
      alert('开通会员成功!')
    } else {
      alert(response.data.message || '开通失败')
    }
  } catch (error) {
    alert(error.response?.data?.message || '开通会员失败')
  } finally {
    isProcessing.value = false
  }
}

const goToMeihuaTest = () => {
  router.push('/meihua-yijing')
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.membership-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;
}

.membership-header {
  text-align: center;
  padding: 48px 24px;
  background: linear-gradient(135deg, #FF8F00 0%, #FFC107 100%);
  border-radius: 24px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(255, 143, 0, 0.3);
}

.header-icon {
  font-size: 72px;
  margin-bottom: 16px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.membership-header h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 15px;
  opacity: 0.95;
  margin: 0;
  font-weight: 400;
}

.member-benefits {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 16px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #5D4037;
  margin: 0;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.benefit-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.benefit-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 143, 0, 0.2);
}

.benefit-icon-wrapper {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #FFF8E7, #FFE0B2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.benefit-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.benefit-title {
  font-size: 15px;
  font-weight: 600;
  color: #5D4037;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.benefit-desc {
  font-size: 12px;
  color: #9E9E9E;
  margin-top: 2px;
}

.membership-price {
  margin-bottom: 24px;
}

.price-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  box-shadow: 0 8px 25px rgba(255, 183, 77, 0.15);
  position: relative;
  overflow: hidden;
}

.price-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.1) 0%, transparent 70%);
}

.price-tag {
  display: flex;
  align-items: baseline;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.currency {
  font-size: 28px;
  font-weight: 600;
  color: #FF8F00;
}

.amount {
  font-size: 64px;
  font-weight: 900;
  color: #FF8F00;
  margin: 0 6px;
  text-shadow: 0 4px 8px rgba(255, 143, 0, 0.2);
}

.period {
  font-size: 18px;
  color: #9E9E9E;
}

.price-desc {
  font-size: 14px;
  color: #9E9E9E;
  margin: 12px 0 0 0;
  position: relative;
  z-index: 1;
}

.payment-section {
  margin-bottom: 24px;
}

.payment-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(255, 183, 77, 0.1);
}

.payment-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F5F5F5;
}

.payment-label {
  font-size: 16px;
  font-weight: 600;
  color: #5D4037;
}

.payment-balance {
  font-size: 15px;
  color: #FF8F00;
  font-weight: 600;
}

.subscribe-btn {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #FF8F00 0%, #FFC107 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.subscribe-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 143, 0, 0.4);
}

.subscribe-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.balance-warning {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #FF5722;
  font-size: 14px;
  margin-top: 16px;
  padding: 12px;
  background: #FFF3E0;
  border-radius: 12px;
}

.warning-icon {
  font-size: 18px;
}

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.success-content {
  background: white;
  border-radius: 28px;
  padding: 56px 36px;
  text-align: center;
  max-width: 340px;
  margin: 20px;
  animation: slideUp 0.4s ease;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}

@keyframes slideUp {
  from { 
    transform: translateY(30px); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0); 
    opacity: 1; 
  }
}

.success-icon {
  font-size: 96px;
  margin-bottom: 20px;
  animation: bounceIn 0.6s ease;
}

@keyframes bounceIn {
  0% { transform: scale(0); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.success-content h2 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 12px 0;
  color: #5D4037;
}

.success-content p {
  margin: 8px 0;
  color: #757575;
  font-size: 15px;
}

.expire-date {
  font-size: 15px;
  color: #FF8F00;
  font-weight: 600;
  margin-top: 20px;
  padding: 12px 20px;
  background: #FFF8E7;
  border-radius: 12px;
  display: inline-block;
}

.success-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #FF8F00 0%, #FFC107 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 28px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(255, 143, 0, 0.3);
}

.success-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 143, 0, 0.4);
}

.back-btn {
  width: 100%;
  padding: 14px;
  background: #F5F5F5;
  color: #616161;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  cursor: pointer;
  margin-top: 14px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #E0E0E0;
}

@media (max-width: 480px) {
  .benefit-grid {
    grid-template-columns: 1fr;
  }
  
  .amount {
    font-size: 48px;
  }
  
  .membership-header {
    padding: 32px 20px;
  }
  
  .success-content {
    padding: 40px 24px;
  }
}
</style>