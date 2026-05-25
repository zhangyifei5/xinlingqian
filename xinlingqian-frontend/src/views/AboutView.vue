<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
</script>

<template>
  <div class="about">
    <button class="home-btn" @click="router.push('/')">🏠 返回首页</button>
    <h1>This is an about page</h1>
  </div>
</template>

<style>
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
}

@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
}
</style>