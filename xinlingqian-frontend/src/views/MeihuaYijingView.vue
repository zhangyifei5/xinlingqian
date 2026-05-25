<template>
  <div class="meihua-container">
    <BackButton />
    
    <div class="meihua-header">
      <div class="header-icon">🌸</div>
      <h1>梅花易数</h1>
      <p class="subtitle">今日运势测算</p>
    </div>

    <div class="divination-area" v-if="!hasResult">
      <div class="divination-card">
        <div class="divination-icon">🔮</div>
        <h3>开始测算今日运势</h3>
        <p class="divination-desc">点击下方按钮，为您测算今日运势</p>
        <button class="divinate-btn" @click="startDivination" :disabled="isDivinating">
          {{ isDivinating ? '测算中...' : '开始测算' }}
        </button>
      </div>
    </div>

    <div class="result-area" v-else>
      <div class="result-card">
        <div class="hexagram-section">
          <div class="hexagram-box">
            <div class="hexagram-icon">{{ hexagram.icon }}</div>
            <div class="hexagram-name">{{ hexagram.name }}</div>
            <div class="hexagram-symbol">{{ hexagram.symbol }}</div>
          </div>
        </div>

        <div class="fortune-section">
          <div class="fortune-header">
            <span class="fortune-label">今日运势</span>
            <span :class="['fortune-level', fortuneLevel.class]">{{ fortuneLevel.text }}</span>
          </div>
          <div class="fortune-bar">
            <div class="fortune-fill" :style="{ width: fortuneLevel.percent + '%' }"></div>
          </div>
        </div>

        <div class="details-section">
          <div class="detail-card">
            <span class="detail-icon">💝</span>
            <div class="detail-content">
              <span class="detail-title">感情运势</span>
              <span class="detail-value">{{ result.love }}</span>
            </div>
          </div>
          <div class="detail-card">
            <span class="detail-icon">💰</span>
            <div class="detail-content">
              <span class="detail-title">财运</span>
              <span class="detail-value">{{ result.wealth }}</span>
            </div>
          </div>
          <div class="detail-card">
            <span class="detail-icon">💼</span>
            <div class="detail-content">
              <span class="detail-title">事业运势</span>
              <span class="detail-value">{{ result.career }}</span>
            </div>
          </div>
          <div class="detail-card">
            <span class="detail-icon">💪</span>
            <div class="detail-content">
              <span class="detail-title">健康运势</span>
              <span class="detail-value">{{ result.health }}</span>
            </div>
          </div>
        </div>

        <div class="analysis-section">
          <h4 class="analysis-title">卦辞解读</h4>
          <p class="analysis-content">{{ result.analysis }}</p>
        </div>

        <div class="advice-section">
          <h4 class="advice-title">今日建议</h4>
          <ul class="advice-list">
            <li v-for="(advice, index) in result.advice" :key="index">
              <span class="advice-dot">•</span>
              <span>{{ advice }}</span>
            </li>
          </ul>
        </div>

        <div class="lucky-section">
          <div class="lucky-item">
            <span class="lucky-label">幸运颜色</span>
            <span class="lucky-value">{{ result.luckyColor }}</span>
          </div>
          <div class="lucky-item">
            <span class="lucky-label">幸运数字</span>
            <span class="lucky-value">{{ result.luckyNumber }}</span>
          </div>
          <div class="lucky-item">
            <span class="lucky-label">幸运方位</span>
            <span class="lucky-value">{{ result.luckyDirection }}</span>
          </div>
        </div>

        <button class="refresh-btn" @click="resetDivination">
          🔄 重新测算
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import BackButton from '../components/BackButton.vue'

const isDivinating = ref(false)
const hasResult = ref(false)

const hexagram = ref({
  icon: '',
  name: '',
  symbol: ''
})

const result = ref({
  love: '',
  wealth: '',
  career: '',
  health: '',
  analysis: '',
  advice: [],
  luckyColor: '',
  luckyNumber: '',
  luckyDirection: ''
})

const hexagrams = [
  { icon: '乾', name: '乾为天', symbol: '☰', fortune: 90 },
  { icon: '坤', name: '坤为地', symbol: '☷', fortune: 80 },
  { icon: '屯', name: '水雷屯', symbol: '☳', fortune: 60 },
  { icon: '需', name: '水天需', symbol: '☰', fortune: 75 },
  { icon: '讼', name: '天水讼', symbol: '☱', fortune: 50 },
  { icon: '师', name: '地水师', symbol: '☵', fortune: 70 },
  { icon: '比', name: '水地比', symbol: '☷', fortune: 85 },
  { icon: '小畜', name: '风天小畜', symbol: '☴', fortune: 72 },
  { icon: '履', name: '天泽履', symbol: '☰', fortune: 78 },
  { icon: '泰', name: '地天泰', symbol: '☰', fortune: 95 },
  { icon: '否', name: '天地否', symbol: '☷', fortune: 45 },
  { icon: '同人', name: '天火同人', symbol: '☰', fortune: 82 },
  { icon: '大有', name: '火天大有', symbol: '☰', fortune: 92 },
  { icon: '谦', name: '地山谦', symbol: '☶', fortune: 88 },
  { icon: '豫', name: '雷地豫', symbol: '☷', fortune: 76 },
  { icon: '随', name: '泽雷随', symbol: '☳', fortune: 74 }
]

const fortuneTexts = {
  love: ['感情顺遂，桃花朵朵', '感情稳定，宜主动沟通', '略有波折，保持耐心', '感情平淡，静待时机', '桃花运旺，把握机会'],
  wealth: ['财运亨通，财源广进', '小有斩获，谨慎投资', '财运平平，守财为上', '偏财得利，把握机会', '财星高照，适合理财'],
  career: ['事业顺利，步步高升', '工作稳定，稳步前进', '事业起伏，保持定力', '机遇多多，勇于尝试', '事业发展，贵人相助'],
  health: ['身体健康，精力充沛', '身体尚可，注意休息', '略有不适，及时调理', '精神饱满，状态良好', '养生有道，身心健康']
}

const analysisTexts = [
  '此卦为吉，诸事顺遂。今日宜积极进取，把握良机。贵人相助，事半功倍。',
  '此卦显示，今日运势平稳，虽有小波折但无碍大局。保持平常心，顺其自然。',
  '卦象显示今日机遇与挑战并存，需谨慎行事，三思而后行。',
  '吉兆显现，今日诸事皆宜。尤其利于人际交往与事业发展。',
  '卦象平和，今日宜守不宜进，稳扎稳打为上策。'
]

const adviceLists = [
  ['今日宜主动与人沟通，增进人际关系', '注意劳逸结合，保持良好状态', '把握机会，勇于尝试新事物', '遇事多听取他人意见'],
  ['保持积极心态，笑对人生', '注意饮食健康，忌暴饮暴食', '今日适合学习和自我提升', '谨慎理财，不宜冲动投资'],
  ['今日贵人运佳，多与人交流', '注意交通安全，谨慎出行', '保持耐心，静待时机', '不宜做重大决策'],
  ['今日运势上扬，把握良机', '注意情绪管理，保持平和', '适合开展新计划、新项目', '多行善事，积累福报'],
  ['今日宜静不宜动，修身养性', '多陪伴家人，增进感情', '避免与人争执，退一步海阔天空', '注意休息，养精蓄锐']
]

const luckyColors = ['红色', '黄色', '蓝色', '绿色', '紫色', '白色', '金色', '橙色']
const luckyNumbers = ['1', '3', '5', '6', '8', '9']
const luckyDirections = ['东方', '南方', '西方', '北方', '东南', '西北']

const fortuneLevel = computed(() => {
  const hex = hexagram.value
  if (!hex.name) return { text: '', class: '', percent: 0 }
  
  const fortune = hex.fortune
  if (fortune >= 85) return { text: '大吉', class: 'great', percent: fortune }
  if (fortune >= 70) return { text: '吉', class: 'good', percent: fortune }
  if (fortune >= 55) return { text: '平', class: 'normal', percent: fortune }
  if (fortune >= 40) return { text: '小凶', class: 'bad', percent: fortune }
  return { text: '大凶', class: 'worse', percent: fortune }
})

const getRandom = (arr) => arr[Math.floor(Math.random() * arr.length)]

const startDivination = async () => {
  isDivinating.value = true
  
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  const selectedHex = getRandom(hexagrams)
  hexagram.value = selectedHex
  
  result.value = {
    love: getRandom(fortuneTexts.love),
    wealth: getRandom(fortuneTexts.wealth),
    career: getRandom(fortuneTexts.career),
    health: getRandom(fortuneTexts.health),
    analysis: getRandom(analysisTexts),
    advice: getRandom(adviceLists),
    luckyColor: getRandom(luckyColors),
    luckyNumber: getRandom(luckyNumbers),
    luckyDirection: getRandom(luckyDirections)
  }
  
  hasResult.value = true
  isDivinating.value = false
}

const resetDivination = () => {
  hasResult.value = false
  hexagram.value = { icon: '', name: '', symbol: '' }
  result.value = {
    love: '',
    wealth: '',
    career: '',
    health: '',
    analysis: '',
    advice: [],
    luckyColor: '',
    luckyNumber: '',
    luckyDirection: ''
  }
}

onMounted(() => {
})
</script>

<style scoped>
.meihua-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;
}

.meihua-header {
  text-align: center;
  padding: 48px 24px;
  background: linear-gradient(135deg, #8B4513 0%, #D2691E 50%, #CD853F 100%);
  border-radius: 24px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(139, 69, 19, 0.3);
}

.header-icon {
  font-size: 72px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

.meihua-header h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.subtitle {
  font-size: 15px;
  opacity: 0.95;
  margin: 0;
  font-weight: 400;
}

.divination-area {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.divination-card {
  background: white;
  border-radius: 24px;
  padding: 48px 32px;
  text-align: center;
  box-shadow: 0 12px 40px rgba(255, 183, 77, 0.2);
  max-width: 320px;
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.divination-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.divination-card h3 {
  font-size: 20px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 12px 0;
}

.divination-desc {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0 0 24px 0;
}

.divinate-btn {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #8B4513 0%, #D2691E 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
}

.divinate-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(139, 69, 19, 0.5);
}

.divinate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.result-area {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.result-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(255, 183, 77, 0.15);
}

.hexagram-section {
  text-align: center;
  padding: 24px 0;
  border-bottom: 1px solid #F5F5F5;
}

.hexagram-box {
  display: inline-block;
  padding: 24px 48px;
  background: linear-gradient(135deg, #FFF8E7, #FFE4C4);
  border-radius: 20px;
  border: 2px solid #D2691E;
}

.hexagram-icon {
  font-size: 48px;
  color: #8B4513;
  margin-bottom: 12px;
  display: block;
}

.hexagram-name {
  font-size: 24px;
  font-weight: 700;
  color: #5D4037;
  margin-bottom: 8px;
  display: block;
}

.hexagram-symbol {
  font-size: 36px;
  color: #D2691E;
}

.fortune-section {
  padding: 20px 0;
  border-bottom: 1px solid #F5F5F5;
}

.fortune-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.fortune-label {
  font-size: 14px;
  color: #9E9E9E;
}

.fortune-level {
  font-size: 18px;
  font-weight: 700;
  padding: 4px 16px;
  border-radius: 20px;
}

.fortune-level.great {
  color: #4CAF50;
  background: #E8F5E9;
}

.fortune-level.good {
  color: #FF8F00;
  background: #FFF8E7;
}

.fortune-level.normal {
  color: #9E9E9E;
  background: #F5F5F5;
}

.fortune-level.bad {
  color: #FF5722;
  background: #FFF3E0;
}

.fortune-level.worse {
  color: #F44336;
  background: #FFEBEE;
}

.fortune-bar {
  height: 8px;
  background: #F5F5F5;
  border-radius: 4px;
  overflow: hidden;
}

.fortune-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF8F00, #FFC107);
  border-radius: 4px;
  transition: width 1s ease;
}

.details-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #F5F5F5;
}

.detail-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #FFF8E7;
  border-radius: 14px;
}

.detail-icon {
  font-size: 24px;
}

.detail-content {
  display: flex;
  flex-direction: column;
}

.detail-title {
  font-size: 12px;
  color: #9E9E9E;
}

.detail-value {
  font-size: 13px;
  font-weight: 600;
  color: #5D4037;
  margin-top: 2px;
}

.analysis-section {
  padding: 20px 0;
  border-bottom: 1px solid #F5F5F5;
}

.analysis-title {
  font-size: 16px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 12px 0;
}

.analysis-content {
  font-size: 14px;
  color: #616161;
  line-height: 1.7;
  margin: 0;
  padding: 16px;
  background: #F5F5F5;
  border-radius: 12px;
}

.advice-section {
  padding: 20px 0;
  border-bottom: 1px solid #F5F5F5;
}

.advice-title {
  font-size: 16px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 12px 0;
}

.advice-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.advice-list li {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  background: #E8F5E9;
  border-radius: 10px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #5D4037;
}

.advice-list li:last-child {
  margin-bottom: 0;
}

.advice-dot {
  color: #4CAF50;
  font-weight: bold;
}

.lucky-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 20px 0;
}

.lucky-item {
  text-align: center;
  padding: 16px 8px;
  background: linear-gradient(135deg, #FFF8E7, #FFE0B2);
  border-radius: 12px;
}

.lucky-label {
  font-size: 11px;
  color: #9E9E9E;
  display: block;
  margin-bottom: 6px;
}

.lucky-value {
  font-size: 15px;
  font-weight: 700;
  color: #8B4513;
}

.refresh-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 143, 0, 0.4);
}

@media (max-width: 480px) {
  .details-section {
    grid-template-columns: 1fr;
  }
  
  .lucky-section {
    grid-template-columns: 1fr;
  }
  
  .meihua-header {
    padding: 32px 20px;
  }
  
  .hexagram-box {
    padding: 20px 32px;
  }
}
</style>