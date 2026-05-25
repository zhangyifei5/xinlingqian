<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { moodTestApi } from '../api';
import BackButton from '../components/BackButton.vue';

const route = useRoute();
const router = useRouter();
const report = ref(null);
const isLoading = ref(true);
const isOtherUser = ref(false);

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

const dateStr = computed(() => route.params.date);
const userId = computed(() => route.query.userId ? parseInt(route.query.userId) : null);

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const parts = dateStr.split('-');
  return `${parts[1]}月${parts[2]}日`;
};

const getMhiColor = (mhi) => {
  if (!mhi) return '#E0E0E0';
  if (mhi >= 80) return '#FF69B4';
  if (mhi >= 65) return '#FFB6C1';
  if (mhi >= 50) return '#FFDAB9';
  if (mhi >= 35) return '#FFE4C4';
  return '#FFB347';
};

const goBack = () => {
  if (userId.value) {
    router.push('/community');
  } else {
    router.push('/mood-calendar');
  }
};

const loadReport = async () => {
  if (!dateStr.value) {
    goBack();
    return;
  }
  
  try {
    if (userId.value) {
      const response = await moodTestApi.getHistoryRecord(userId.value, dateStr.value);
      if (response.data.code === 200 && response.data.data) {
        const record = response.data.data;
        isOtherUser.value = true;
        report.value = {
          mhi: record.mhi,
          mainTag: record.mainTag,
          mainTagCn: getChineseTag(record.mainTag),
          dimensions: {
            EV: record.evScore,
            ME: record.meScore,
            SC: record.scScore,
            CC: record.ccScore
          },
          baselines: {
            EV: record.evBaseline,
            ME: record.meBaseline,
            SC: record.scBaseline,
            CC: record.ccBaseline
          },
          insight: record.insight,
          suggestion: record.suggestion,
          subTags: record.subTags ? record.subTags.split(',').map(t => t.trim()) : []
        };
      } else {
        report.value = null;
      }
    } else {
      const response = await moodTestApi.getRecordByDate(dateStr.value);
      if (response.data.code === 200) {
        report.value = response.data.data;
      } else {
        report.value = null;
      }
    }
  } catch (error) {
    console.error('获取历史报告失败:', error);
    report.value = null;
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadReport();
});
</script>

<template>
  <div class="history-report-page">
    <div class="page-header">
      <BackButton />
      <h1>{{ isOtherUser ? '👀 Ta的心情报告' : '📝 历史心情报告' }}</h1>
    </div>
    
    <div v-if="isLoading" class="loading">
      <span class="loading-icon">⏳</span>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="!report" class="no-record">
      <span class="no-record-icon">📭</span>
      <p>该日期暂无心情记录</p>
      <button class="back-btn" @click="goBack">{{ isOtherUser ? '返回社区' : '返回日历' }}</button>
    </div>
    
    <div v-else class="report-content">
      <div class="date-card">
        <span class="date-icon">📅</span>
        <span class="date-text">{{ formatDate(dateStr) }}</span>
      </div>
      
      <div class="mhi-card">
        <div class="mhi-score">
          <span class="score-value">{{ report.mhi?.toFixed(0) }}</span>
          <span class="score-label">分</span>
        </div>
        <span class="mhi-tag" :style="{ color: getMhiColor(report.mhi) }">
          {{ report.mainTagCn || getChineseTag(report.mainTag) }}
        </span>
      </div>
      
      <div class="dimensions-section">
        <h3>🎯 四维指标</h3>
        <div class="dimensions-grid">
          <div class="dimension-card">
            <div class="dim-header">
              <span class="dim-icon">❤️</span>
              <span class="dim-name">情绪效价</span>
            </div>
            <div class="dim-score">
              <span class="dim-value">{{ report.dimensions?.EV?.toFixed(1) }}</span>
              <span class="dim-unit">/5</span>
            </div>
            <div class="dim-bar">
              <div class="dim-fill" :style="{ width: (report.dimensions?.EV * 20) + '%', backgroundColor: '#FF69B4' }"></div>
            </div>
            <div class="dim-baseline">基线: {{ report.baselines?.EV?.toFixed(1) }}</div>
          </div>
          
          <div class="dimension-card">
            <div class="dim-header">
              <span class="dim-icon">⚡</span>
              <span class="dim-name">心理能量</span>
            </div>
            <div class="dim-score">
              <span class="dim-value">{{ report.dimensions?.ME?.toFixed(1) }}</span>
              <span class="dim-unit">/5</span>
            </div>
            <div class="dim-bar">
              <div class="dim-fill" :style="{ width: (report.dimensions?.ME * 20) + '%', backgroundColor: '#FFD700' }"></div>
            </div>
            <div class="dim-baseline">基线: {{ report.baselines?.ME?.toFixed(1) }}</div>
          </div>
          
          <div class="dimension-card">
            <div class="dim-header">
              <span class="dim-icon">👥</span>
              <span class="dim-name">社会连接</span>
            </div>
            <div class="dim-score">
              <span class="dim-value">{{ report.dimensions?.SC?.toFixed(1) }}</span>
              <span class="dim-unit">/5</span>
            </div>
            <div class="dim-bar">
              <div class="dim-fill" :style="{ width: (report.dimensions?.SC * 20) + '%', backgroundColor: '#90EE90' }"></div>
            </div>
            <div class="dim-baseline">基线: {{ report.baselines?.SC?.toFixed(1) }}</div>
          </div>
          
          <div class="dimension-card">
            <div class="dim-header">
              <span class="dim-icon">🧠</span>
              <span class="dim-name">认知清晰</span>
            </div>
            <div class="dim-score">
              <span class="dim-value">{{ report.dimensions?.CC?.toFixed(1) }}</span>
              <span class="dim-unit">/5</span>
            </div>
            <div class="dim-bar">
              <div class="dim-fill" :style="{ width: (report.dimensions?.CC * 20) + '%', backgroundColor: '#87CEEB' }"></div>
            </div>
            <div class="dim-baseline">基线: {{ report.baselines?.CC?.toFixed(1) }}</div>
          </div>
        </div>
      </div>
      
      <div v-if="report.insight" class="insight-card">
        <div class="card-icon">💭</div>
        <div class="card-content">
          <h4>今日洞察</h4>
          <p>{{ report.insight }}</p>
        </div>
      </div>
      
      <div v-if="report.suggestion" class="suggestion-card">
        <div class="card-icon">💡</div>
        <div class="card-content">
          <h4>行动建议</h4>
          <p>{{ report.suggestion }}</p>
        </div>
      </div>
      
      <div v-if="report.subTags && report.subTags.length > 0" class="subtags-card">
        <h4>🏷️ 辅助标签</h4>
        <div class="subtags-list">
          <span v-for="tag in report.subTags" :key="tag" class="subtag">
            {{ getChineseTag(tag) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-report-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.back-btn {
  padding: 8px 16px;
  background: #FFE4E1;
  border: none;
  border-radius: 20px;
  color: #FF69B4;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.back-btn:hover {
  background: #FFB6C1;
}

.page-header h1 {
  font-size: 24px;
  color: #5D4037;
  margin: 0;
}

.loading, .no-record {
  text-align: center;
  padding: 50px 20px;
}

.loading-icon, .no-record-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 15px;
}

.loading p, .no-record p {
  font-size: 16px;
  color: #9E9E9E;
}

.report-content {
  max-width: 400px;
  margin: 0 auto;
}

.date-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 15px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.date-icon {
  font-size: 24px;
}

.date-text {
  font-size: 18px;
  font-weight: bold;
  color: #5D4037;
}

.mhi-card {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #FFF0F5 0%, #FFE4E1 100%);
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.mhi-score {
  margin-bottom: 10px;
}

.score-value {
  font-size: 56px;
  font-weight: bold;
  color: #FF69B4;
}

.score-label {
  font-size: 18px;
  color: #9E9E9E;
  margin-left: 8px;
}

.mhi-tag {
  font-size: 24px;
  font-weight: bold;
}

.dimensions-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.dimensions-section h3 {
  font-size: 18px;
  color: #5D4037;
  margin: 0 0 15px 0;
}

.dimensions-grid {
  display: grid;
  gap: 15px;
}

.dimension-card {
  background: #FAFAFA;
  border-radius: 12px;
  padding: 15px;
}

.dim-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.dim-icon {
  font-size: 20px;
}

.dim-name {
  font-size: 14px;
  color: #5D4037;
  font-weight: 500;
}

.dim-score {
  margin-bottom: 8px;
}

.dim-value {
  font-size: 24px;
  font-weight: bold;
  color: #5D4037;
}

.dim-unit {
  font-size: 14px;
  color: #9E9E9E;
}

.dim-bar {
  height: 8px;
  background: #E0E0E0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.dim-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.dim-baseline {
  font-size: 12px;
  color: #9E9E9E;
}

.insight-card, .suggestion-card {
  display: flex;
  gap: 15px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.insight-card {
  background: rgba(21, 101, 192, 0.05);
}

.suggestion-card {
  background: rgba(46, 125, 50, 0.05);
}

.card-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.card-content h4 {
  font-size: 14px;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.card-content p {
  font-size: 14px;
  color: #5D4037;
  line-height: 1.6;
  margin: 0;
}

.subtags-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.subtags-card h4 {
  font-size: 14px;
  color: #5D4037;
  margin: 0 0 12px 0;
}

.subtags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.subtag {
  padding: 6px 12px;
  background: #FFF0F5;
  border-radius: 15px;
  font-size: 12px;
  color: #FF69B4;
}
</style>
