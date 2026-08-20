<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero">
          <p class="eyebrow">Question bank</p>
          <h1>海量真题，按需练习，针对性突破。</h1>
          <p>按岗位、按题型、按难度分类，专项练习，随时随地打磨面试能力。</p>
        </section>

        <section class="page-card bank-card">
          <div class="section-head">
            <span class="soft-badge">筛选条件</span>
          </div>
          <div class="filters">
            <select class="filter-select" v-model="selectedJobFilter" @change="handleFilterChange">
              <option value="">选择岗位</option>
              <option
                v-for="job in jobsForFilter"
                :key="job.id"
                :value="job.id"
              >
                {{ job.jobName }}
              </option>
            </select>

            <select class="filter-select" v-model="selectedDifficulty" @change="handleFilterChange">
              <option value="">全部难度</option>
              <option value="1">简单</option>
              <option value="2">中等</option>
              <option value="3">困难</option>
            </select>

            <button class="reset-btn" @click="handleReset">重置筛选</button>
          </div>
        </section>

        <section class="page-card bank-card">
          <div class="section-head">
            <span class="soft-badge">题目列表</span>
          </div>
          <div v-if="loading" class="empty-copy">加载中...</div>
          <div v-else-if="questions.length" class="question-list">
            <div v-for="(item, index) in questions" :key="item.id" class="question-row">
              <div class="question-header">
                <span class="question-number">{{ index + 1 }}.</span>
                <strong>{{ item.title }}</strong>
                <span
                  class="difficulty-badge"
                  :class="`difficulty-${item.difficulty}`"
                >
                  {{ getDifficultyLabel(item.difficulty) }}
                </span>
              </div>
              <p class="question-content">{{ item.content }}</p>
            </div>
          </div>
          <div v-else class="empty-copy">暂无题目数据。</div>
        </section>
      </div>
    </main>

    <button
      class="scroll-to-top-btn"
      @click="handleScrollToTop"
      aria-label="回到顶部"
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <polyline points="18 15 12 9 6 15"></polyline>
      </svg>
    </button>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import Sidebar from "../components/Sidebar.vue";
import { fetchQuestionBankData, getQuestions } from "../api/questionBank";

const jobs = ref([]);
const jobsForFilter = ref([]);
const questions = ref([]);
const selectedJobId = ref(null);
const selectedJobFilter = ref("");
const selectedDifficulty = ref("");
const loading = ref(false);

const difficultyLabels = {
  1: "简单",
  2: "中等",
  3: "困难",
};

function getDifficultyLabel(difficulty) {
  return difficultyLabels[difficulty] || "未知";
}

onMounted(async () => {
  try {
    const data = await fetchQuestionBankData();
    jobs.value = data.jobs;
    jobsForFilter.value = data.jobs;
  } catch (error) {
    ElMessage.error(error.message || "获取岗位数据失败");
  }
});

async function handleJobSelect(jobId) {
  selectedJobId.value = selectedJobId.value === jobId ? null : jobId;
  await loadQuestions();
}

async function handleFilterChange() {
  await loadQuestions();
}

async function loadQuestions() {
  const jobId = selectedJobFilter.value
    ? parseInt(selectedJobFilter.value)
    : null;
  if (!jobId) {
    questions.value = [];
    return;
  }

  loading.value = true;
  try {
    const difficulty = selectedDifficulty.value
      ? parseInt(selectedDifficulty.value)
      : null;
    const data = await getQuestions(jobId, difficulty);
    questions.value = Array.isArray(data) ? data : [];
  } catch (error) {
    ElMessage.error(error.message || "获取题目失败");
    questions.value = [];
  } finally {
    loading.value = false;
  }
}

function handleReset() {
  selectedJobFilter.value = "";
  selectedDifficulty.value = "";
  questions.value = [];
}

function handleScrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}
</script>

<style scoped>
.bank-card {
  padding: 24px;
}

.section-head {
  margin-bottom: 18px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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

.reset-btn {
  min-height: 46px;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  padding: 0 14px;
  color: var(--color-heading);
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn:hover {
  background: var(--color-bg-lighter);
  border-color: var(--color-text-muted);
}

.question-list {
  display: grid;
  gap: 16px;
}

.question-row {
  padding: 20px;
  border-radius: 12px;
  background: var(--color-bg-soft);
  border-left: 4px solid var(--color-primary);
}

.question-number {
  font-weight: 600;
  color: var(--color-primary);
  margin-right: 8px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 12px;
  margin-bottom: 12px;
}

.question-header strong {
  color: var(--color-heading);
  flex: 1;
}

.difficulty-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.difficulty-1 {
  background: #E6F4EA;
  color: #09793F;
}
.difficulty-2 {
  background: #FFF4CC;
  color: #925E04;
}
.difficulty-3 {
  background: #FDE6E8;
  color: #A52634;
}

.question-content {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.6;
  font-size: 0.95rem;
}

.empty-copy {
  color: var(--color-text-muted);
  padding: 40px 20px;
  text-align: center;
}

.scroll-to-top-btn {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #efb566 0%, #d97706 100%);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.2);
  z-index: 100;
}

.scroll-to-top-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(217, 119, 6, 0.3);
}

.scroll-to-top-btn:active {
  transform: translateY(0);
}

.scroll-to-top-btn svg {
  width: 20px;
  height: 20px;
}
</style>