<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi, moodTestApi, communityApi } from '../api'

const userNickname = ref('')
const currentMoodEmoji = ref('🤔')
const currentMoodText = ref('未测试')
const moodScore = ref(0)
const hasTestedToday = ref(false)
const loading = ref(true)
const topPosts = ref([])
const isAdmin = ref(false)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息哦~'
  if (hour < 9) return '早上好！新的一天开始了~'
  if (hour < 12) return '上午好！今天也要元气满满哦~'
  if (hour < 14) return '中午好！记得吃午饭哦~'
  if (hour < 18) return '下午好！下午茶时间到~'
  if (hour < 22) return '晚上好！今天辛苦了~'
  return '夜深了，早点休息哦~'
})

const quickActions = [
  { id: 1, icon: '🧠', title: '今日心情测试', desc: '测测今天的心理状态', color: '#FF8F00', route: '/daily-test' },
  { id: 2, icon: '📊', title: '七日心情趋势', desc: '看看心情变化曲线', color: '#4CAF50', route: '/mood-trend' },
  { id: 3, icon: '📅', title: '历史心情日历', desc: '回顾往日心情足迹', color: '#2196F3', route: '/mood-calendar' },
  { id: 4, icon: '💬', title: '心情社区', desc: '分享你的心情故事', color: '#9C27B0', route: '/community' },
  { id: 5, icon: '👨‍⚕️', title: '心理咨询', desc: '专业老师为你解惑', color: '#E91E63', route: '/counselor' },
  { id: 6, icon: '📦', title: '进阶报告', desc: '深入了解你自己', color: '#FF5722', route: '/advanced-report' },
]

const handleLogout = () => {
  localStorage.clear()
  window.location.href = '/login'
}

const getMoodEmoji = (mhi) => {
  if (mhi >= 85) return '😊'
  if (mhi >= 70) return '🙂'
  if (mhi >= 50) return '😐'
  if (mhi >= 30) return '😔'
  return '😢'
}

const getMoodText = (mhi) => {
  if (mhi >= 85) return '很棒哦'
  if (mhi >= 70) return '还不错'
  if (mhi >= 50) return '一般般'
  if (mhi >= 30) return '有点糟'
  return '不太好'
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const getUserAvatar = (avatar) => {
  if (!avatar || avatar.trim() === '') {
    return '👤'
  }
  if (avatar.startsWith('http')) {
    return ''
  }
  return avatar
}

const loadTopPosts = async () => {
  try {
    const response = await communityApi.getPosts()
    if (response.data.code === 200) {
      const sortedPosts = response.data.data.sort((a, b) => b.likes - a.likes)
      topPosts.value = sortedPosts.slice(0, 2)
    }
  } catch (error) {
    console.error('获取热门帖子失败:', error)
  }
}

onMounted(async () => {
  try {
    const userRes = await userApi.getUserInfo()
    if (userRes.data.code === 200) {
      userNickname.value = userRes.data.data.nickname
      isAdmin.value = userRes.data.data.role === 'ADMIN'
    }

    const moodRes = await moodTestApi.getTodayRecord()
    if (moodRes.data.code === 200 && moodRes.data.data) {
      const record = moodRes.data.data
      hasTestedToday.value = true
      moodScore.value = Math.round(record.mhi)
      currentMoodEmoji.value = getMoodEmoji(record.mhi)
      currentMoodText.value = getMoodText(record.mhi)
    }

    await loadTopPosts()
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-container">
    <div class="home-bg"></div>

    <nav class="navbar">
      <div class="navbar-content">
        <div class="logo">
          <span class="logo-icon">✨</span>
          <span class="logo-text">心灵签</span>
        </div>
        <div class="nav-actions">
          <button v-if="isAdmin" class="admin-btn" @click="$router.push('/admin')" title="管理后台">
            <span class="nav-icon">⚙️</span>
          </button>
          <button class="nav-btn" title="消息">
            <span class="nav-icon">🔔</span>
            <span class="nav-badge">3</span>
          </button>
          <div class="user-avatar" @click="handleLogout">
            <span>😃</span>
          </div>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <div class="greeting-section">
        <div class="greeting-text">
          <h1 class="greeting-title">{{ greeting }}</h1>
          <p class="greeting-subtitle">欢迎回来，{{ userNickname }}！</p>
        </div>
        <div class="mood-indicator">
          <span class="mood-emoji">{{ currentMoodEmoji }}</span>
          <div class="mood-info">
            <span class="mood-label">今日心情</span>
            <span class="mood-value">{{ hasTestedToday ? currentMoodText : '去测试' }}</span>
          </div>
        </div>
      </div>

      <div class="today-mood-card" @click="$router.push('/daily-test')">
      <div class="card-header">
        <div class="header-left">
          <span class="header-icon">🎯</span>
          <span class="header-title">今日心情指数</span>
        </div>
        <span class="card-tag">{{ hasTestedToday ? '已测试' : '点击测试' }}</span>
      </div>
      <div class="mood-display">
        <div class="mood-circle">
          <svg class="mood-ring" viewBox="0 0 120 120">
            <circle class="ring-bg" cx="60" cy="60" r="54" />
            <circle
              class="ring-progress"
              cx="60"
              cy="60"
              r="54"
              :stroke-dasharray="`${moodScore * 3.39} 339`"
            />
          </svg>
          <div class="mood-center">
            <span class="mood-big-emoji">{{ currentMoodEmoji }}</span>
            <span class="mood-score">{{ hasTestedToday ? moodScore : '?' }}</span>
          </div>
        </div>
        <div class="mood-details">
          <p class="mood-description">{{ hasTestedToday ? `今日心情指数${moodScore}分` : '还没测今天的心情哦' }}</p>
          <p class="mood-hint">{{ hasTestedToday ? '点击查看详情' : '点击上方按钮，开始今日心情测试' }}</p>
        </div>
      </div>
    </div>

      <section class="quick-actions">
        <h2 class="section-title">
          <span class="title-icon">🚀</span>
          <span>快捷功能</span>
        </h2>
        <div class="actions-grid">
          <div
            v-for="action in quickActions"
            :key="action.id"
            class="action-card"
            @click="$router.push(action.route)"
          >
            <div class="action-icon" :style="{ background: action.color + '20', color: action.color }">
              {{ action.icon }}
            </div>
            <div class="action-info">
              <span class="action-title">{{ action.title }}</span>
              <span class="action-desc">{{ action.desc }}</span>
            </div>
            <span class="action-arrow">→</span>
          </div>
        </div>
      </section>

      <section class="community-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">💬</span>
            <span>心情社区</span>
          </h2>
          <span class="more-link" @click="$router.push('/community')">查看更多 →</span>
        </div>
        <div class="feed-list">
          <div v-if="topPosts.length === 0" class="feed-empty">
            <span class="empty-icon">🌱</span>
            <p>还没有人发布动态</p>
            <p class="empty-hint">成为第一个分享心情的人吧~</p>
          </div>
          <div v-for="item in topPosts" :key="item.id" class="feed-card">
            <div class="feed-header">
              <div class="feed-user">
                <div class="feed-avatar-wrapper">
                  <img 
                    v-if="item.avatar && (item.avatar.startsWith('http') || item.avatar.includes('.'))" 
                    :src="item.avatar.startsWith('http') ? item.avatar : `http://localhost:8080${item.avatar.startsWith('/') ? '' : '/uploads/'}` + item.avatar" 
                    class="feed-avatar-img" 
                    alt="头像" 
                  />
                  <span v-else class="feed-avatar-emoji">{{ item.avatar || '👤' }}</span>
                </div>
                <span class="feed-name">{{ item.nickname }}</span>
              </div>
              <span class="feed-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <p class="feed-content">{{ item.content }}</p>
            <div class="feed-actions">
              <span class="feed-action">
                <span>❤️</span>
                <span>{{ item.likes }}</span>
              </span>
              <span class="feed-action">
                <span>💬</span>
                <span>{{ item.commentsCount }}</span>
              </span>
            </div>
          </div>
        </div>
      </section>

      <section class="counselor-section">
        <div class="counselor-card" @click="$router.push('/counselor')">
          <div class="counselor-left">
            <span class="counselor-icon">👨‍⚕️</span>
            <div class="counselor-info">
              <h3 class="counselor-title">需要专业帮助？</h3>
              <p class="counselor-desc">心理咨询师为你答疑解惑</p>
            </div>
          </div>
          <span class="counselor-arrow">→</span>
        </div>
      </section>
    </main>

    <nav class="bottom-nav">
      <div class="nav-item active">
        <span class="nav-item-icon">🏠</span>
        <span class="nav-item-text">首页</span>
      </div>
      <div class="nav-item" @click="$router.push('/mood-calendar')">
        <span class="nav-item-icon">📅</span>
        <span class="nav-item-text">日历</span>
      </div>
      <div class="nav-item" @click="$router.push('/community')">
        <span class="nav-item-icon">💬</span>
        <span class="nav-item-text">社区</span>
      </div>
      <div class="nav-item" @click="$router.push('/profile')">
        <span class="nav-item-icon">👤</span>
        <span class="nav-item-text">我的</span>
      </div>
    </nav>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding-bottom: 80px;
  position: relative;
  overflow-x: hidden;
}

.home-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 10% 20%, rgba(255, 183, 77, 0.2) 0%, transparent 30%),
    radial-gradient(circle at 90% 80%, rgba(255, 215, 0, 0.15) 0%, transparent 30%),
    radial-gradient(circle at 50% 50%, rgba(255, 152, 0, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 183, 77, 0.2);
}

.navbar-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-btn, .admin-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn {
  background: #FFF8E7;
}

.admin-btn {
  background: #E3F2FD;
}

.nav-btn:hover {
  background: #FFE0B2;
  transform: translateY(-2px);
}

.admin-btn:hover {
  background: #BBDEFB;
  transform: translateY(-2px);
}

.nav-icon {
  font-size: 20px;
}

.nav-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 18px;
  height: 18px;
  background: #FF5722;
  color: white;
  font-size: 10px;
  font-weight: bold;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.greeting-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.greeting-text {
  flex: 1;
}

.greeting-title {
  font-size: 24px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 4px 0;
}

.greeting-subtitle {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.mood-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 10px 16px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.2);
}

.mood-emoji {
  font-size: 32px;
}

.mood-info {
  display: flex;
  flex-direction: column;
}

.mood-label {
  font-size: 12px;
  color: #9E9E9E;
}

.mood-value {
  font-size: 16px;
  font-weight: 600;
  color: #5D4037;
}

.today-mood-card {
  background: white;
  border-radius: 24px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(255, 183, 77, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
}

.today-mood-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(255, 143, 0, 0.25);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #5D4037;
}

.card-tag {
  font-size: 12px;
  color: #FF8F00;
  background: #FFF8E7;
  padding: 4px 12px;
  border-radius: 20px;
}

.mood-display {
  display: flex;
  align-items: center;
  gap: 24px;
}

.mood-circle {
  position: relative;
  width: 120px;
  height: 120px;
}

.mood-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #FFE0B2;
  stroke-width: 10;
}

.ring-progress {
  fill: none;
  stroke: url(#gradient);
  stroke-width: 10;
  stroke-linecap: round;
  stroke: #FF8F00;
  transition: stroke-dasharray 1s ease;
}

.mood-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mood-big-emoji {
  font-size: 36px;
}

.mood-score {
  font-size: 24px;
  font-weight: 800;
  color: #FF8F00;
}

.mood-details {
  flex: 1;
}

.mood-description {
  font-size: 18px;
  font-weight: 600;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.mood-hint {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 16px 0;
}

.title-icon {
  font-size: 20px;
}

.quick-actions {
  margin-bottom: 24px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 143, 0, 0.2);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #5D4037;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-desc {
  font-size: 12px;
  color: #9E9E9E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-arrow {
  font-size: 16px;
  color: #BDBDBD;
  transition: all 0.3s ease;
}

.action-card:hover .action-arrow {
  color: #FF8F00;
  transform: translateX(4px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.more-link {
  font-size: 14px;
  color: #FF8F00;
  cursor: pointer;
  transition: color 0.3s ease;
}

.more-link:hover {
  color: #E65100;
}

.community-section {
  margin-bottom: 24px;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feed-empty {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 16px;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.feed-empty p {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.empty-hint {
  font-size: 12px !important;
  margin-top: 4px !important;
}

.feed-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
}

.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.feed-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feed-avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB6C1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.feed-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.feed-avatar-emoji {
  font-size: 18px;
}

.feed-name {
  font-size: 14px;
  font-weight: 600;
  color: #5D4037;
}

.feed-time {
  font-size: 12px;
  color: #9E9E9E;
}

.feed-content {
  font-size: 14px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.feed-actions {
  display: flex;
  gap: 20px;
}

.feed-action {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #9E9E9E;
}

.counselor-section {
  margin-bottom: 24px;
}

.counselor-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  border-radius: 20px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.counselor-card:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 30px rgba(255, 143, 0, 0.4);
}

.counselor-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.counselor-icon {
  font-size: 40px;
}

.counselor-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0 0 4px 0;
}

.counselor-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.counselor-arrow {
  font-size: 24px;
  color: white;
  transition: transform 0.3s ease;
}

.counselor-card:hover .counselor-arrow {
  transform: translateX(4px);
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  box-shadow: 0 -4px 20px rgba(255, 183, 77, 0.15);
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.nav-item:hover {
  background: #FFF8E7;
}

.nav-item.active {
  color: #FF8F00;
}

.nav-item-icon {
  font-size: 24px;
}

.nav-item-text {
  font-size: 11px;
  font-weight: 500;
}

@media (max-width: 480px) {
  .greeting-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .mood-indicator {
    width: 100%;
  }

  .mood-display {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>