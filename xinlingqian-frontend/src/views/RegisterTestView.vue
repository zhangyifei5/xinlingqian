<script setup>import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { moodTestApi } from '../api';
const questions = ref([]);
const currentIndex = ref(0);
const answers = ref({});
const isLoading = ref(false);
const isCompleted = ref(false);
const report = ref(null);
const getQuestions = async () => {
 isLoading.value = true;
 try {
 const response = await moodTestApi.getRegisterQuestions();
 if (response.data.code === 200) {
 questions.value = response.data.data;
 }
 }
 catch (error) {
 ElMessage.error('获取题目失败');
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
 const response = await moodTestApi.submitRegisterAnswers({ answers: answers.value });
 if (response.data.code === 200) {
 report.value = response.data.data;
 isCompleted.value = true;
 }
 }
 catch (error) {
 ElMessage.error('提交失败');
 }
 finally {
 isLoading.value = false;
 }
};
const currentQuestion = () => {
 return questions.value[currentIndex.value];
};
onMounted(() => {
 getQuestions();
});
</script>

<template>
  <div class="test-container">
    <div class="test-bg"></div>
    
    <div v-if="!isCompleted" class="test-content">
      <div class="test-header">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${((currentIndex + 1) / questions.length) * 100}%` }"></div>
        </div>
        <span class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</span>
      </div>

      <div class="question-card">
        <div class="question-number">第 {{ currentIndex + 1 }} 题</div>
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
          {{ isLoading ? '提交中...' : '提交答案' }}
        </button>
      </div>
    </div>

    <div v-else class="report-content">
      <div class="report-header">
        <span class="report-icon">✨</span>
        <h1>你的心灵底片</h1>
        <p>初次见面，这是你情绪世界的原色</p>
      </div>

      <div class="personality-card">
        <div class="personality-icon">{{ report.personalityType?.split(' ')[1] }}</div>
        <div class="personality-info">
          <h2>{{ report.personalityType?.split(' ')[0] }}</h2>
          <p>{{ report.personalityDesc }}</p>
        </div>
      </div>

      <div class="dimensions-grid">
        <div class="dimension-card">
          <span class="dimension-label">情绪效价</span>
          <div class="dimension-bar">
            <div class="dimension-fill ev" :style="{ width: `${report.dimensions?.EV * 20}%` }"></div>
          </div>
          <span class="dimension-value">{{ report.dimensions?.EV?.toFixed(1) }}</span>
        </div>
        <div class="dimension-card">
          <span class="dimension-label">心理能量</span>
          <div class="dimension-bar">
            <div class="dimension-fill me" :style="{ width: `${report.dimensions?.ME * 20}%` }"></div>
          </div>
          <span class="dimension-value">{{ report.dimensions?.ME?.toFixed(1) }}</span>
        </div>
        <div class="dimension-card">
          <span class="dimension-label">社会连接</span>
          <div class="dimension-bar">
            <div class="dimension-fill sc" :style="{ width: `${report.dimensions?.SC * 20}%` }"></div>
          </div>
          <span class="dimension-value">{{ report.dimensions?.SC?.toFixed(1) }}</span>
        </div>
        <div class="dimension-card">
          <span class="dimension-label">认知清晰</span>
          <div class="dimension-bar">
            <div class="dimension-fill cc" :style="{ width: `${report.dimensions?.CC * 20}%` }"></div>
          </div>
          <span class="dimension-value">{{ report.dimensions?.CC?.toFixed(1) }}</span>
        </div>
      </div>

      <div class="suggestion-card">
        <span class="suggestion-icon">💡</span>
        <p>{{ report.suggestion }}</p>
      </div>

      <button class="finish-btn" @click="$router.push('/')">
        进入首页 →
      </button>
    </div>
  </div>
</template>

<style scoped>
.test-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
  position: relative;
}

.test-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(255, 183, 77, 0.15) 0%, transparent 30%),
    radial-gradient(circle at 80% 70%, rgba(255, 215, 0, 0.1) 0%, transparent 30%);
  pointer-events: none;
}

.test-content {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.test-header {
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

.question-number {
  font-size: 12px;
  color: #FF8F00;
  font-weight: 600;
  margin-bottom: 12px;
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

.option-item.selected .option-letter {
  background: #E65100;
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

.prev-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.next-btn {
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
}

.next-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.next-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.report-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.report-header h1 {
  font-size: 28px;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.report-header p {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.personality-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(255, 183, 77, 0.15);
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
}

.personality-icon {
  font-size: 56px;
}

.personality-info h2 {
  font-size: 20px;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.personality-info p {
  font-size: 14px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0;
}

.dimensions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.dimension-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
}

.dimension-label {
  font-size: 12px;
  color: #9E9E9E;
  display: block;
  margin-bottom: 8px;
}

.dimension-bar {
  height: 6px;
  background: #FFE0B2;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.dimension-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.dimension-fill.ev { background: linear-gradient(90deg, #FF8F00, #FFC107); }
.dimension-fill.me { background: linear-gradient(90deg, #4CAF50, #8BC34A); }
.dimension-fill.sc { background: linear-gradient(90deg, #2196F3, #64B5F6); }
.dimension-fill.cc { background: linear-gradient(90deg, #9C27B0, #CE93D8); }

.dimension-value {
  font-size: 18px;
  font-weight: 700;
  color: #5D4037;
}

.suggestion-card {
  background: linear-gradient(135deg, #FFF8E7, #FFE4C4);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
}

.suggestion-icon {
  font-size: 28px;
}

.suggestion-card p {
  font-size: 14px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0;
}

.finish-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.finish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 143, 0, 0.3);
}
</style>
