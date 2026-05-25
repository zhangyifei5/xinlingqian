<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';
import VChart from 'vue-echarts';
import { moodTestApi } from '../api';
import BackButton from '../components/BackButton.vue';

const router = useRouter();

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
]);

const historyData = ref([]);
const isLoading = ref(true);

const weekDays = ['日', '一', '二', '三', '四', '五', '六'];

const getLastSevenDays = () => {
  const days = [];
  for (let i = 6; i >= 0; i--) {
    const date = new Date();
    date.setDate(date.getDate() - i);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekDay = weekDays[date.getDay()];
    days.push({
      date: `${month}月${day}日`,
      weekDay: weekDay,
      fullDate: date.toISOString().split('T')[0]
    });
  }
  return days;
};

const chartData = computed(() => {
  const days = getLastSevenDays();
  const mhiData = [];
  const evData = [];
  const meData = [];
  const scData = [];
  const ccData = [];

  const recordMap = new Map();
  historyData.value.forEach(record => {
    recordMap.set(record.date, record);
  });

  days.forEach(day => {
    const record = recordMap.get(day.fullDate);
    if (record) {
      mhiData.push(Math.round(record.mhi));
      evData.push(Math.round(record.evScore * 20));
      meData.push(Math.round(record.meScore * 20));
      scData.push(Math.round(record.scScore * 20));
      ccData.push(Math.round(record.ccScore * 20));
    } else {
      mhiData.push(null);
      evData.push(null);
      meData.push(null);
      scData.push(null);
      ccData.push(null);
    }
  });

  return {
    days: days.map(d => `${d.date}(${d.weekDay})`),
    mhiData,
    evData,
    meData,
    scData,
    ccData
  };
});

const chartOption = computed(() => ({
  title: {
    text: '七日心情趋势',
    left: 'center',
    top: 10,
    textStyle: {
      color: '#5D4037',
      fontSize: 18,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#FFB6C1',
    borderWidth: 1,
    textStyle: {
      color: '#5D4037'
    },
    formatter: (params) => {
      let result = `<div style="font-weight:bold;margin-bottom:5px;">${params[0].axisValue}</div>`;
      params.forEach(param => {
        if (param.value !== null) {
          result += `<div style="display:flex;align-items:center;margin:3px 0;">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${param.color};margin-right:8px;"></span>
            <span>${param.seriesName}: ${param.value}分</span>
          </div>`;
        }
      });
      return result;
    }
  },
  legend: {
    data: ['心情健康值', '情绪效价', '心理能量', '社会连接', '认知清晰'],
    bottom: 10,
    textStyle: {
      color: '#5D4037'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartData.value.days,
    axisLine: {
      lineStyle: {
        color: '#FFB6C1'
      }
    },
    axisLabel: {
      color: '#5D4037',
      fontSize: 12,
      rotate: 30
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100,
    axisLine: {
      lineStyle: {
        color: '#FFB6C1'
      }
    },
    axisLabel: {
      color: '#5D4037'
    },
    splitLine: {
      lineStyle: {
        color: '#FFE4E1',
        type: 'dashed'
      }
    }
  },
  series: [
    {
      name: '心情健康值',
      type: 'line',
      data: chartData.value.mhiData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: '#FF69B4'
      },
      itemStyle: {
        color: '#FF69B4',
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(255, 105, 180, 0.3)' },
            { offset: 1, color: 'rgba(255, 105, 180, 0.05)' }
          ]
        }
      }
    },
    {
      name: '情绪效价',
      type: 'line',
      data: chartData.value.evData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: '#FFB6C1'
      },
      itemStyle: {
        color: '#FFB6C1',
        borderWidth: 2,
        borderColor: '#fff'
      }
    },
    {
      name: '心理能量',
      type: 'line',
      data: chartData.value.meData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: '#FFD700'
      },
      itemStyle: {
        color: '#FFD700',
        borderWidth: 2,
        borderColor: '#fff'
      }
    },
    {
      name: '社会连接',
      type: 'line',
      data: chartData.value.scData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: '#98FB98'
      },
      itemStyle: {
        color: '#98FB98',
        borderWidth: 2,
        borderColor: '#fff'
      }
    },
    {
      name: '认知清晰',
      type: 'line',
      data: chartData.value.ccData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: '#87CEEB'
      },
      itemStyle: {
        color: '#87CEEB',
        borderWidth: 2,
        borderColor: '#fff'
      }
    }
  ]
}));

const loadHistoryData = async () => {
  isLoading.value = true;
  try {
    const response = await moodTestApi.getHistoryRecords(7);
    console.log('历史记录API响应:', response);
    if (response.data.code === 200) {
      historyData.value = response.data.data;
      console.log('历史记录数据:', historyData.value);
      console.log('生成的日期列表:', getLastSevenDays());
    }
  } catch (error) {
    console.error('获取历史记录失败:', error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadHistoryData();
});
</script>

<template>
  <div class="trend-page">
    <div class="page-header">
      <BackButton />
      <h1>📊 七日心情趋势</h1>
      <p>追踪你的心情变化，发现生活中的小规律</p>
    </div>
    
    <div class="chart-container">
      <div v-if="isLoading" class="loading">
        <span class="loading-icon">⏳</span>
        <span>加载中...</span>
      </div>
      <v-chart v-else class="chart" :option="chartOption" autoresize />
    </div>
    
    <div class="stats-card">
      <div class="stat-item">
        <span class="stat-label">平均心情值</span>
        <span class="stat-value">
          {{ chartData.mhiData.filter(v => v !== null).length > 0 
            ? Math.round(chartData.mhiData.filter(v => v !== null).reduce((a, b) => a + b, 0) / chartData.mhiData.filter(v => v !== null).length) 
            : '--' }}分
        </span>
      </div>
      <div class="stat-item">
        <span class="stat-label">最高心情值</span>
        <span class="stat-value">
          {{ chartData.mhiData.filter(v => v !== null).length > 0 
            ? Math.max(...chartData.mhiData.filter(v => v !== null)) 
            : '--' }}分
        </span>
      </div>
      <div class="stat-item">
        <span class="stat-label">记录天数</span>
        <span class="stat-value">{{ chartData.mhiData.filter(v => v !== null).length }}天</span>
      </div>
    </div>
    
    <div class="tips-card">
      <h3>💡 小提示</h3>
      <p>观察你的心情变化规律，可以更好地了解自己。尝试找出影响心情的因素，比如天气、睡眠、社交等。</p>
    </div>
  </div>
</template>

<style scoped>
.trend-page {
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

.chart-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  min-height: 400px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #9E9E9E;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.chart {
  height: 400px;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #9E9E9E;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #FF69B4;
}

.tips-card {
  background: rgba(255, 105, 180, 0.1);
  border-radius: 16px;
  padding: 20px;
  border-left: 4px solid #FF69B4;
}

.tips-card h3 {
  font-size: 16px;
  color: #5D4037;
  margin-bottom: 10px;
}

.tips-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
