<script setup>import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { counselorApi, userApi } from '../api';
import BackButton from '../components/BackButton.vue';
const router = useRouter();
const counselors = ref([]);
const searchKeyword = ref('');
const isLoading = ref(true);
const showApplyModal = ref(false);
const currentUserId = ref(null);
const currentRole = ref('');
const loadCounselors = async () => {
 isLoading.value = true;
 try {
 const response = await counselorApi.getCounselors(searchKeyword.value);
 if (response.data.code === 200) {
 counselors.value = response.data.data.filter(c => c.userId !== currentUserId.value);
 }
 }
 catch (error) {
 console.error('获取咨询师列表失败:', error);
 }
 finally {
 isLoading.value = false;
 }
};
const getCurrentUserInfo = async () => {
 try {
 const response = await userApi.getUserInfo();
 if (response.data.code === 200) {
 currentUserId.value = response.data.data.id;
 currentRole.value = response.data.data.role;
 }
 } catch (error) {
 console.error('获取用户信息失败:', error);
 }
};
const searchCounselors = () => {
 loadCounselors();
};
const getRandomCounselor = async () => {
 try {
 const response = await counselorApi.getRandomCounselor();
 if (response.data.code === 200) {
 const counselor = response.data.data;
 window.location.href = `/counselor/${counselor.id}`;
 }
 else {
 ElMessage.warning('暂无可用的咨询师');
 }
 }
 catch (error) {
 console.error('获取推荐咨询师失败:', error);
 }
};
const goToCounselorDetail = (id) => {
 window.location.href = `/counselor/${id}`;
};
const goToApply = () => {
 window.location.href = '/counselor/apply';
};
onMounted(async () => {
 await getCurrentUserInfo();
 loadCounselors();
});
</script>

<template>
  <div class="counselor-list-page">
    <div class="page-header">
      <BackButton />
      <h1>🧘 心理咨询师</h1>
      <p>专业的心理支持，温暖的陪伴</p>
    </div>

    <div class="action-bar">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索咨询师姓名、专长..."
          @keyup.enter="searchCounselors"
        />
        <button class="search-btn" @click="searchCounselors">🔍</button>
      </div>
      <button class="random-btn" @click="getRandomCounselor">🎲 随机匹配</button>
    </div>

    <div class="apply-banner" @click="goToApply">
      <div class="banner-content">
        <span class="banner-icon">💡</span>
        <div class="banner-text">
          <span class="banner-title">成为心理咨询师</span>
          <span class="banner-desc">分享你的专业，帮助更多人</span>
        </div>
      </div>
      <span class="banner-arrow">→</span>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loading-animation">
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
      </div>
      <p>加载中...</p>
    </div>

    <div v-else class="counselors-container">
      <div v-if="counselors.length === 0" class="empty-state">
        <div class="empty-emoji">👋</div>
        <h3>暂无咨询师</h3>
        <p>快来成为第一位咨询师吧~</p>
      </div>

      <div v-for="counselor in counselors" :key="counselor.id" class="counselor-card" @click="goToCounselorDetail(counselor.id)">
        <div class="counselor-avatar-wrapper">
          <img 
            v-if="counselor.avatar && (counselor.avatar.startsWith('http') || counselor.avatar.includes('.'))" 
            :src="counselor.avatar.startsWith('http') ? counselor.avatar : `http://localhost:8080${counselor.avatar.startsWith('/') ? '' : '/uploads/'}` + counselor.avatar" 
            class="counselor-avatar" 
            alt="头像" 
          />
          <span v-else class="counselor-avatar-emoji">{{ counselor.avatar || '👤' }}</span>
        </div>
        <div class="counselor-info">
          <div class="counselor-header">
            <span class="counselor-name">{{ counselor.name }}</span>
            <span class="counselor-title">{{ counselor.title }}</span>
          </div>
          <p class="counselor-specialty">🏷️ {{ counselor.specialty }}</p>
          <div class="counselor-stats">
            <span class="stat-item">⭐ {{ counselor.rating || 0.0 }}</span>
            <span class="stat-item">👥 {{ counselor.consultationCount || 0 }}次咨询</span>
            <span class="stat-item">💰 ¥{{ counselor.hourlyRate }}/小时</span>
          </div>
          <p class="counselor-desc">{{ counselor.description }}</p>
        </div>
        <div class="counselor-arrow">→</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.counselor-list-page {
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

.action-bar {
  display: flex;
  gap: 12px;
  padding: 0 10px;
  margin-bottom: 20px;
}

.search-box {
  flex: 1;
  display: flex;
  background: white;
  border-radius: 30px;
  padding: 10px 18px;
  box-shadow: 0 2px 15px rgba(229, 115, 115, 0.1);
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #5D4037;
}

.search-box input::placeholder {
  color: #BDBDBD;
}

.search-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 0 5px;
}

.random-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.random-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.4);
}

.apply-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, #FFF8F5 0%, #FFEFEB 100%);
  border-radius: 20px;
  margin: 0 10px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px dashed #FFAB91;
}

.apply-banner:hover {
  background: linear-gradient(135deg, #FFEFEB 0%, #FFE0D6 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.15);
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.banner-icon {
  font-size: 32px;
}

.banner-text {
  display: flex;
  flex-direction: column;
}

.banner-title {
  font-weight: 600;
  color: #E57373;
  font-size: 16px;
}

.banner-desc {
  font-size: 12px;
  color: #FFAB91;
}

.banner-arrow {
  font-size: 24px;
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

.counselors-container {
  max-width: 600px;
  margin: 0 auto;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-emoji {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: #5D4037;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: #9E9E9E;
}

.counselor-card {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 20px rgba(229, 115, 115, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.counselor-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(229, 115, 115, 0.15);
}

.counselor-avatar-wrapper {
  width: 75px;
  height: 75px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4D4 0%, #FFCCBC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.2);
}

.counselor-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.counselor-avatar-emoji {
  font-size: 36px;
}

.counselor-info {
  flex: 1;
  min-width: 0;
}

.counselor-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.counselor-name {
  font-weight: 600;
  color: #E57373;
  font-size: 17px;
}

.counselor-title {
  padding: 3px 12px;
  background: rgba(229, 115, 115, 0.1);
  color: #E57373;
  border-radius: 20px;
  font-size: 12px;
}

.counselor-specialty {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #FFAB91;
}

.counselor-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}

.stat-item {
  font-size: 12px;
  color: #A1887F;
  display: flex;
  align-items: center;
  gap: 3px;
}

.counselor-desc {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.counselor-arrow {
  font-size: 20px;
  color: #BDBDBD;
  flex-shrink: 0;
  align-self: center;
}
</style>