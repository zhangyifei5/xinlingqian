<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.back();
};

const goHome = () => {
  router.push('/');
};
</script>

<template>
  <div class="back-button-group">
    <button class="back-btn" @click="goBack">
      <span class="btn-icon">←</span>
      <span class="btn-text">返回</span>
    </button>
    <button class="home-btn" @click="goHome">
      <span class="btn-icon">🏠</span>
      <span class="btn-text">首页</span>
    </button>
  </div>
</template>

<style scoped>
.back-button-group {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.back-btn,
.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 2px solid #FFE0B2;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  font-weight: 500;
  color: #E57373;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(229, 115, 115, 0.1);
}

.back-btn:hover,
.home-btn:hover {
  background: linear-gradient(135deg, #FFF5F0 0%, #FFE4D4 100%);
  border-color: #FFAB91;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.back-btn:active,
.home-btn:active {
  transform: translateY(0);
}

.back-btn {
  border-color: #FFCCBC;
  color: #E57373;
}

.home-btn {
  border-color: #FFB7B2;
  color: #FF8B94;
}

.btn-icon {
  font-size: 18px;
}

.btn-text {
  font-size: 14px;
}
</style>