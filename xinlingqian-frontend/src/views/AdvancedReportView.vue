<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElButton, ElCard, ElProgress, ElDivider } from 'element-plus';
import api from '../api';
import BackButton from '../components/BackButton.vue';

const router = useRouter();

const report = ref(null);
const loading = ref(true);
const showPurchaseModal = ref(false);

const reportApi = {
  getAdvancedReport: () => api.get('/report/advanced'),
  purchaseReport: () => api.post('/report/advanced/purchase'),
};

const fetchReport = async () => {
        loading.value = true;
        try {
            const response = await reportApi.getAdvancedReport();
            if (response.data.code === 200) {
                report.value = response.data.data;
            } else {
                if (response.data.message && response.data.message.includes('请先购买')) {
                    showPurchaseModal.value = true;
                } else {
                    ElMessage.error(response.data.message || '获取报告失败');
                }
            }
        } catch (error) {
            console.error('获取报告失败:', error);
            const errorMsg = error.response?.data?.message || error.message || '获取报告失败';
            if (errorMsg.includes('请先购买') || error.response?.status === 403) {
                showPurchaseModal.value = true;
            } else {
                ElMessage.error(errorMsg);
            }
        } finally {
            loading.value = false;
        }
    };

const purchaseReport = async () => {
  try {
    const response = await reportApi.purchaseReport();
    if (response.data.code === 200) {
      ElMessage.success('购买成功！');
      showPurchaseModal.value = false;
      await fetchReport();
    } else {
      ElMessage.error(response.data.message || '购买失败');
    }
  } catch (error) {
    console.error('购买失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '购买失败';
    if (errorMsg.includes('余额不足')) {
      ElMessage.error('余额不足，请先充值');
    } else {
      ElMessage.error(errorMsg);
    }
  }
};

onMounted(() => {
  fetchReport();
});
</script>

<template>
  <div class="advanced-report-container">
    <div class="report-header">
      <BackButton />
      <div class="header-content">
        <span class="header-icon">📊</span>
        <div class="header-text">
          <h1>进阶心理健康报告</h1>
          <p class="subtitle">深入了解您的心理健康状态</p>
        </div>
      </div>
    </div>

    <div class="report-content" v-if="!loading && report">
      <ElCard class="summary-card" shadow="hover">
        <div class="card-header">
          <span class="card-icon">📋</span>
          <h3>报告摘要</h3>
        </div>
        <p class="summary-text">{{ report.summary }}</p>
      </ElCard>

      <div class="section-row">
        <ElCard class="trend-card" shadow="hover">
          <div class="card-header">
            <span class="card-icon">📈</span>
            <h3>情绪趋势分析</h3>
          </div>
          
          <div class="trend-info">
            <div class="trend-badge" :class="{ 'locked': !report.purchased }">
              {{ report.moodTrend.trend }}
            </div>
            <p class="trend-desc" :class="{ 'locked-text': !report.purchased }">
              {{ report.moodTrend.description }}
            </p>
          </div>

          <div class="weekly-chart">
            <div 
              v-for="(day, index) in report.moodTrend.weeklyData" 
              :key="index" 
              class="chart-item"
              :class="{ 'locked': !report.purchased }"
            >
              <div class="chart-bar">
                <span class="mood-emoji">{{ day.mood }}</span>
                <div class="bar-fill" :style="{ height: day.score * 10 + 'px' }"></div>
              </div>
              <span class="chart-date">{{ day.date }}</span>
            </div>
          </div>
        </ElCard>

        <ElCard class="score-card" shadow="hover">
          <div class="card-header">
            <span class="card-icon">🏥</span>
            <h3>健康评分</h3>
          </div>
          
          <div class="score-overall" :class="{ 'locked': !report.purchased }">
            <div class="score-circle">
              <span class="score-value">{{ report.purchased ? report.healthScore.overall : '**' }}</span>
              <span class="score-label">分</span>
            </div>
            <div class="score-level">{{ report.healthScore.level }}</div>
          </div>

          <div class="score-details">
            <div class="score-item">
              <span class="score-icon">💖</span>
              <div class="score-info">
                <span class="score-name">情绪健康</span>
                <ElProgress 
                  :percentage="report.purchased ? report.healthScore.emotional : 0" 
                  :show-text="report.purchased"
                  stroke-width="8"
                  :color="report.purchased ? '#FFB7B2' : '#DDDDDD'"
                />
              </div>
            </div>
            <div class="score-item">
              <span class="score-icon">💤</span>
              <div class="score-info">
                <span class="score-name">睡眠质量</span>
                <ElProgress 
                  :percentage="report.purchased ? report.healthScore.sleep : 0" 
                  :show-text="report.purchased"
                  stroke-width="8"
                  :color="report.purchased ? '#A8D8EA' : '#DDDDDD'"
                />
              </div>
            </div>
            <div class="score-item">
              <span class="score-icon">🏃</span>
              <div class="score-info">
                <span class="score-name">活动水平</span>
                <ElProgress 
                  :percentage="report.purchased ? report.healthScore.activity : 0" 
                  :show-text="report.purchased"
                  stroke-width="8"
                  :color="report.purchased ? '#FFDAC1' : '#DDDDDD'"
                />
              </div>
            </div>
          </div>
        </ElCard>
      </div>

      <ElCard class="advices-card" shadow="hover">
        <div class="card-header">
          <span class="card-icon">💡</span>
          <h3>个性化建议</h3>
        </div>

        <div class="advices-grid">
          <div 
            v-for="(advice, index) in report.advices" 
            :key="index" 
            class="advice-item"
            :class="{ 'locked': !report.purchased }"
          >
            <div class="advice-header">
              <span class="advice-category">{{ advice.category }}</span>
              <h4>{{ advice.title }}</h4>
            </div>
            <p class="advice-content" :class="{ 'locked-text': !report.purchased }">
              {{ advice.content }}
            </p>
            <div class="advice-suggestion" :class="{ 'locked-text': !report.purchased }">
              <span class="suggestion-icon">💬</span>
              {{ advice.suggestion }}
            </div>
          </div>
        </div>
      </ElCard>

      <div v-if="!report.purchased" class="purchase-section">
        <div class="purchase-overlay">
          <div class="overlay-content">
            <span class="lock-icon">🔒</span>
            <h3>完整报告已锁定</h3>
            <p>解锁完整报告，获取专业的心理健康分析和个性化建议</p>
            <div class="price-info">
              <span class="price-symbol">¥</span>
              <span class="price-amount">50</span>
              <span class="price-unit">元</span>
            </div>
            <ElButton 
              class="purchase-btn" 
              type="primary" 
              size="large"
              @click="showPurchaseModal = true"
            >
              立即解锁
            </ElButton>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在生成报告...</p>
    </div>

    <ElButton 
      v-if="report?.purchased" 
      class="refresh-btn"
      @click="fetchReport"
    >
      🔄 刷新报告
    </ElButton>

    <teleport to="body">
      <div v-if="showPurchaseModal" class="modal-overlay" @click.self="showPurchaseModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>确认购买</h3>
            <span class="close-btn" @click="showPurchaseModal = false">×</span>
          </div>
          <div class="modal-body">
            <div class="confirm-price">
              <span class="confirm-symbol">¥</span>
              <span class="confirm-amount">50</span>
            </div>
            <p>确认支付50元解锁进阶心理健康报告？</p>
            <p class="balance-hint">支付将从您的账户余额中扣除</p>
          </div>
          <div class="modal-footer">
            <ElButton @click="showPurchaseModal = false">取消</ElButton>
            <ElButton type="primary" @click="purchaseReport">确认支付</ElButton>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.advanced-report-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
  padding-bottom: 100px;
}

.report-header {
  text-align: center;
  padding: 30px 20px;
  margin-bottom: 20px;
}

.home-btn {
  background: rgba(255, 139, 148, 0.1);
  border: 1px solid #FF8B94;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: #FF8B94;
  margin-bottom: 10px;
  transition: all 0.2s;
}

.home-btn:hover {
  background: rgba(255, 139, 148, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.header-icon {
  font-size: 48px;
}

.header-text h1 {
  font-size: 28px;
  color: #5D4037;
  margin: 0;
}

.subtitle {
  font-size: 16px;
  color: #9E9E9E;
  margin: 8px 0 0 0;
}

.report-content {
  max-width: 1200px;
  margin: 0 auto;
}

.summary-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.card-icon {
  font-size: 24px;
}

.card-header h3 {
  font-size: 18px;
  color: #5D4037;
  margin: 0;
}

.summary-text {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
}

.section-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .section-row {
    grid-template-columns: 1fr;
  }
}

.trend-card, .score-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
}

.trend-info {
  margin-bottom: 20px;
}

.trend-badge {
  display: inline-block;
  padding: 8px 16px;
  background: linear-gradient(135deg, #FFB7B2 0%, #FF8B94 100%);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.trend-badge.locked {
  background: #DDDDDD;
  color: #999;
}

.trend-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.locked-text {
  color: #AAA;
  filter: blur(2px);
}

.weekly-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 120px;
  padding: 10px 0;
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.chart-item.locked .mood-emoji,
.chart-item.locked .bar-fill {
  opacity: 0.5;
  filter: blur(2px);
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100px;
  justify-content: flex-end;
}

.mood-emoji {
  font-size: 20px;
  margin-bottom: 5px;
}

.bar-fill {
  width: 24px;
  background: linear-gradient(180deg, #FFB7B2 0%, #FF8B94 100%);
  border-radius: 12px 12px 0 0;
  transition: height 0.3s ease;
}

.chart-date {
  font-size: 11px;
  color: #999;
  margin-top: 8px;
}

.score-overall {
  text-align: center;
  margin-bottom: 20px;
}

.score-overall.locked .score-value,
.score-overall.locked .score-level {
  color: #AAA;
}

.score-circle {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  background: linear-gradient(135deg, #FFB7B2 0%, #FF8B94 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
}

.score-label {
  font-size: 12px;
}

.score-level {
  font-size: 16px;
  color: #5D4037;
  font-weight: 600;
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-icon {
  font-size: 20px;
}

.score-info {
  flex: 1;
}

.score-name {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 4px;
}

.advices-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
}

.advices-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .advices-grid {
    grid-template-columns: 1fr;
  }
}

.advice-item {
  background: #FFF8F0;
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid #FFB7B2;
}

.advice-item.locked {
  opacity: 0.7;
}

.advice-header {
  margin-bottom: 10px;
}

.advice-category {
  font-size: 14px;
  color: #FF8B94;
  font-weight: 600;
}

.advice-header h4 {
  font-size: 15px;
  color: #5D4037;
  margin: 6px 0 0 0;
}

.advice-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.advice-suggestion {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #888;
  background: rgba(255, 183, 178, 0.1);
  padding: 10px;
  border-radius: 8px;
}

.suggestion-icon {
  font-size: 16px;
}

.purchase-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 248, 231, 0.95);
  backdrop-filter: blur(10px);
  padding: 30px 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.purchase-overlay {
  max-width: 400px;
  margin: 0 auto;
  text-align: center;
}

.lock-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.purchase-overlay h3 {
  font-size: 22px;
  color: #5D4037;
  margin: 0 0 10px 0;
}

.purchase-overlay p {
  font-size: 14px;
  color: #888;
  margin: 0 0 20px 0;
}

.price-info {
  margin-bottom: 20px;
}

.price-symbol {
  font-size: 24px;
  color: #FF8B94;
}

.price-amount {
  font-size: 48px;
  font-weight: bold;
  color: #FF8B94;
}

.price-unit {
  font-size: 18px;
  color: #FF8B94;
}

.purchase-btn {
  background: linear-gradient(135deg, #FFB7B2 0%, #FF8B94 100%);
  border: none;
  padding: 14px 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(255, 139, 148, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}

.purchase-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 139, 148, 0.5);
}

.refresh-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: white;
  border: 1px solid #FFB7B2;
  color: #FF8B94;
  padding: 12px 20px;
  border-radius: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.modal-overlay {
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
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #EEE;
}

.modal-header h3 {
  margin: 0;
  color: #5D4037;
}

.close-btn {
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 30px 24px;
  text-align: center;
}

.confirm-price {
  margin-bottom: 20px;
}

.confirm-symbol {
  font-size: 32px;
  color: #FF8B94;
}

.confirm-amount {
  font-size: 64px;
  font-weight: bold;
  color: #FF8B94;
}

.modal-body p {
  font-size: 15px;
  color: #666;
  margin: 0 0 10px 0;
}

.balance-hint {
  font-size: 13px !important;
  color: #999 !important;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #EEE;
}

.modal-footer button {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #FFE4C4;
  border-top-color: #FF8B94;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p {
  color: #999;
}
</style>