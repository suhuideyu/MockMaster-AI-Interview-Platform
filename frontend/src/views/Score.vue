<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero">
          <p class="eyebrow">Score overview</p>
          <h1>面试结束，来看看你的表现吧。</h1>
          <p>
            生成你的面试评分、多维度分析专业能力、表达能力、逻辑思维、应变能力等。
          </p>
        </section>

        <section class="metrics-grid">
          <article
            v-for="item in metrics"
            :key="item.label"
            class="page-card metric-card"
          >
            <span class="data-label">{{ item.label }}</span>
            <strong>{{ item.value }}{{ item.suffix }}</strong>
          </article>
        </section>

        <section class="page-card chart-card">
          <div class="section-head">
            <span class="soft-badge">维度评分</span>
          </div>
          <div
            v-if="chartData.avgAccuracy > 0"
            ref="chartRef"
            class="chart-host"
          ></div>
          <div v-else class="empty-copy">
            暂无评分数据，完成面试后会显示详细分析。
          </div>
        </section>

        <section class="page-card suggestion-card">
          <div class="section-head">
            <span class="soft-badge">成长建议</span>
            <span class="chart-title">针对性建议，帮你稳步提升</span>
          </div>
          <div class="suggestion-content">
            {{ growthSuggestion }}
          </div>
        </section>

        <section class="detail-grid">
          <article class="page-card ratio-card">
            <div class="section-head">
              <span class="soft-badge">模式统计</span>
            </div>
            <div class="ratio-list">
              <div class="ratio-row">
                <span>语音模式</span>
                <strong>{{ ratio.voiceCount }} 场</strong>
              </div>
              <div class="ratio-row">
                <span>文本模式</span>
                <strong>{{ ratio.textCount }} 场</strong>
              </div>
              <div class="ratio-row">
                <span>总计划时长</span>
                <strong>{{ ratio.totalPlanned }} 分钟</strong>
              </div>
              <div class="ratio-row">
                <span>总实际时长</span>
                <strong>{{ ratio.totalActual }} 分钟</strong>
              </div>
            </div>
          </article>

          <article class="page-card summary-card">
            <div class="section-head">
              <span class="soft-badge">近期面试</span>
            </div>
            <div v-if="summaries.length" class="summary-list">
              <div
                v-for="item in summaries"
                :key="item.interviewId"
                class="summary-row"
              >
                <div class="summary-header">
                  <strong>{{ item.jobName }}</strong>
                  <span class="score-badge"
                    >{{ item.totalScore?.toFixed(2) || "-" }} 分</span
                  >
                </div>
              </div>
            </div>
            <div v-else class="empty-copy">暂无可展示的评分相关数据。</div>
          </article>
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
import { fetchScoreOverview } from "../api/score";

const chartRef = ref(null);
const metrics = ref([]);
const chartData = ref({
  avgAccuracy: 0,
  avgProfessional: 0,
  avgLogic: 0,
  avgTotalScore: 0,
});
const ratio = ref({
  voiceCount: 0,
  textCount: 0,
  totalPlanned: 0,
  totalActual: 0,
});
const summaries = ref([]);
let chart = null;

const growthSuggestion = computed(() => {
  const s = parseFloat(chartData.value.avgAccuracy) || 0;
  const p = parseFloat(chartData.value.avgProfessional) || 0;
  const l = parseFloat(chartData.value.avgLogic) || 0;

  if (s === 0 && p === 0 && l === 0) {
    return "完成面试后，将基于你的语义、专业、逻辑三大维度生成专属提升建议。";
  }

  const getComment = (score, type) => {
    const list = {
      logic: [
        "逻辑链条混乱，观点前后不连贯，面试官很难跟上思路。建议强制使用「总-分-总」结构，先给结论，再分点阐述。",
        "逻辑框架基本成立，但层次不清、容易跑题。回答前先列关键词大纲，会清晰很多。",
        "逻辑表达合格，结构完整，但过渡生硬。可多用连接词让要点串联更自然。",
        "逻辑表达清晰，层次分明。可尝试用 STAR 法则让回答更具说服力。",
        "逻辑表达非常出色，结构严谨条理清晰，是你的核心优势，请继续保持。"
      ],
      professional: [
        "专业知识储备明显不足，建议先梳理岗位核心知识点，结合项目做专项练习。",
        "专业基础薄弱，只能回答概念无法展开。务必把知识点和项目经验绑定。",
        "专业基础尚可，但深度不足。可补充底层原理与优化思路，提升回答质量。",
        "专业知识扎实，有细节有案例。可关注行业新技术，让回答更具前瞻性。",
        "专业能力非常优秀，深度与广度兼备，是你的核心竞争力。"
      ],
      semantic: [
        "表达卡顿严重、不流畅。建议把高频回答写下来，反复朗读练习断句与流畅度。",
        "表达能听懂，但口头禅多、重复明显。可录音自查，刻意去掉冗余词汇。",
        "表达基本流畅，但语气平淡。可加强停顿与重音，提升感染力。",
        "表达自然流畅，沟通感佳，自信度很好。可加入案例让表达更生动。",
        "表达能力出色，从容有感染力，面试加分项，请继续保持。"
      ]
    };
    const idx = score < 60 ? 0 : score < 70 ? 1 : score < 80 ? 2 : score < 90 ? 3 : 4;
    return list[type][idx];
  };

  const logicTxt = getComment(l, "logic");
  const proTxt = getComment(p, "professional");
  const semTxt = getComment(s, "semantic");

  const highNum = [l, p, s].filter(i => i >= 90).length;
  const lowNum = [l, p, s].filter(i => i < 60).length;

  if (highNum === 3) {
    return `你整体表现非常优秀！${logicTxt} ${proTxt} ${semTxt}`;
  }

  if (highNum >= 1) {
    let str = "你具备优秀的能力项，继续保持！";
    if (lowNum > 0) str += " 但存在明显短板，需要重点提升：";
    return str + ` ${logicTxt} ${proTxt} ${semTxt}`;
  }

  if (lowNum >= 2) {
    return `目前整体基础较弱，针对性提升会进步飞快：${logicTxt} ${proTxt} ${semTxt}`;
  }

  return `整体表现中等，仍有较大提升空间：${logicTxt} ${proTxt} ${semTxt}`;
});

function renderChart() {
  if (!chartRef.value) return;

  chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: { trigger: "item" },
    legend: { data: ["语义分", "专业分", "逻辑分"], bottom: 0 },
    radar: {
      indicator: [
        { name: "语义分", max: 100 },
        { name: "专业分", max: 100 },
        { name: "逻辑分", max: 100 },
        { name: "综合得分", max: 100 },
      ],
    },
    series: [
      {
        name: "你的表现",
        type: "radar",
        data: [
          {
            value: [
              parseFloat(chartData.value.avgAccuracy) || 0,
              parseFloat(chartData.value.avgProfessional) || 0,
              parseFloat(chartData.value.avgLogic) || 0,
              parseFloat(chartData.value.avgTotalScore) || 0,
            ],
            areaStyle: { color: "rgba(201, 111, 59, 0.2)" },
            lineStyle: { color: "#c96f3b" },
            itemStyle: { color: "#c96f3b" },
          },
        ],
      },
    ],
  });
}

function handleResize() {
  chart?.resize();
}

onMounted(async () => {
  try {
    const data = await fetchScoreOverview();

    metrics.value = [
      { label: "已完成面试", value: data.completedCount, suffix: "场" },
      { label: "平均语义分", value: data.avgAccuracy?.toFixed(2) || 0, suffix: "" },
      { label: "平均专业分", value: data.avgProfessional?.toFixed(2) || 0, suffix: "" },
      { label: "平均逻辑分", value: data.avgLogic?.toFixed(2) || 0, suffix: "" },
      { label: "综合平均分", value: data.avgTotalScore?.toFixed(2) || 0, suffix: "" },
      {
        label: "语音模式占比",
        value: data.completedCount ? Math.round((data.voiceCount / data.completedCount) * 100) : 0,
        suffix: "%",
      },
    ];

    chartData.value = {
      avgAccuracy: data.avgAccuracy || 0,
      avgProfessional: data.avgProfessional || 0,
      avgLogic: data.avgLogic || 0,
      avgTotalScore: data.avgTotalScore || 0,
    };

    ratio.value = {
      voiceCount: data.voiceCount,
      textCount: data.textCount,
      totalPlanned: data.totalPlannedDuration,
      totalActual: data.totalActualDuration,
    };

    summaries.value = data.recentSummaries || [];

    await nextTick(() => {
      renderChart();
      window.addEventListener("resize", handleResize);
    });
  } catch (error) {
    ElMessage.error(error.message || "获取评分概览失败");
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  chart?.dispose();
});
</script>

<style scoped>
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.metrics-grid .metric-card:nth-child(1) {
  grid-column: 1 / 3;
}

.metrics-grid .metric-card:nth-child(6) {
  grid-column: 3 / 5;
}

.metrics-grid .metric-card:nth-child(2),
.metrics-grid .metric-card:nth-child(3),
.metrics-grid .metric-card:nth-child(4),
.metrics-grid .metric-card:nth-child(5) {
  grid-column: auto;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.metric-card,
.ratio-card {
  padding: 24px;
}

.summary-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 400px;
}

.metric-card {
  display: grid;
  gap: 12px;
}

.metric-card strong {
  font-family: var(--font-display);
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  color: var(--color-heading);
}

.section-head {
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-card .section-head {
  flex-shrink: 0;
}

.summary-card > .empty-copy {
  flex: 1;
  overflow-y: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ratio-list {
  display: grid;
  gap: 14px;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.ratio-row,
.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: var(--color-bg-soft);
}

.summary-row {
  display: grid;
}

.empty-copy {
  color: var(--color-text-muted);
}

.chart-card {
  padding: 24px;
}

.chart-host {
  height: 400px;
  width: 100%;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.score-badge {
  background: rgba(201, 111, 59, 0.15);
  color: #c96f3b;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
}

.suggestion-card {
  padding: 24px;
}
.suggestion-content {
  line-height: 1.8;
  font-size: 14px;
  color: var(--color-text-muted);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-heading);
}
.soft-badge {
  background: #fdf0e7;
  color: #c96f3b;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 980px) {
  .metrics-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>