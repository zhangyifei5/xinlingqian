<script setup>import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { moodTestApi } from '../api';
import BackButton from '../components/BackButton.vue';
const router = useRouter();

const tagChineseNames = {
  // 能量状态
  'FULL': '满电怪',
  'POWER': '强力输出',
  'ZZZZ': '装死者',
  'SLOW': '0.5倍速',
  'BED': '床之眷者',
  'MEH': '好累人',
  'LOW': '电量过低',
  // 情绪基调
  'SUN': '小太阳',
  'CALM': '平静如镜',
  'CHILL': '松弛感大师',
  'FAKE': '伪人',
  'BLUE': '淡淡的蓝',
  'GRR': '暴躁辣椒',
  'OUCH': '破碎感',
  // 社交状态
  'MALO': '吗喽',
  'HIDE': '电子乌龟',
  'ATM-er': '送钱者',
  'CHAT': '话唠精',
  'BOSS': '领导者',
  'JOKER': '小丑',
  'HUG': '贴贴怪',
  // 内在思维
  'FLOW': '心流者',
  'OH-NO': '哦不人',
  'WHEEL': '跑轮鼠',
  'ZEN': '短暂佛',
  'AHA': '顿悟者',
  'FOG': '脑雾人',
  'FOCUS': '激光眼',
  // 行为驱动
  'YUM': '美食雷达',
  'BUY': '野性消费',
  'PLAY': '头号玩家',
  'GOGO': '行者',
  'CRY': '泪失禁',
  'READ': '宇宙探索者',
  'DO IT': '彻底疯狂',
  'WILD': '野人'
};

const getChineseTag = (tag) => {
  return tagChineseNames[tag] || tag;
};
const questions = ref([]);
const currentIndex = ref(0);
const answers = ref({});
const isLoading = ref(false);
const isCompleted = ref(false);
const report = ref(null);
const hasTodayRecord = ref(false);
const checkTodayRecord = async () => {
 try {
 const response = await moodTestApi.getTodayRecord();
 if (response.data.code === 200 && response.data.data) {
 hasTodayRecord.value = true;
 isCompleted.value = true;
 report.value = response.data.data;
 }
 }
 catch (error) {
 console.error('检查今日记录失败:', error);
 }
};
const getQuestions = async () => {
 isLoading.value = true;
 try {
 const response = await moodTestApi.getDailyQuestions();
 console.log('获取题目成功:', response.data);
 if (response.data.code === 200) {
 questions.value = response.data.data;
 }
 }
 catch (error) {
 console.error('获取题目失败:', error);
 console.error('错误响应:', error.response);
 ElMessage.error(error.response?.data?.message || '获取题目失败');
 }
 finally {
 isLoading.value = false;
 }
};
const selectAnswer = (optionIndex) => {
 answers.value[currentIndex.value + 1] = optionIndex;
};
const nextQuestion = () => {
 if (currentIndex.value < questions.value.length - 1) {
 currentIndex.value++;
 }
};
const prevQuestion = () => {
 if (currentIndex.value > 0) {
 currentIndex.value--;
 }
};
const submitAnswers = async () => {
 if (Object.keys(answers.value).length !== questions.value.length) {
 ElMessage.warning('请完成所有题目');
 return;
 }
 isLoading.value = true;
 try {
 const response = await moodTestApi.submitDailyAnswers({ answers: answers.value });
 if (response.data.code === 200) {
 report.value = response.data.data;
 isCompleted.value = true;
 }
 }
 catch (error) {
 ElMessage.error(error.response?.data?.message || '提交失败');
 }
 finally {
 isLoading.value = false;
 }
};
const currentQuestion = () => {
 return questions.value[currentIndex.value];
};
const getGreeting = () => {
 const hour = new Date().getHours();
 if (hour < 6)
 return '夜深了，是时候和今天的自己聊一聊了';
 if (hour < 9)
 return '早上好，新的一天开始了';
 if (hour < 12)
 return '上午好，今天的进度条已经加载了一小半';
 if (hour < 14)
 return '中午好，该给自己的精神和身体都充个电了';
 if (hour < 18)
 return '下午好，今天的剧情已经过半';
 if (hour < 22)
 return '晚上好，白天的角色可以下班了';
 return '夜深了，是时候和今天的自己聊一聊了';
};
onMounted(async () => {
 await checkTodayRecord();
 if (!hasTodayRecord.value) {
 getQuestions();
 }
});
</script>

<template>
  <div class="daily-container">
    <div class="daily-bg"></div>
    
    <div v-if="!isCompleted" class="daily-content">
      <div class="daily-header">
        <BackButton />
        <span class="header-emoji">📝</span>
        <h1>今日心情测试</h1>
        <p>{{ getGreeting() }}</p>
      </div>

      <div class="progress-bar-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${((currentIndex + 1) / questions.length) * 100}%` }"></div>
        </div>
        <span class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</span>
      </div>

      <div class="question-card">
        <div class="question-text">{{ currentQuestion()?.questionText }}</div>
        
        <div class="options-list">
          <div
            v-for="(option, index) in currentQuestion()?.options"
            :key="index"
            class="option-item"
            :class="{ selected: answers[currentIndex + 1] === index }"
            @click="selectAnswer(index)"
          >
            <span class="option-letter">{{ ['A', 'B', 'C', 'D', 'E'][index] }}</span>
            <span class="option-text">{{ option }}</span>
          </div>
        </div>
      </div>

      <div class="nav-buttons">
        <button 
          class="nav-btn prev-btn" 
          :disabled="currentIndex === 0"
          @click="prevQuestion"
        >
          ← 上一题
        </button>
        <button 
          v-if="currentIndex < questions.length - 1"
          class="nav-btn next-btn"
          :disabled="answers[currentIndex + 1] === undefined"
          @click="nextQuestion"
        >
          下一题 →
        </button>
        <button 
          v-else
          class="nav-btn submit-btn"
          :disabled="isLoading"
          @click="submitAnswers"
        >
          {{ isLoading ? '生成报告中...' : '查看今日心情' }}
        </button>
      </div>
    </div>

    <div v-else class="report-content">
      <div class="report-header">
        <span class="report-date">{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
        <h1>今日情绪日报</h1>
        <p>{{ getGreeting() }}</p>
      </div>

      <div class="mhi-card">
        <div class="mhi-circle">
          <svg viewBox="0 0 120 120">
            <circle class="mhi-ring-bg" cx="60" cy="60" r="54" />
            <circle
              class="mhi-ring-progress"
              cx="60"
              cy="60"
              r="54"
              :stroke-dasharray="`${report.mhi * 3.39} 339`"
            />
          </svg>
          <div class="mhi-center">
            <span class="mhi-value">{{ report.mhi?.toFixed(0) }}</span>
            <span class="mhi-label">分</span>
          </div>
        </div>
        <div class="mhi-info">
          <span class="mhi-level">{{ getChineseTag(report.mainTag) }}</span>
          <div class="main-tag">
            <span class="tag-icon">🏷️</span>
            <span class="tag-name">{{ report.mainTagDesc || '暂无描述' }}</span>
          </div>
        </div>
      </div>

      <div class="dimensions-row">
        <div class="dimension-item">
          <span class="dim-icon">❤️</span>
          <span class="dim-label">情绪</span>
          <span class="dim-value" :class="report.dimensions?.EV > 3 ? 'high' : report.dimensions?.EV > 2 ? 'mid' : 'low'">
            {{ report.dimensions?.EV?.toFixed(1) }}
          </span>
        </div>
        <div class="dimension-item">
          <span class="dim-icon">⚡</span>
          <span class="dim-label">能量</span>
          <span class="dim-value" :class="report.dimensions?.ME > 3 ? 'high' : report.dimensions?.ME > 2 ? 'mid' : 'low'">
            {{ report.dimensions?.ME?.toFixed(1) }}
          </span>
        </div>
        <div class="dimension-item">
          <span class="dim-icon">👥</span>
          <span class="dim-label">社交</span>
          <span class="dim-value" :class="report.dimensions?.SC > 3 ? 'high' : report.dimensions?.SC > 2 ? 'mid' : 'low'">
            {{ report.dimensions?.SC?.toFixed(1) }}
          </span>
        </div>
        <div class="dimension-item">
          <span class="dim-icon">🧠</span>
          <span class="dim-label">思维</span>
          <span class="dim-value" :class="report.dimensions?.CC > 3 ? 'high' : report.dimensions?.CC > 2 ? 'mid' : 'low'">
            {{ report.dimensions?.CC?.toFixed(1) }}
          </span>
        </div>
      </div>

      <div class="sub-tags">
        <span class="sub-tag" v-for="(tag, index) in report.subTags" :key="index">{{ getChineseTag(tag) }}</span>
      </div>

      <div class="insight-card">
        <span class="insight-icon">💭</span>
        <p>{{ report.insight }}</p>
      </div>

      <div class="suggestion-card">
        <span class="suggestion-icon">💡</span>
        <p>{{ report.suggestion }}</p>
      </div>

      <div class="action-buttons">
        <button class="action-btn secondary" @click="$router.push('/mood-calendar')">
          📅 查看历史
        </button>
      </div>
      <div class="report-back-button">
        <BackButton />
      </div>
    </div>
  </div>
</template>

<style scoped>
.daily-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
  position: relative;
}

.daily-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 30% 20%, rgba(255, 183, 77, 0.15) 0%, transparent 30%),
    radial-gradient(circle at 70% 80%, rgba(255, 215, 0, 0.1) 0%, transparent 30%);
  pointer-events: none;
}

.daily-content {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.daily-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-emoji {
  font-size: 40px;
  display: block;
  margin-bottom: 8px;
}

.daily-header h1 {
  font-size: 26px;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.daily-header p {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.progress-bar-container {
  margin-bottom: 20px;
}

.progress-bar {
  height: 8px;
  background: #FFE0B2;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF8F00, #FFC107);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  display: block;
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #5D4037;
  font-weight: 500;
}

.question-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(255, 183, 77, 0.15);
}

.question-text {
  font-size: 18px;
  color: #5D4037;
  line-height: 1.6;
  margin-bottom: 20px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #FFF8E7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.option-item:hover {
  background: #FFE0B2;
}

.option-item.selected {
  background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
  border-color: #FF8F00;
}

.option-letter {
  width: 28px;
  height: 28px;
  background: #FF8F00;
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.option-text {
  font-size: 15px;
  color: #5D4037;
  line-height: 1.5;
}

.nav-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: center;
}

.nav-btn {
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.prev-btn {
  background: #FFF8E7;
  color: #5D4037;
}

.prev-btn:hover:not(:disabled) {
  background: #FFE0B2;
}

.next-btn {
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
}

.next-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.submit-btn {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
}

.report-content {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.report-header {
  text-align: center;
  margin-bottom: 24px;
}

.report-date {
  font-size: 14px;
  color: #FF8F00;
  font-weight: 500;
}

.report-header h1 {
  font-size: 28px;
  color: #5D4037;
  margin: 8px 0 8px 0;
}

.report-header p {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.mhi-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(255, 183, 77, 0.15);
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.mhi-circle {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.mhi-circle svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.mhi-ring-bg {
  fill: none;
  stroke: #FFE0B2;
  stroke-width: 10;
}

.mhi-ring-progress {
  fill: none;
  stroke: linear-gradient(90deg, #FF8F00, #FFC107);
  stroke-width: 10;
  stroke-linecap: round;
  stroke: #FF8F00;
}

.mhi-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.mhi-value {
  font-size: 36px;
  font-weight: 800;
  color: #FF8F00;
  display: block;
}

.mhi-label {
  font-size: 12px;
  color: #9E9E9E;
}

.mhi-info {
  flex: 1;
}

.mhi-level {
  font-size: 20px;
  font-weight: 700;
  color: #5D4037;
  display: block;
  margin-bottom: 8px;
}

.main-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #FFF8E7;
  padding: 6px 12px;
  border-radius: 20px;
}

.tag-icon {
  font-size: 14px;
}

.tag-name {
  font-size: 14px;
  font-weight: 600;
  color: #5D4037;
}

.dimensions-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.dimension-item {
  flex: 1;
  background: white;
  border-radius: 16px;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
}

.dim-icon {
  font-size: 24px;
  display: block;
  margin-bottom: 4px;
}

.dim-label {
  font-size: 12px;
  color: #9E9E9E;
  display: block;
  margin-bottom: 4px;
}

.dim-value {
  font-size: 20px;
  font-weight: 700;
}

.dim-value.high { color: #4CAF50; }
.dim-value.mid { color: #FF8F00; }
.dim-value.low { color: #E57373; }

.sub-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.sub-tag {
  background: linear-gradient(135deg, #FFF8E7, #FFE4C4);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #5D4037;
  font-weight: 500;
}

.insight-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.insight-icon {
  font-size: 28px;
}

.insight-card p {
  font-size: 15px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0;
}

.suggestion-card {
  background: linear-gradient(135deg, #FFF8E7, #FFE4C4);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.suggestion-icon {
  font-size: 28px;
}

.suggestion-card p {
  font-size: 15px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn.secondary {
  background: #FFF8E7;
  color: #5D4037;
}

.action-btn.secondary:hover {
  background: #FFE0B2;
}

.action-btn.primary {
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.report-back-button {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
