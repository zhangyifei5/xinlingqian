<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { moodTestApi } from '../api';
import BackButton from '../components/BackButton.vue';

const router = useRouter();
const currentDate = ref(new Date());
const moodRecords = ref({});
const isLoading = ref(false);

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

const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());

const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', 
                    '七月', '八月', '九月', '十月', '十一月', '十二月'];
const weekDays = ['日', '一', '二', '三', '四', '五', '六'];

const daysInMonth = computed(() => {
  const year = currentYear.value;
  const month = currentMonth.value;
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const days = [];
  
  const startDay = firstDay.getDay();
  for (let i = 0; i < startDay; i++) {
    days.push(null);
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
    const record = moodRecords.value[dateStr];
    days.push({
      day: i,
      dateStr: dateStr,
      record: record,
      hasRecord: !!record
    });
  }
  
  return days;
});

const prevMonth = () => {
  const year = currentYear.value;
  const month = currentMonth.value;
  if (month === 0) {
    currentDate.value = new Date(year - 1, 11, 1);
  } else {
    currentDate.value = new Date(year, month - 1, 1);
  }
  loadMoodRecords();
};

const nextMonth = () => {
  const year = currentYear.value;
  const month = currentMonth.value;
  if (month === 11) {
    currentDate.value = new Date(year + 1, 0, 1);
  } else {
    currentDate.value = new Date(year, month + 1, 1);
  }
  loadMoodRecords();
};

const selectDate = (day) => {
  if (!day || !day.hasRecord) return;
  router.push(`/history-report/${day.dateStr}`);
};

const getMhiColor = (mhi) => {
  if (!mhi) return '#E0E0E0';
  if (mhi >= 80) return '#FF69B4';
  if (mhi >= 65) return '#FFB6C1';
  if (mhi >= 50) return '#FFDAB9';
  if (mhi >= 35) return '#FFE4C4';
  return '#FFB347';
};

const getMhiLevel = (mhi) => {
  if (!mhi) return '';
  if (mhi >= 80) return '优秀';
  if (mhi >= 65) return '良好';
  if (mhi >= 50) return '普通';
  if (mhi >= 35) return '注意';
  return '预警';
};

const loadMoodRecords = async () => {
  isLoading.value = true;
  try {
    const year = currentYear.value;
    const month = currentMonth.value;
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const response = await moodTestApi.getHistoryRecords(daysInMonth + 14);
    if (response.data.code === 200) {
      const records = response.data.data;
      const recordMap = {};
      records.forEach(record => {
        if (record.date) {
          recordMap[record.date] = record;
        }
      });
      moodRecords.value = recordMap;
    }
  } catch (error) {
    console.error('获取心情记录失败:', error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadMoodRecords();
});
</script>

<template>
  <div class="calendar-page">
    <div class="page-header">
      <BackButton />
      <h1>📅 历史心情日历</h1>
      <p>点击有记录的日期查看详细报告</p>
    </div>
    
    <div class="calendar-container">
      <div class="calendar-header">
        <button class="nav-btn" @click="prevMonth">‹</button>
        <div class="month-year">
          <span>{{ monthNames[currentMonth] }} {{ currentYear }}</span>
        </div>
        <button class="nav-btn" @click="nextMonth">›</button>
      </div>
      
      <div class="calendar-weekdays">
        <span v-for="day in weekDays" :key="day" class="weekday">{{ day }}</span>
      </div>
      
      <div class="calendar-grid">
        <div 
          v-for="(day, index) in daysInMonth" 
          :key="index"
          class="calendar-day"
          :class="{ 
            'has-record': day && day.hasRecord,
            'empty': !day
          }"
          @click="selectDate(day)"
        >
          <span v-if="day" class="day-number">{{ day.day }}</span>
          <span v-if="day && day.hasRecord" class="mhi-dot" :style="{ backgroundColor: getMhiColor(day.record.mhi) }"></span>
        </div>
      </div>
      
    </div>
  </div>
</template>

<style scoped>
.calendar-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E7 0%, #FFE4C4 50%, #FFDAB9 100%);
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 28px;
  color: #5D4037;
  margin-bottom: 5px;
}

.page-header p {
  font-size: 14px;
  color: #9E9E9E;
}

.calendar-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #FFE4E1;
  border-radius: 50%;
  font-size: 24px;
  color: #FF69B4;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  background: #FFB6C1;
}

.month-year {
  font-size: 18px;
  font-weight: bold;
  color: #5D4037;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 10px;
}

.weekday {
  text-align: center;
  font-size: 14px;
  color: #9E9E9E;
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  background: #FAFAFA;
}

.calendar-day:hover:not(.empty) {
  background: #FFE4E1;
}

.calendar-day.has-record {
  background: #FFF0F5;
}

.calendar-day.selected {
  background: #FFB6C1;
}

.calendar-day.empty {
  background: transparent;
  cursor: default;
}

.day-number {
  font-size: 14px;
  color: #5D4037;
}

.mhi-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 4px;
}

.report-panel {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.report-header h2 {
  font-size: 18px;
  color: #5D4037;
}

.close-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: #FFE4E1;
  border-radius: 50%;
  font-size: 20px;
  color: #FF69B4;
  cursor: pointer;
}

.mhi-card {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #FFF0F5 0%, #FFE4E1 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.mhi-score {
  margin-bottom: 10px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #FF69B4;
}

.score-label {
  font-size: 16px;
  color: #9E9E9E;
  margin-left: 5px;
}

.mhi-level {
  font-size: 20px;
  font-weight: bold;
}

.dimensions-row {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.dimension-item {
  text-align: center;
}

.dim-icon {
  font-size: 24px;
  display: block;
  margin-bottom: 5px;
}

.dim-label {
  font-size: 12px;
  color: #9E9E9E;
  display: block;
  margin-bottom: 5px;
}

.dim-value {
  font-size: 18px;
  font-weight: bold;
  color: #5D4037;
}

.tag-desc {
  background: rgba(255, 105, 180, 0.1);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #5D4037;
}

.tag-icon {
  margin-right: 8px;
}

.insight-card, .suggestion-card {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.6;
}

.insight-card {
  background: #E3F2FD;
  color: #1565C0;
}

.suggestion-card {
  background: #E8F5E9;
  color: #2E7D32;
}

.insight-icon, .suggestion-icon {
  margin-right: 8px;
}

.empty-hint {
  text-align: center;
  color: #9E9E9E;
  font-size: 14px;
}
</style>
