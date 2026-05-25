<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { counselorApi, userApi } from '../api';
import BackButton from '../components/BackButton.vue';

const counselorInfo = ref({
  name: '',
  title: '',
  specialty: '',
  description: '',
  hourlyRate: 0,
  avatar: '',
  level: 1,
  rating: 0,
  consultationCount: 0
});

const isLoading = ref(true);
const showEditModal = ref(false);
const reviews = ref([]);

const editForm = ref({
  title: '',
  specialty: '',
  description: '',
  hourlyRate: 0
});

const loadCounselorInfo = async () => {
  isLoading.value = true;
  try {
    const response = await counselorApi.getMyProfile();
    if (response.data.code === 200) {
      counselorInfo.value = response.data.data;
      editForm.value = {
        title: counselorInfo.value.title || '',
        specialty: counselorInfo.value.specialty || '',
        description: counselorInfo.value.description || '',
        hourlyRate: counselorInfo.value.hourlyRate || 0
      };
    }
  } catch (error) {
    console.error('获取咨询师资料失败:', error);
    ElMessage.error('获取资料失败，请稍后重试');
  } finally {
    isLoading.value = false;
  }
};

const loadReviews = async () => {
  try {
    const response = await counselorApi.getMyReviews();
    if (response.data.code === 200) {
      reviews.value = response.data.data || [];
    }
  } catch (error) {
    console.error('获取评论失败:', error);
  }
};

const openEditModal = () => {
  showEditModal.value = true;
};

const saveProfile = async () => {
  try {
    const response = await counselorApi.updateProfile(editForm.value);
    if (response.data.code === 200) {
      counselorInfo.value = { ...counselorInfo.value, ...editForm.value };
      showEditModal.value = false;
      ElMessage.success('保存成功');
    } else {
      ElMessage.error(response.data.message || '保存失败');
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败，请稍后重试');
  }
};

const getLevelBadge = (level) => {
  const badges = ['⭐', '⭐⭐', '⭐⭐⭐', '⭐⭐⭐⭐', '⭐⭐⭐⭐⭐', '🏆⭐', '🏆⭐⭐', '🏆⭐⭐⭐', '🏆⭐⭐⭐⭐', '🏆⭐⭐⭐⭐⭐'];
  return badges[Math.min(level - 1, 9)] || '⭐';
};

onMounted(() => {
  loadCounselorInfo();
  loadReviews();
});
</script>

<template>
  <div class="counselor-profile-page">
    <div class="page-header">
      <BackButton />
      <h1>🧘 我的咨询师资料</h1>
      <p>管理您的咨询师信息</p>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loading-animation">
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
      </div>
      <p>加载中...</p>
    </div>

    <div v-else class="profile-content">
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img 
              v-if="counselorInfo.avatar && (counselorInfo.avatar.startsWith('http') || counselorInfo.avatar.includes('.'))" 
              :src="counselorInfo.avatar.startsWith('http') ? counselorInfo.avatar : `http://localhost:8080${counselorInfo.avatar.startsWith('/') ? '' : '/uploads/'}` + counselorInfo.avatar" 
              class="avatar-img" 
              alt="头像" 
            />
            <span v-else class="avatar-emoji">{{ counselorInfo.avatar || '👤' }}</span>
            <span class="level-badge">{{ getLevelBadge(counselorInfo.level) }}</span>
          </div>
          <div class="basic-info">
            <h2 class="name">{{ counselorInfo.name }}</h2>
            <span class="title">{{ counselorInfo.title }}</span>
          </div>
        </div>

        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-value">⭐ {{ counselorInfo.rating || 0.0 }}</span>
            <span class="stat-label">平均评分</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">👥 {{ counselorInfo.consultationCount || 0 }}</span>
            <span class="stat-label">咨询次数</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">💰 ¥{{ counselorInfo.hourlyRate }}/时</span>
            <span class="stat-label">咨询费用</span>
          </div>
        </div>

        <div class="info-section">
          <div class="info-item">
            <span class="info-label">专长领域</span>
            <span class="info-value">{{ counselorInfo.specialty || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">个人简介</span>
            <p class="info-desc">{{ counselorInfo.description || '未设置' }}</p>
          </div>
        </div>

        <button class="edit-btn" @click="openEditModal">
          ✏️ 编辑资料
        </button>
      </div>

      <div class="tips-card">
        <h3>💡 小贴士</h3>
        <ul>
          <li>完善您的专业资料可以吸引更多来访者</li>
          <li>保持良好的服务态度可以获得更高评分</li>
          <li>每完成5次5星好评可提升一级</li>
        </ul>
      </div>

      <div class="reviews-card">
        <div class="reviews-header">
          <h3>⭐ 用户评价</h3>
          <span class="reviews-count">{{ reviews.length }} 条评价</span>
        </div>
        
        <div v-if="reviews.length === 0" class="empty-reviews">
          <p>暂无评价</p>
          <p class="empty-hint">完成咨询后，用户会对您进行评价</p>
        </div>
        
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <span class="reviewer-name">{{ review.userNickname || '匿名用户' }}</span>
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

    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-content edit-modal">
        <div class="modal-header">
          <h3>编辑咨询师资料</h3>
          <span class="modal-close" @click="showEditModal = false">×</span>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">职称</label>
            <input 
              v-model="editForm.title" 
              type="text" 
              class="form-input"
              placeholder="如：心理咨询师、心理治疗师"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">专长领域</label>
            <input 
              v-model="editForm.specialty" 
              type="text" 
              class="form-input"
              placeholder="如：情绪管理、人际关系、职业发展"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">咨询费用（元/小时）</label>
            <input 
              v-model.number="editForm.hourlyRate" 
              type="number" 
              class="form-input"
              placeholder="请输入咨询费用"
              min="0"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">个人简介</label>
            <textarea 
              v-model="editForm.description" 
              class="form-textarea"
              placeholder="介绍一下您的专业背景和咨询风格..."
              rows="4"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-cancel" @click="showEditModal = false">取消</button>
          <button class="btn-save" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.counselor-profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFE4D4 30%, #FFDCC9 60%, #FFF0EB 100%);
  padding: 20px;
  padding-bottom: 100px;
}

.page-header {
  text-align: center;
  padding: 30px 20px;
}

.page-header h1 {
  font-size: 28px;
  color: #E57373;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(229, 115, 115, 0.1);
}

.page-header p {
  font-size: 14px;
  color: #FFAB91;
}

.loading-container {
  text-align: center;
  padding: 50px 20px;
}

.loading-animation {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.loading-dot {
  font-size: 20px;
  color: #FF8A65;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.profile-content {
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 24px;
  padding: 25px;
  box-shadow: 0 8px 30px rgba(229, 115, 115, 0.1);
  margin-bottom: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #FFE0E0;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4D4 0%, #FFCCBC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-emoji {
  font-size: 48px;
}

.level-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border-radius: 20px;
  padding: 4px 10px;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(255, 215, 0, 0.4);
}

.basic-info {
  flex: 1;
}

.basic-info .name {
  font-size: 22px;
  font-weight: 600;
  color: #E57373;
  margin: 0 0 8px 0;
}

.basic-info .title {
  display: inline-block;
  padding: 5px 15px;
  background: rgba(229, 115, 115, 0.1);
  color: #E57373;
  border-radius: 20px;
  font-size: 13px;
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 0;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #FFF8F5 0%, #FFFBF9 100%);
  border-radius: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #E57373;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #A1887F;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #FFD0D0;
}

.info-section {
  margin-bottom: 25px;
}

.info-item {
  margin-bottom: 16px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  display: block;
  font-size: 13px;
  color: #A1887F;
  margin-bottom: 6px;
}

.info-value {
  font-size: 15px;
  color: #5D4037;
  font-weight: 500;
}

.info-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  padding: 12px;
  background: #FFF8F5;
  border-radius: 12px;
}

.edit-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.4);
}

.tips-card {
  background: linear-gradient(135deg, #FFF9C4 0%, #FFF59D 100%);
  border-radius: 16px;
  padding: 20px;
  border: 2px dashed #FFD54F;
}

.tips-card h3 {
  font-size: 16px;
  color: #FF8F00;
  margin: 0 0 12px 0;
}

.tips-card ul {
  margin: 0;
  padding-left: 20px;
}

.tips-card li {
  font-size: 13px;
  color: #8D6E63;
  margin-bottom: 8px;
}

.tips-card li:last-child {
  margin-bottom: 0;
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
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 450px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 25px;
  border-bottom: 1px solid #F0F0F0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #E57373;
}

.modal-close {
  font-size: 28px;
  color: #BDBDBD;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #5D4037;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #FFE0E0;
  border-radius: 12px;
  font-size: 14px;
  color: #5D4037;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #FF8A65;
  box-shadow: 0 0 0 3px rgba(255, 171, 145, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #FFE0E0;
  border-radius: 12px;
  font-size: 14px;
  color: #5D4037;
  resize: none;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: #FF8A65;
  box-shadow: 0 0 0 3px rgba(255, 171, 145, 0.1);
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 25px;
  border-top: 1px solid #F0F0F0;
}

.btn-cancel {
  flex: 1;
  padding: 12px;
  background: #F5F5F5;
  color: #666;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #E8E8E8;
}

.btn-save {
  flex: 2;
  padding: 12px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.btn-save:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.4);
}

.reviews-card {
  background: white;
  border-radius: 24px;
  padding: 25px;
  box-shadow: 0 8px 30px rgba(229, 115, 115, 0.1);
  margin-top: 20px;
}

.reviews-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #FFE0E0;
}

.reviews-header h3 {
  font-size: 18px;
  color: #E57373;
  margin: 0;
}

.reviews-count {
  font-size: 13px;
  color: #A1887F;
  background: #FFF8F5;
  padding: 5px 12px;
  border-radius: 20px;
}

.empty-reviews {
  text-align: center;
  padding: 30px;
}

.empty-reviews p {
  margin: 0;
  font-size: 15px;
  color: #9E9E9E;
}

.empty-reviews .empty-hint {
  font-size: 13px;
  margin-top: 8px;
  color: #BDBDBD;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 18px;
  background: #FFF8F5;
  border-radius: 16px;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.reviewer-name {
  font-size: 15px;
  font-weight: 500;
  color: #5D4037;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.review-rating .star {
  font-size: 16px;
  color: #FFD700;
}

.review-rating .star:last-child {
  color: #E0E0E0;
}

.review-comment {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.review-time {
  margin: 0;
  font-size: 12px;
  color: #BDBDBD;
}
</style>
