<script setup>import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { communityApi, moodTestApi } from '../api';
import BackButton from '../components/BackButton.vue';

const router = useRouter();
const posts = ref([]);
const isLoading = ref(true);
const showPostModal = ref(false);
const newPostContent = ref('');
const selectedMoodStatus = ref('');
const selectedMoodTag = ref('');
const todayReport = ref(null);
const expandedComments = ref(new Set());
const commentInput = ref({});

const currentUserId = computed(() => {
  return localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null;
});

const moodStatusOptions = [
  { emoji: '🌟', value: 'super_happy', label: '超级开心', description: '今天心情好到爆炸！' },
  { emoji: '😊', value: 'happy', label: '小开心', description: '今天还不错哦~' },
  { emoji: '😌', value: 'calm', label: '岁月静好', description: '今天很平静很舒服' },
  { emoji: '😔', value: 'sad', label: '蓝瘦香菇', description: '今天有点小难过' },
  { emoji: '😤', value: 'angry', label: '暴躁在线', description: '别惹我，我超凶的！' },
  { emoji: '🤔', value: 'thinking', label: '思考人生', description: '我是谁，我在哪，我要干嘛' },
  { emoji: '🥰', value: 'love', label: '恋爱ing', description: '空气中都是粉色泡泡' },
  { emoji: '🎉', value: 'excited', label: '嗨到飞起', description: '今天是派对时刻！' },
  { emoji: '😴', value: 'tired', label: '电量不足', description: '让我再睡5分钟' },
  { emoji: '🤯', value: 'stress', label: '压力山大', description: '头发又掉了一把' },
  { emoji: '😜', value: 'playful', label: '皮一下很开心', description: '今天我是小调皮蛋' },
  { emoji: '🫶', value: 'grateful', label: '感恩的心', description: '今天是被爱包围的一天' }
];

const tagChineseNames = {
  'FULL': '满电怪', 'POWER': '强力输出', 'ZZZZ': '装死者', 'SLOW': '0.5倍速',
  'BED': '床之眷者', 'MEH': '好累人', 'LOW': '电量过低', 'SUN': '小太阳',
  'CALM': '平静如镜', 'CHILL': '松弛感大师', 'FAKE': '伪人', 'BLUE': '淡淡的蓝',
  'GRR': '暴躁辣椒', 'OUCH': '破碎感', 'MALO': '吗喽', 'HIDE': '电子乌龟',
  'ATM-er': '送钱者', 'CHAT': '话唠精', 'BOSS': '领导者', 'JOKER': '小丑',
  'HUG': '贴贴怪', 'FLOW': '心流者', 'OH-NO': '哦不人', 'WHEEL': '跑轮鼠',
  'ZEN': '短暂佛', 'AHA': '顿悟者', 'FOG': '脑雾人', 'FOCUS': '激光眼',
  'YUM': '美食雷达', 'BUY': '野性消费', 'PLAY': '头号玩家', 'GOGO': '行者',
  'CRY': '泪失禁', 'READ': '宇宙探索者', 'DO IT': '彻底疯狂', 'WILD': '野人'
};

const getChineseTag = (tag) => tagChineseNames[tag] || tag;

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  if (hours < 1) return '刚刚';
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN');
};

const loadPosts = async () => {
  isLoading.value = true;
  try {
    const response = await communityApi.getPosts();
    if (response.data.code === 200) {
      posts.value = response.data.data;
    }
  } catch (error) {
    console.error('获取帖子失败:', error);
  } finally {
    isLoading.value = false;
  }
};

const loadTodayReport = async () => {
  try {
    const response = await moodTestApi.getTodayRecord();
    if (response.data.code === 200 && response.data.data) {
      todayReport.value = response.data.data;
    }
  } catch (error) {
    console.error('获取今日报告失败:', error);
  }
};

const openPostModal = () => {
  showPostModal.value = true;
};

const closePostModal = () => {
  showPostModal.value = false;
  newPostContent.value = '';
  selectedMoodStatus.value = '';
  selectedMoodTag.value = '';
};

const shareTodayReport = () => {
  if (!todayReport.value) {
    ElMessage.warning('今日还未完成心情测试哦~');
    return;
  }
  selectedMoodTag.value = todayReport.value.mainTag;
  selectedMoodStatus.value = 'share_report';
  newPostContent.value = `📊 今日心情报告：${getChineseTag(todayReport.value.mainTag)}，${Math.round(todayReport.value.mhi)}分\n\n快来看看我今天的心情状态吧~`;
};

const submitPost = async () => {
  if (!newPostContent.value.trim() && !selectedMoodStatus.value) {
    ElMessage.warning('请输入内容或选择心情状态');
    return;
  }
  try {
    const postData = {
      content: newPostContent.value,
      moodStatus: selectedMoodStatus.value === 'share_report' ? null : selectedMoodStatus.value,
      moodTag: selectedMoodTag.value,
      moodScore: todayReport.value?.mhi || null,
      hasReportLink: selectedMoodStatus.value === 'share_report' ? true : false
    };
    await communityApi.createPost(postData);
    ElMessage.success('发布成功！');
    closePostModal();
    loadPosts();
  } catch (error) {
    ElMessage.error('发布失败，请稍后重试');
  }
};

const deletePost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await communityApi.deletePost(post.id);
    ElMessage.success('删除成功！');
    loadPosts();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后重试');
    }
  }
};

const likePost = async (post) => {
  try {
    await communityApi.likePost(post.id);
    post.likes++;
  } catch (error) {
    ElMessage.error('点赞失败');
  }
};

const toggleComments = (postId) => {
  if (expandedComments.value.has(postId)) {
    expandedComments.value.delete(postId);
  } else {
    expandedComments.value.add(postId);
    loadPostComments(postId);
  }
};

const loadPostComments = async (postId) => {
  try {
    const response = await communityApi.getComments(postId);
    if (response.data.code === 200) {
      const post = posts.value.find(p => p.id === postId);
      if (post) {
        post.comments = response.data.data;
      }
    }
  } catch (error) {
    console.error('获取评论失败:', error);
  }
};

const submitComment = async (post) => {
  const content = commentInput.value[post.id];
  if (!content || !content.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  try {
    const response = await communityApi.addComment(post.id, { content });
    if (response.data.code === 200) {
      const comment = response.data.data;
      if (!post.comments) post.comments = [];
      post.comments.unshift(comment);
      post.commentsCount++;
      commentInput.value[post.id] = '';
      ElMessage.success('评论成功');
    }
  } catch (error) {
    ElMessage.error('评论失败');
  }
};

const getUserAvatar = (avatar) => {
  if (!avatar || avatar.trim() === '') {
    return '👤';
  }
  if (avatar.startsWith('http') || avatar.includes('.')) {
    return '';
  }
  return avatar;
};

const goToReport = (post) => {
  if (post.hasReportLink || (post.content && post.content.includes('📊 今日心情报告'))) {
    router.push({
      name: 'historyReport',
      params: { date: post.createTime.split('T')[0] },
      query: { userId: post.userId }
    });
  }
};

onMounted(() => {
  loadPosts();
  loadTodayReport();
});
</script>

<template>
  <div class="community-page">
    <div class="page-header">
      <BackButton />
      <div class="header-content">
        <h1>💬 心情社区</h1>
        <p>分享你的心情故事，遇见同频的灵魂</p>
      </div>
      <div class="decoration-ring"></div>
      <div class="decoration-star star1">⭐</div>
      <div class="decoration-star star2">✨</div>
    </div>

    <div class="fab-button" @click="openPostModal">
      <span class="fab-icon">+</span>
      <span class="fab-text">发动态</span>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loading-animation">
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
      </div>
      <p>加载中...</p>
    </div>

    <div v-else class="posts-container">
      <div v-if="posts.length === 0" class="empty-state">
        <div class="empty-illustration">
          <span class="empty-emoji">🌱</span>
          <div class="empty-cloud cloud1">☁️</div>
          <div class="empty-cloud cloud2">☁️</div>
        </div>
        <h3>还没有人发布心情</h3>
        <p>做第一个分享心情的人吧~</p>
        <button class="empty-btn" @click="openPostModal">发布第一条心情</button>
      </div>

      <div v-for="post in posts" :key="post.id" class="post-card">
        <div class="post-header">
          <div class="user-info">
            <div class="avatar-wrapper">
              <img v-if="post.avatar && (post.avatar.startsWith('http') || post.avatar.includes('.'))" :src="post.avatar.startsWith('http') ? post.avatar : `http://localhost:8080${post.avatar.startsWith('/') ? '' : '/uploads/'}` + post.avatar" class="avatar-img" alt="头像" />
              <span v-else class="avatar-emoji">{{ post.avatar || '👤' }}</span>
            </div>
            <div class="user-details">
              <span class="nickname">{{ post.nickname }}</span>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <button v-if="post.userId === currentUserId" class="delete-btn" @click="deletePost(post)">
            🗑️
          </button>
        </div>

        <div class="post-content">
          <p :class="{ 'clickable': post.hasReportLink || (post.content && post.content.includes('📊 今日心情报告')) }"
             @click="goToReport(post)">
            {{ post.content }}
          </p>
          
          <div v-if="post.moodStatus || post.moodTag" class="mood-badge-group">
            <span v-if="post.moodStatus" class="mood-badge status">
              {{ moodStatusOptions.find(o => o.value === post.moodStatus)?.emoji }}
              {{ moodStatusOptions.find(o => o.value === post.moodStatus)?.label }}
            </span>
            <span v-if="post.moodTag" class="mood-badge tag">
              🏷️ {{ getChineseTag(post.moodTag) }}
            </span>
            <span v-if="post.moodScore" class="mood-badge score">
              💯 {{ Math.round(post.moodScore) }}分
            </span>
          </div>
          
          <div v-if="post.hasReportLink || (post.content && post.content.includes('📊 今日心情报告'))" class="report-hint" @click="goToReport(post)">
            📌 点击查看详细心情报告 →
          </div>
        </div>

        <div class="post-actions">
          <button class="action-btn" @click="likePost(post)">
            <span class="action-icon">❤️</span>
            <span class="action-text">{{ post.likes }}</span>
          </button>
          <button class="action-btn" @click="toggleComments(post.id)">
            <span class="action-icon">💬</span>
            <span class="action-text">{{ post.commentsCount }}</span>
          </button>
        </div>

        <div v-if="expandedComments.has(post.id)" class="comments-section">
          <div v-if="(!post.comments || post.comments.length === 0)" class="no-comments">
            <p>还没有评论，来说点什么吧~</p>
          </div>
          
          <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar-wrapper">
              <img v-if="comment.avatar && (comment.avatar.startsWith('http') || comment.avatar.includes('.'))" :src="comment.avatar.startsWith('http') ? comment.avatar : `http://localhost:8080${comment.avatar.startsWith('/') ? '' : '/uploads/'}` + comment.avatar" class="comment-avatar-img" alt="头像" />
              <span v-else class="comment-avatar-emoji">{{ comment.avatar || '👤' }}</span>
            </div>
            <div class="comment-content">
              <span class="comment-nickname">{{ comment.nickname }}</span>
              <span class="comment-text">{{ comment.content }}</span>
            </div>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>

          <div class="comment-input-wrapper">
            <input 
              v-model="commentInput[post.id]"
              type="text" 
              placeholder="写下你的评论..."
              class="comment-input"
              @keyup.enter="submitComment(post)"
            />
            <button class="comment-submit" @click="submitComment(post)">发送</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showPostModal" class="modal-overlay" @click.self="closePostModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>✍️ 发布心情</h2>
          <button class="close-btn" @click="closePostModal">×</button>
        </div>

        <div class="modal-body">
          <div v-if="todayReport" class="quick-share" @click="shareTodayReport">
            <div class="share-card">
              <span class="share-emoji">📊</span>
              <div class="share-info">
                <span class="share-label">一键分享今日报告</span>
                <span class="share-preview">{{ getChineseTag(todayReport.mainTag) }} · {{ Math.round(todayReport.mhi) }}分</span>
              </div>
              <span class="share-arrow">→</span>
            </div>
          </div>

          <textarea 
            v-model="newPostContent"
            placeholder="分享你的心情故事..."
            class="post-textarea"
            rows="4"
          ></textarea>

          <div class="mood-selector">
            <h4>🌈 选择今日状态</h4>
            <div class="mood-options">
              <button 
                v-for="mood in moodStatusOptions" 
                :key="mood.value"
                class="mood-option"
                :class="{ selected: selectedMoodStatus === mood.value }"
                @click="selectedMoodStatus = selectedMoodStatus === mood.value ? '' : mood.value"
              >
                <span class="mood-emoji">{{ mood.emoji }}</span>
                <span class="mood-label">{{ mood.label }}</span>
                <span class="mood-desc">{{ mood.description }}</span>
              </button>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closePostModal">取消</button>
          <button class="btn-submit" @click="submitPost">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 30%, #FFF0F5 60%, #FFE4E1 100%);
  padding: 20px;
  padding-bottom: 100px;
  position: relative;
  overflow-x: hidden;
}

.page-header {
  text-align: center;
  padding: 30px 20px;
  position: relative;
}

.header-content h1 {
  font-size: 32px;
  color: #5D4037;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(255, 105, 180, 0.1);
}

.header-content p {
  font-size: 14px;
  color: #9E9E9E;
}

.decoration-ring {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 120px;
  height: 120px;
  border: 3px solid rgba(255, 105, 180, 0.3);
  border-radius: 50%;
  animation: ringRotate 20s linear infinite;
}

.decoration-star {
  position: absolute;
  font-size: 24px;
  animation: starTwinkle 2s ease-in-out infinite;
}

.star1 {
  top: 10px;
  left: 20%;
  animation-delay: 0s;
}

.star2 {
  bottom: 10px;
  right: 25%;
  animation-delay: 1s;
}

@keyframes ringRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes starTwinkle {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

.fab-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px 25px;
  background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.4);
  z-index: 100;
  transition: all 0.3s ease;
}

.fab-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(255, 105, 180, 0.5);
}

.fab-icon {
  font-size: 24px;
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
  color: #FF69B4;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.posts-container {
  max-width: 500px;
  margin: 0 auto;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  position: relative;
  margin-bottom: 20px;
}

.empty-emoji {
  font-size: 64px;
  display: block;
  animation: float 3s ease-in-out infinite;
}

.empty-cloud {
  position: absolute;
  font-size: 24px;
  animation: cloudFloat 4s ease-in-out infinite;
}

.cloud1 {
  top: 10px;
  left: 30%;
}

.cloud2 {
  top: 20px;
  right: 30%;
  animation-delay: 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes cloudFloat {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(10px); }
}

.empty-state h3 {
  font-size: 20px;
  color: #5D4037;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: #9E9E9E;
  margin-bottom: 20px;
}

.empty-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.empty-btn:hover {
  transform: scale(1.05);
}

.post-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.post-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #FF69B4, #FFD700, #90EE90, #87CEEB);
  opacity: 0.6;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB6C1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(255, 105, 180, 0.2);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-emoji {
  font-size: 22px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: bold;
  color: #5D4037;
  font-size: 15px;
}

.post-time {
  font-size: 12px;
  color: #9E9E9E;
}

.delete-btn {
  background: #FFF0F0;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: #FFE0E0;
  transform: scale(1.1);
}

.post-content {
  margin-bottom: 15px;
}

.post-content p {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin: 0;
  word-break: break-all;
}

.post-content p.clickable {
  cursor: pointer;
  color: #FF69B4;
}

.post-content p.clickable:hover {
  text-decoration: underline;
}

.mood-badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.mood-badge {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

.mood-badge.status {
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.15) 0%, rgba(255, 105, 180, 0.1) 100%);
  color: #FF1493;
  border: 1px solid rgba(255, 105, 180, 0.3);
}

.mood-badge.tag {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15) 0%, rgba(255, 215, 0, 0.1) 100%);
  color: #FFA500;
  border: 1px solid rgba(255, 215, 0, 0.3);
}

.mood-badge.score {
  background: linear-gradient(135deg, rgba(144, 238, 144, 0.15) 0%, rgba(144, 238, 144, 0.1) 100%);
  color: #32CD32;
  border: 1px solid rgba(144, 238, 144, 0.3);
}

.report-hint {
  margin-top: 10px;
  padding: 8px 12px;
  background: linear-gradient(90deg, #FFF0F5 0%, #FFE4E1 100%);
  border-radius: 10px;
  font-size: 12px;
  color: #FF69B4;
  cursor: pointer;
  transition: all 0.3s ease;
}

.report-hint:hover {
  background: linear-gradient(90deg, #FFE4E1 0%, #FFB6C1 100%);
  transform: translateX(3px);
}

.post-actions {
  display: flex;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #F5F5F5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 15px;
  background: #FAFAFA;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #FFE4E1;
  transform: scale(1.05);
}

.action-icon {
  font-size: 16px;
}

.action-text {
  font-size: 14px;
  color: #5D4037;
}

.comments-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #F0F0F0;
}

.no-comments {
  text-align: center;
  padding: 20px;
  color: #9E9E9E;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #FAFAFA;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E0E0E0 0%, #F5F5F5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.comment-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-avatar-emoji {
  font-size: 14px;
}

.comment-content {
  flex: 1;
}

.comment-nickname {
  font-weight: bold;
  color: #5D4037;
  font-size: 13px;
  margin-right: 8px;
}

.comment-text {
  font-size: 14px;
  color: #333;
}

.comment-time {
  font-size: 11px;
  color: #BDBDBD;
  flex-shrink: 0;
}

.comment-input-wrapper {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.comment-input {
  flex: 1;
  padding: 12px 15px;
  border: 2px solid #F0F0F0;
  border-radius: 25px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.comment-input:focus {
  outline: none;
  border-color: #FF69B4;
}

.comment-submit {
  padding: 12px 20px;
  background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.comment-submit:hover {
  transform: scale(1.05);
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
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 25px 25px 0 0;
  padding: 25px;
  padding-bottom: calc(25px + env(safe-area-inset-bottom));
  animation: slideUp 0.3s ease;
  max-height: 85vh;
  overflow-y: auto;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.modal-header h2 {
  font-size: 20px;
  color: #5D4037;
  margin: 0;
}

.close-btn {
  width: 35px;
  height: 35px;
  border: none;
  background: #F5F5F5;
  border-radius: 50%;
  font-size: 24px;
  color: #9E9E9E;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #E0E0E0;
}

.quick-share {
  margin-bottom: 20px;
  cursor: pointer;
}

.share-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  background: linear-gradient(135deg, #FFF0F5 0%, #FFE4E1 100%);
  border-radius: 15px;
  border: 2px dashed #FFB6C1;
  transition: all 0.3s ease;
}

.share-card:hover {
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB6C1 100%);
  transform: scale(1.02);
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.2);
}

.share-emoji {
  font-size: 32px;
}

.share-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.share-label {
  font-size: 14px;
  font-weight: bold;
  color: #5D4037;
}

.share-preview {
  font-size: 12px;
  color: #FF69B4;
}

.share-arrow {
  font-size: 20px;
  color: #FF69B4;
  font-weight: bold;
}

.post-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #F0F0F0;
  border-radius: 15px;
  font-size: 15px;
  resize: none;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.post-textarea:focus {
  outline: none;
  border-color: #FF69B4;
}

.post-textarea::placeholder {
  color: #BDBDBD;
}

.mood-selector {
  margin-top: 20px;
}

.mood-selector h4 {
  font-size: 15px;
  color: #5D4037;
  margin: 0 0 15px 0;
}

.mood-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.mood-option {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 15px;
  background: #FAFAFA;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mood-option:hover {
  background: #FFF0F5;
  border-color: #FFB6C1;
  transform: translateY(-2px);
}

.mood-option.selected {
  background: linear-gradient(135deg, #FFF0F5 0%, #FFE4E1 100%);
  border-color: #FF69B4;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.2);
}

.mood-emoji {
  font-size: 28px;
  margin-bottom: 4px;
}

.mood-label {
  font-size: 14px;
  font-weight: bold;
  color: #5D4037;
}

.mood-desc {
  font-size: 11px;
  color: #9E9E9E;
  line-height: 1.3;
}

.modal-footer {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.btn-cancel {
  flex: 1;
  padding: 14px;
  background: #F5F5F5;
  border: none;
  border-radius: 15px;
  font-size: 14px;
  color: #5D4037;
  cursor: pointer;
  transition: background 0.3s ease;
}

.btn-cancel:hover {
  background: #E0E0E0;
}

.btn-submit {
  flex: 2;
  padding: 14px;
  background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
  border: none;
  border-radius: 15px;
  font-size: 14px;
  font-weight: bold;
  color: white;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.btn-submit:hover {
  transform: scale(1.02);
}
</style>