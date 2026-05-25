<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
</script>

<template>
  <div class="placeholder-page">
    <button class="home-btn" @click="router.push('/')">🏠 返回首页</button>
    <div class="content">
      <span class="emoji">🧠</span>
      <h1>今日心情测试</h1>
      <p>功能开发中，敬请期待...</p>
    </div>
  </div>
</template>

<style scoped>
.placeholder-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.content {
  text-align: center;
}
.emoji {
  font-size: 80px;
  display: block;
  margin-bottom: 20px;
}
h1 {
  font-size: 28px;
  color: #5D4037;
  margin-bottom: 10px;
}
p {
  font-size: 16px;
  color: #9E9E9E;
}
</style>
