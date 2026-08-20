<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero">
          <p class="eyebrow">Interview history</p>
          <h1>查看你的每一次练习轨迹。</h1>
          <p>保存每一次练习，让进步有迹可循。</p>
        </section>

        <section class="filters page-card">
          <select v-model="filterTime" class="filter-select">
            <option value="all">全部时间</option>
            <option value="7">近 7 天</option>
            <option value="30">近 30 天</option>
          </select>
          <select v-model="filterJob" class="filter-select">
            <option value="all">全部岗位</option>
            <option v-for="job in jobs" :key="job.id" :value="job.id">
              {{ job.jobName }}
            </option>
          </select>
          <select v-model="filterMode" class="filter-select">
            <option value="all">全部模式</option>
            <option value="voice">语音面试</option>
            <option value="text">文本面试</option>
          </select>
        </section>

        <section class="history-list">
          <div v-if="loading" class="page-card empty-block">
            历史记录加载中...
          </div>
          <div
            v-else-if="filteredList.length === 0"
            class="page-card empty-block"
          >
            还没有符合条件的完整面试记录。
          </div>

          <article
            v-for="item in filteredList"
            :key="item.id"
            class="page-card history-item"
          >
            <div class="item-main">
              <div class="item-head">
                <h2>{{ item.jobName }}</h2>
                <span class="soft-badge">{{
                  item.mode === "voice" ? "语音模式" : "文本模式"
                }}</span>
              </div>

              <div class="item-meta">
                <span>难度：{{ difficultyLabelMap[item.difficulty] }}</span>
                <span>计划时长：{{ item.plannedDuration }} 分钟</span>
                <span>实际时长：{{ item.actualDuration }} 分钟</span>
                <span>完成时间：{{ item.endTime }}</span>
              </div>

              <div class="item-summary">
                <div class="summary-card">
                  <span class="data-label">本场评分</span>
                  <p class="score-display">
                    {{ item.averageScore?.toFixed(2) || "-" }} 分
                  </p>
                </div>
              </div>
            </div>

            <div class="item-actions">
              <button
                class="pill-button is-primary"
                @click="restartInterview(item)"
              >
                再来一场
              </button>
              <button class="pill-button" @click="removeItem(item.id)">
                删除记录
              </button>
            </div>
          </article>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import Sidebar from "../components/Sidebar.vue";
import { fetchJobs } from "../api/job";
import { deleteInterview, fetchInterviewHistory } from "../api/interview";

const router = useRouter();
const loading = ref(false);
const historyList = ref([]);
const jobs = ref([]);

const filterTime = ref("all");
const filterJob = ref("all");
const filterMode = ref("all");

const difficultyLabelMap = {
  easy: "简单",
  medium: "中等",
  hard: "困难",
};

onMounted(async () => {
  loading.value = true;
  try {
    const [jobData, historyData] = await Promise.all([
      fetchJobs(),
      fetchInterviewHistory(),
    ]);
    jobs.value = jobData;
    historyList.value = historyData;
  } catch (error) {
    ElMessage.error(error.message || "获取历史记录失败");
  } finally {
    loading.value = false;
  }
});

const filteredList = computed(() =>
  historyList.value.filter((item) => {
    const endDate = new Date(item.endTime);
    const timePass =
      filterTime.value === "all" ||
      endDate >
        new Date(Date.now() - Number(filterTime.value) * 24 * 60 * 60 * 1000);
    const jobPass =
      filterJob.value === "all" ||
      String(item.jobId) === String(filterJob.value);
    const modePass =
      filterMode.value === "all" || item.mode === filterMode.value;
    return timePass && jobPass && modePass;
  }),
);

function restartInterview(item) {
  router.push({
    name: "AiInterviewSelect",
    query: {
      jobId: item.jobId,
      difficulty: item.difficulty,
      duration: item.plannedDuration,
    },
  });
}

async function removeItem(interviewId) {
  try {
    await ElMessageBox.confirm("删除后将无法恢复，确认继续吗？", "删除记录", {
      type: "warning",
    });
    await deleteInterview(interviewId);
    historyList.value = historyList.value.filter(
      (item) => item.id !== interviewId,
    );
    ElMessage.success("删除成功");
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
}
</script>

<style scoped>
.filters {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 240px));
  gap: 14px;
}

.filter-select {
  min-height: 46px;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-panel);
  padding: 0 14px;
  color: var(--color-heading);
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #d97706;
  box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.1);
}

.filter-select option:checked {
  background: linear-gradient(#d97706, #d97706);
  background-color: #d97706;
  color: #fff;
}

.history-list {
  display: grid;
  gap: 16px;
}

.history-item {
  padding: 22px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
}

.item-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.item-head h2 {
  margin: 0;
  font-family: var(--font-display-alt);
  font-size: 1.55rem;
  color: var(--color-heading);
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
}

.item-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  padding: 16px;
  border-radius: 20px;
  background: var(--color-bg-soft);
}

.summary-card p {
  margin: 8px 0 0;
  color: var(--color-text);
  line-height: var(--leading-cn-normal);
}

.score-display {
  font-size: 1.8rem;
  font-weight: 700;
  color: #c96f3b;
  font-family: var(--font-display);
}

.item-actions {
  display: grid;
  align-content: start;
  gap: 10px;
  min-width: 124px;
}

.empty-block {
  padding: 34px;
  text-align: center;
  color: var(--color-text-muted);
}

@media (max-width: 960px) {
  .filters,
  .item-summary {
    grid-template-columns: 1fr;
  }

  .history-item {
    grid-template-columns: 1fr;
  }

  .item-actions {
    grid-auto-flow: row;
  }
}
</style>