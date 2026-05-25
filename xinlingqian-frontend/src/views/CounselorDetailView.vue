<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { counselorApi, consultationApi, reviewApi, userApi } from '../api';
import BackButton from '../components/BackButton.vue';

const route = useRoute();

const counselor = ref(null);
const isLoading = ref(true);
const showPaymentModal = ref(false);
const paymentAmount = ref(0);
const paymentOptions = [50, 100, 200, 500, 1000];
const consultationId = ref(null);
const currentUserId = ref(null);
const isViewingSelf = ref(false);

const reviews = ref([]);
const showReviewModal = ref(false);
const userRating = ref(0);
const userComment = ref('');
const reviewStatus = ref({ hasReviewed: false, hasPaid: false });
const averageRating = ref(0);
const reviewCount = ref(0);

const getCurrentUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo();
    if (response.data.code === 200) {
      currentUserId.value = response.data.data.id;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
};

const loadCounselor = async () => {
  isLoading.value = true;
  const id = route.params.id;
  try {
    const response = await counselorApi.getCounselor(id);
    if (response.data.code === 200) {
      counselor.value = response.data.data;
      isViewingSelf.value = counselor.value.userId === currentUserId.value;
    }
    await loadReviews(id);
    await checkReviewStatus(id);
    await loadRatingInfo(id);
  } catch (error) {
    console.error('获取咨询师信息失败:', error);
  } finally {
    isLoading.value = false;
  }
};

const loadReviews = async (counselorId) => {
  try {
    const response = await reviewApi.getReviews(counselorId);
    if (response.data.code === 200) {
      reviews.value = response.data.data;
    }
  } catch (error) {
    console.error('获取评论失败:', error);
  }
};

const checkReviewStatus = async (counselorId) => {
  try {
    const response = await reviewApi.checkReviewStatus(counselorId);
    if (response.data.code === 200) {
      reviewStatus.value = response.data.data;
    }
  } catch (error) {
    console.error('检查评论状态失败:', error);
  }
};

const loadRatingInfo = async (counselorId) => {
  try {
    const response = await reviewApi.getRatingInfo(counselorId);
    if (response.data.code === 200) {
      averageRating.value = response.data.data.averageRating;
      reviewCount.value = response.data.data.reviewCount;
    }
  } catch (error) {
    console.error('获取评分信息失败:', error);
  }
};

const startChat = async () => {
  try {
    const response = await consultationApi.createConsultation(counselor.value.id);
    if (response.data.code === 200) {
      consultationId.value = response.data.data.id;
      window.location.href = `/consultation/chat/${response.data.data.id}`;
    }
  } catch (error) {
    console.error('创建咨询会话失败:', error);
    ElMessage.error('创建会话失败，请稍后重试');
  }
};

const openPaymentModal = () => {
  showPaymentModal.value = true;
};

const selectPaymentAmount = (amount) => {
  paymentAmount.value = amount;
};

const confirmPayment = async () => {
  if (!paymentAmount.value || paymentAmount.value <= 0) {
    ElMessage.warning('请选择支付金额');
    return;
  }
  try {
    let consultId = consultationId.value;
    if (!consultId) {
      const response = await consultationApi.createConsultation(counselor.value.id);
      if (response.data.code === 200) {
        consultId = response.data.data.id;
      }
    }
    const paymentResponse = await consultationApi.createPayment({
      consultationId: consultId,
      counselorId: counselor.value.id,
      amount: paymentAmount.value
    });
    if (paymentResponse.data.code === 200) {
      const paymentId = paymentResponse.data.data.id;
      const completeResponse = await consultationApi.completePayment(paymentId);
      if (completeResponse.data.code === 200) {
        ElMessage.success('支付成功！');
        showPaymentModal.value = false;
        paymentAmount.value = 0;
        setTimeout(() => {
          window.location.href = `/consultation/chat/${consultId}`;
        }, 1500);
      }
    }
  } catch (error) {
    console.error('支付失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '支付失败，请稍后重试';
    if (errorMsg.includes('余额不足')) {
      ElMessage.error('余额不足，请先充值');
    } else {
      ElMessage.error(errorMsg);
    }
  }
};

const openReviewModal = () => {
  if (!reviewStatus.value.hasPaid) {
    ElMessage.warning('请先完成支付后再进行评价');
    return;
  }
  if (reviewStatus.value.hasReviewed) {
    ElMessage.warning('您已经评价过这位咨询师了');
    return;
  }
  showReviewModal.value = true;
};

const selectStar = (star) => {
  userRating.value = star;
};

const submitReview = async () => {
  if (userRating.value === 0) {
    ElMessage.warning('请选择评分');
    return;
  }
  try {
    const response = await reviewApi.addReview({
      counselorId: counselor.value.id,
      rating: userRating.value,
      comment: userComment.value
    });
    if (response.data.code === 200) {
      ElMessage.success('评价成功！');
      showReviewModal.value = false;
      userRating.value = 0;
      userComment.value = '';
      reviewStatus.value.hasReviewed = true;
      await loadReviews(counselor.value.id);
      await loadRatingInfo(counselor.value.id);
    }
  } catch (error) {
    console.error('提交评价失败:', error);
    ElMessage.error(error.response?.data?.message || '评价失败，请稍后重试');
  }
};

onMounted(async () => {
  await getCurrentUserInfo();
  loadCounselor();
});
</script>

<template>
  <div class="counselor-detail-page">
    <div v-if="isLoading" class="loading-container">
      <div class="loading-animation">
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
      </div>
      <p>加载中...</p>
    </div>

    <template v-else>
      <div class="counselor-header">
        <BackButton />
        <div class="counselor-avatar-section">
          <div class="avatar-wrapper">
            <img 
              v-if="counselor.avatar && (counselor.avatar.startsWith('http') || counselor.avatar.includes('.'))" 
              :src="counselor.avatar.startsWith('http') ? counselor.avatar : `http://localhost:8080${counselor.avatar.startsWith('/') ? '' : '/uploads/'}` + counselor.avatar" 
              class="counselor-avatar" 
              alt="头像" 
            />
            <span v-else class="avatar-emoji">{{ counselor.avatar || '👤' }}</span>
          </div>
          <div class="counselor-basic-info">
            <h1>{{ counselor.name }}</h1>
            <span class="counselor-title">{{ counselor.title }}</span>
          </div>
        </div>
      </div>

      <div class="stats-bar">
        <div class="stat-item level-stat">
          <div class="level-badge">
            <span class="level-number">{{ counselor.level || 1 }}</span>
            <span class="level-label">级咨询师</span>
          </div>
          <span class="stat-label">等级</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">⭐ {{ averageRating || counselor.rating || 0.0 }}</span>
          <span class="stat-label">评分</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">📝 {{ reviewCount }}</span>
          <span class="stat-label">评价数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">👥 {{ counselor.consultationCount || 0 }}</span>
          <span class="stat-label">咨询次数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">💰 ¥{{ counselor.hourlyRate }}</span>
          <span class="level-label">/小时</span>
        </div>
      </div>

      <div class="content-section">
        <div class="section-card">
          <h3 class="section-title">🏷️ 专长领域</h3>
          <p class="section-content">{{ counselor.specialty }}</p>
        </div>

        <div class="section-card">
          <h3 class="section-title">📚 从业经历</h3>
          <p class="section-content">{{ counselor.experience }}</p>
        </div>

        <div class="section-card">
          <h3 class="section-title">🎓 学历背景</h3>
          <p class="section-content">{{ counselor.education }}</p>
        </div>

        <div class="section-card">
          <h3 class="section-title">📜 资质证书</h3>
          <p class="section-content">{{ counselor.certificate }}</p>
        </div>

        <div class="section-card">
          <h3 class="section-title">💬 个人简介</h3>
          <p class="section-content">{{ counselor.description }}</p>
        </div>

        <div class="section-card review-section">
          <div class="review-header">
            <h3 class="section-title">⭐ 用户评价</h3>
            <button 
              class="write-review-btn" 
              :disabled="!reviewStatus.hasPaid || reviewStatus.hasReviewed"
              @click="openReviewModal"
            >
              {{ reviewStatus.hasReviewed ? '已评价' : (reviewStatus.hasPaid ? '写评价' : '支付后评价') }}
            </button>
          </div>
          
          <div v-if="reviews.length === 0" class="empty-reviews">
            <p>暂无评价，快来成为第一个评价的人吧~</p>
          </div>
          
          <div v-else class="reviews-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header-item">
                <div class="reviewer-info">
                  <span class="reviewer-avatar">👤</span>
                  <span class="reviewer-name">用户{{ review.userId }}</span>
                </div>
                <div class="review-rating">
                  <span v-for="i in 5" :key="i" class="star">
                    {{ i <= review.rating ? '★' : '☆' }}
                  </span>
                </div>
              </div>
              <p v-if="review.comment" class="review-comment">{{ review.comment }}</p>
              <p class="review-time">{{ review.createTime }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="action-buttons" v-if="!isViewingSelf">
        <button class="action-btn chat-btn" @click="startChat">
          <span class="btn-icon">💬</span>
          <span class="btn-text">开始交流</span>
        </button>
        <button class="action-btn payment-btn" @click="openPaymentModal">
          <span class="btn-icon">💰</span>
          <span class="btn-text">预约咨询</span>
        </button>
      </div>
      
      <div v-else class="self-profile-hint">
        <p>👤 这是您自己的咨询师资料</p>
        <p class="hint-text">您可以在"我的"页面查看和编辑您的资料</p>
      </div>

      <div v-if="showPaymentModal" class="modal-overlay" @click.self="showPaymentModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>预约咨询</h3>
            <span class="modal-close" @click="showPaymentModal = false">×</span>
          </div>
          <div class="modal-body">
            <p class="payment-hint">选择预约金额（咨询费用）</p>
            <div class="amount-options">
              <button 
                v-for="amount in paymentOptions" 
                :key="amount"
                class="amount-btn"
                :class="{ selected: paymentAmount === amount }"
                @click="selectPaymentAmount(amount)"
              >
                ¥{{ amount }}
              </button>
            </div>
            <div v-if="paymentAmount > 0" class="payment-summary">
              <p>已选择: <span class="selected-amount">¥{{ paymentAmount }}</span></p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showPaymentModal = false">取消</button>
            <button class="btn-confirm" @click="confirmPayment">确认支付</button>
          </div>
        </div>
      </div>

      <div v-if="showReviewModal" class="modal-overlay" @click.self="showReviewModal = false">
        <div class="modal-content review-modal">
          <div class="modal-header">
            <h3>评价咨询师</h3>
            <span class="modal-close" @click="showReviewModal = false">×</span>
          </div>
          <div class="modal-body">
            <div class="rating-section">
              <p class="rating-label">请选择评分</p>
              <div class="star-rating">
                <button 
                  v-for="i in 5" 
                  :key="i" 
                  class="star-btn"
                  :class="{ active: i <= userRating }"
                  @click="selectStar(i)"
                >
                  {{ i <= userRating ? '★' : '☆' }}
                </button>
              </div>
              <p class="rating-hint">{{ ['非常不满意', '不满意', '一般', '满意', '非常满意'][userRating - 1] || '请选择评分' }}</p>
            </div>
            <div class="comment-section">
              <label class="comment-label">评价内容（选填）</label>
              <textarea 
                v-model="userComment" 
                class="comment-input" 
                placeholder="请输入您的评价..."
                maxlength="500"
              ></textarea>
              <span class="comment-count">{{ userComment.length }}/500</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showReviewModal = false">取消</button>
            <button class="btn-confirm" @click="submitReview">提交评价</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.counselor-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFE4D4 30%, #FFDCC9 60%, #FFF0EB 100%);
  padding-bottom: 120px;
}

.loading-container {
  text-align: center;
  padding: 100px 20px;
}

.loading-animation {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.loading-dot {
  font-size: 24px;
  color: #FF8A65;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.counselor-header {
  padding: 20px;
  padding-top: 30px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-bottom: 20px;
  margin-right: 10px;
}

.home-btn {
  background: rgba(255, 139, 148, 0.1);
  border: 1px solid #FF8B94;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: #FF8B94;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.home-btn:hover {
  background: rgba(255, 139, 148, 0.2);
  color: #E57373;
  font-weight: 500;
}

.counselor-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4D4 0%, #FFCCBC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(255, 171, 145, 0.3);
}

.counselor-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-emoji {
  font-size: 48px;
}

.counselor-basic-info {
  flex: 1;
}

.counselor-basic-info h1 {
  font-size: 28px;
  color: #E57373;
  margin: 0 0 10px 0;
}

.counselor-title {
  padding: 6px 15px;
  background: rgba(229, 115, 115, 0.1);
  color: #E57373;
  border-radius: 20px;
  font-size: 14px;
}

.stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  background: white;
  margin: 0 20px;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(229, 115, 115, 0.08);
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 70px;
}

.stat-item.level-stat {
  min-width: 80px;
}

.level-badge {
  display: flex;
  align-items: baseline;
  gap: 2px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.level-number {
  font-size: 24px;
  font-weight: 700;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.level-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #E57373;
}

.stat-label {
  font-size: 12px;
  color: #FFAB91;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #FFE4D4;
}

.content-section {
  padding: 20px;
}

.section-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 15px rgba(229, 115, 115, 0.08);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #E57373;
  margin: 0 0 10px 0;
}

.section-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.review-section {
  padding-bottom: 15px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.write-review-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  border: none;
  border-radius: 20px;
  font-size: 14px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.write-review-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.write-review-btn:disabled {
  background: #E0E0E0;
  cursor: not-allowed;
}

.empty-reviews {
  text-align: center;
  padding: 30px;
  color: #BDBDBD;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  padding: 15px;
  background: #FFF8F5;
  border-radius: 15px;
}

.review-header-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reviewer-avatar {
  font-size: 24px;
}

.reviewer-name {
  font-size: 14px;
  color: #E57373;
  font-weight: 500;
}

.review-rating .star {
  font-size: 18px;
  color: #FFB74D;
}

.review-comment {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 10px 0;
}

.review-time {
  font-size: 12px;
  color: #BDBDBD;
  margin: 0;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 15px;
  padding: 20px;
  background: white;
  box-shadow: 0 -4px 20px rgba(229, 115, 115, 0.1);
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 18px;
  border: none;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-btn {
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.4);
}

.payment-btn {
  background: linear-gradient(135deg, #FFCC80 0%, #FFB74D 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.3);
}

.payment-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 183, 77, 0.4);
}

.btn-icon {
  font-size: 20px;
}

.self-profile-hint {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 25px;
  background: linear-gradient(135deg, #F5F5F5 0%, #E8E8E8 100%);
  text-align: center;
  padding-bottom: calc(25px + env(safe-area-inset-bottom));
}

.self-profile-hint p {
  margin: 0;
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.self-profile-hint .hint-text {
  font-size: 13px;
  color: #9E9E9E;
  margin-top: 5px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 25px 25px 0 0;
  padding: 25px;
  padding-bottom: calc(25px + env(safe-area-inset-bottom));
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  font-size: 20px;
  color: #E57373;
  margin: 0;
}

.modal-close {
  font-size: 30px;
  color: #BDBDBD;
  cursor: pointer;
}

.modal-body {
  padding: 10px 0;
}

.payment-hint {
  font-size: 14px;
  color: #666;
  margin: 0 0 20px 0;
  text-align: center;
}

.amount-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.amount-btn {
  padding: 15px;
  border: 2px solid #FFE4D4;
  border-radius: 15px;
  font-size: 18px;
  font-weight: 600;
  color: #E57373;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.amount-btn:hover {
  border-color: #FF8A65;
}

.amount-btn.selected {
  border-color: #FF8A65;
  background: rgba(255, 171, 145, 0.1);
  color: #E57373;
}

.payment-summary {
  text-align: center;
  margin-top: 20px;
  padding: 15px;
  background: #FFF5F0;
  border-radius: 15px;
}

.selected-amount {
  font-size: 24px;
  font-weight: 600;
  color: #E57373;
}

.modal-footer {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.btn-cancel {
  flex: 1;
  padding: 15px;
  background: #F5F5F5;
  border: none;
  border-radius: 15px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
}

.btn-confirm {
  flex: 2;
  padding: 15px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  border: none;
  border-radius: 15px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
}

.rating-section {
  text-align: center;
  padding: 15px 0;
}

.rating-label {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
}

.star-rating {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.star-btn {
  font-size: 40px;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.star-btn:hover {
  transform: scale(1.2);
}

.star-btn.active {
  color: #FFB74D;
}

.star-btn:not(.active) {
  color: #E0E0E0;
}

.rating-hint {
  font-size: 14px;
  color: #FF8A65;
  margin: 0;
}

.comment-section {
  margin-top: 20px;
}

.comment-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: block;
}

.comment-input {
  width: 100%;
  height: 120px;
  padding: 15px;
  border: 2px solid #FFE4D4;
  border-radius: 15px;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
}

.comment-input:focus {
  outline: none;
  border-color: #FF8A65;
}

.comment-count {
  font-size: 12px;
  color: #BDBDBD;
  float: right;
  margin-top: 5px;
}
</style>