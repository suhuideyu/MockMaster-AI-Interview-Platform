<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero">
          <p class="eyebrow">Growth curve</p >
          <h1>看见自己的进步，每一次练习都算数。</h1>
          <p>这里记录你所有面试练习的分数、时长、维度变化，用数据见证你的成长。</p >
        </section>

        <section v-if="points.length" class="stats-grid">
          <div class="stat-card">
            <div class="stat-label">练习总场次</div>
            <div class="stat-value">{{ stats.totalCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">累计时长</div>
            <div class="stat-value">{{ stats.totalDuration }} <span class="stat-unit">分钟</span></div>
          </div>
          <div class="stat-card">
            <div class="stat-label">平均分数</div>
            <div class="stat-value">{{ stats.avgScore }} <span class="stat-unit">分</span></div>
          </div>
          <div class="stat-card">
            <div class="stat-label">最近练习</div>
            <div class="stat-value">{{ stats.latestDate }}</div>
          </div>
        </section>

        <section class="page-card chart-card">
          <div class="section-head">
            <span class="soft-badge">趋势</span>
            <span class="chart-title">平均分数趋势</span>
          </div>
          <div v-if="points.length" ref="trendChartRef" class="chart-host"></div>
          <div v-else class="empty-copy">暂无成长曲线数据，等有训练记录后会自动显示。</div>
        </section>

        <section class="page-card chart-card">
          <div class="section-head">
            <span class="soft-badge">日历</span>
            <span class="chart-title">练习时间统计</span>
          </div>
          <div v-if="points.length" ref="heatmapChartRef" class="chart-host heatmap-host"></div>
          <div v-else class="empty-copy">暂无活跃度数据</div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, computed } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import Sidebar from "../components/Sidebar.vue";
import { fetchGrowthCurveData } from "../api/growthCurve";

const trendChartRef = ref(null);
const heatmapChartRef = ref(null);
const points = ref([]);
let trendChart = null;
let heatmapChart = null;

const stats = computed(() => {
  if (!points.value.length) {
    return { totalCount: 0, totalDuration: 0, avgScore: 0, latestDate: "-" };
  }
  const totalCount = points.value.reduce((sum, p) => sum + (p.interviewCount || 0), 0);
  const totalDuration = points.value.reduce((sum, p) => sum + (p.totalDuration || 0), 0);
  const avgScore = (
    points.value.reduce((sum, p) => sum + (parseFloat(p.avgScore) || 0), 0) / points.value.length
  ).toFixed(1);
  const latestDate = points.value[points.value.length - 1]?.date || "-";
  return { totalCount, totalDuration, avgScore, latestDate };
});

function renderTrendChart() {
  if (!trendChartRef.value || !points.value.length) return;
  trendChart = echarts.init(trendChartRef.value);
  trendChart.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: 'cross' } },
    grid: { left: 40, right: 30, top: 30, bottom: 40, containLabel: true },
    xAxis: {
      type: "category",
      data: points.value.map((item) => item.date),
      axisLine: { lineStyle: { color: "#e8e0d5" } },
      axisLabel: { color: "#8b7355" }
    },
    yAxis: {
      type: "value",
      min: 0, max: 100,
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      name: "平均分",
      type: "line",
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: points.value.map((item) => parseFloat(item.avgScore) || 0),
      lineStyle: { color: "#c96f3b", width: 3 },
      itemStyle: { color: "#c96f3b", borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(201, 111, 59, 0.3)' },
          { offset: 1, color: 'rgba(201, 111, 59, 0)' }
        ])
      }
    }]
  });
}

function renderHeatmapChart() {
  if (!heatmapChartRef.value || !points.value.length) return;

  const dates = points.value.map((p) => p.date).sort();
  const currentYear = new Date(dates[dates.length - 1]).getFullYear();
  const heatmapData = points.value.map((item) => [item.date, item.totalDuration || 0]);
  const maxDuration = Math.max(...points.value.map((p) => p.totalDuration || 0), 30);

  heatmapChart = echarts.init(heatmapChartRef.value);
  heatmapChart.setOption({
    tooltip: {
      position: 'top',
      formatter: (p) => `${p.data[0]}<br/>练习时长: <b>${p.data[1]}</b> 分钟`
    },
    visualMap: {
      min: 0,
      max: maxDuration,
      type: 'piecewise',
      orient: 'horizontal',
      left: 'center',
      top: 0,
      pieces: [
        { min: 0, max: 0, label: '无练习', color: '#ebedf0' },
        { min: 1, max: 10, label: '1-10分', color: '#ffe8d6' },
        { min: 11, max: 30, label: '11-30分', color: '#ffb38a' },
        { min: 31, label: '30分+', color: '#c96f3b' }
      ],
      textStyle: { color: '#8b7355', fontSize: 11 }
    },
    calendar: {
      top: 70,
      left: 40,
      right: 20,
      cellSize: [16, 16], 
      range: currentYear,
      itemStyle: { borderWidth: 3, borderColor: '#fff' },
      splitLine: { show: false },
      dayLabel: { nameMap: ['日', '一', '二', '三', '四', '五', '六'], color: '#8b7355', fontSize: 11 },
      monthLabel: { nameMap: 'cn', color: '#8b7355', fontSize: 11 },
      yearLabel: { show: false }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: heatmapData
    }]
  });
}

function handleResize() {
  trendChart?.resize();
  heatmapChart?.resize();
}

onMounted(async () => {
  try {
    points.value = await fetchGrowthCurveData();
    await nextTick();
    renderTrendChart();
    renderHeatmapChart();
    window.addEventListener("resize", handleResize);
  } catch (error) {
    ElMessage.error(error.message || "获取数据失败");
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  trendChart?.dispose();
  heatmapChart?.dispose();
});
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fdfaf5;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  border: 1px solid #eee5d8;
  transition: transform 0.2s;
}

.stat-card:hover { transform: translateY(-2px); }

.stat-label { font-size: 12px; color: #8b7355; margin-bottom: 8px; text-transform: uppercase; }
.stat-value { font-size: 32px; font-weight: 700; color: #2c2416; }
.stat-unit { font-size: 14px; margin-left: 4px; font-weight: 400; color: #8b7355; }

.chart-card { padding: 24px; margin-bottom: 24px; background: #fff; border-radius: 16px; border: 1px solid #f0ece6; }
.section-head { margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
.soft-badge { background: #fdf0e7; color: #c96f3b; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.chart-title { font-size: 15px; font-weight: 600; color: #2c2416; }

.chart-host { width: 100%; height: 350px; }
.heatmap-host { height: 240px; } 

.empty-copy { padding: 40px; text-align: center; color: #bda98e; font-style: italic; }
</style>