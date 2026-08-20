<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero hero-layout">
          <div class="hero-copy">
            <p class="eyebrow">Interview configuration</p>
            <h1>在开始之前，把这场模拟面试调成最适合你的状态。</h1>
            <p>
              这一页集中完成岗位、难度、时长和模式配置。界面保持对称、克制、温暖，让注意力留在决策本身，而不是复杂表单上。
            </p>
          </div>
          <button class="pill-button is-muted go-back" @click="goHome">
            返回岗位页
          </button>
        </section>

        <section class="config-grid">
          <div class="page-card config-card">
            <div class="card-head">
              <span class="soft-badge">配置</span>
              <h2>面试设置</h2>
            </div>

            <el-form :model="form" label-position="top" class="config-form">
              <el-form-item label="意向岗位">
                <select v-model="form.jobId" class="interview-select">
                  <option :value="null">请选择岗位</option>
                  <option v-for="job in jobs" :key="job.id" :value="job.id">
                    {{ job.jobName }}
                  </option>
                </select>
              </el-form-item>

              <el-form-item label="难度">
                <div class="chip-grid">
                  <button
                    v-for="item in difficultyOptions"
                    :key="item.value"
                    type="button"
                    class="choice-card"
                    :class="{ active: form.difficulty === item.value }"
                    @click="form.difficulty = item.value"
                  >
                    <span>{{ item.label }}</span>
                    <small>{{ item.desc }}</small>
                  </button>
                </div>
              </el-form-item>

              <el-form-item label="时长">
                <div class="chip-grid">
                  <button
                    v-for="item in durationOptions"
                    :key="item.value"
                    type="button"
                    class="choice-card"
                    :class="{ active: form.duration === item.value }"
                    @click="form.duration = item.value"
                  >
                    <span>{{ item.label }}</span>
                    <small>{{ item.desc }}</small>
                  </button>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <div class="mode-stack">
            <button
              v-for="mode in modeOptions"
              :key="mode.value"
              type="button"
              class="page-card mode-card"
              @click="startInterview(mode.value)"
            >
              <div class="mode-card__top">
                <div class="mode-card__icon">{{ mode.icon }}</div>
                <span class="soft-badge">{{ mode.badge }}</span>
              </div>
              <h2>{{ mode.title }}</h2>
              <p>{{ mode.desc }}</p>
              <div class="mode-card__footer">
                以当前配置开始 {{ mode.title }}
              </div>
            </button>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import Sidebar from "../components/Sidebar.vue";
import { fetchJobs } from "../api/job";

const router = useRouter();
const route = useRoute();

const jobs = ref([]);
const form = ref({
  jobId: null,
  difficulty: "medium",
  duration: "20",
});

const difficultyOptions = [
  { value: "easy", label: "简单", desc: "适合热身与建立信心" },
  { value: "medium", label: "中等", desc: "标准模拟，兼顾压力与节奏" },
  { value: "hard", label: "困难", desc: "高压追问，更贴近正式场景" },
];

const durationOptions = [
  { value: "10", label: "10 分钟", desc: "快速练习" },
  { value: "20", label: "20 分钟", desc: "常规模拟" },
  { value: "30", label: "30 分钟", desc: "完整一轮" },
];

const modeOptions = [
  {
    value: "voice",
    icon: "语",
    badge: "实时对话",
    title: "语音面试",
    desc: "更贴近真实口语作答场景，适合训练表达节奏、停顿控制和临场反应。",
  },
  {
    value: "text",
    icon: "文",
    badge: "结构化问答",
    title: "文本面试",
    desc: "通过打字完成多轮问答，适合梳理结构化表达、项目复盘与逻辑组织。",
  },
];

onMounted(async () => {
  const q = route.query;
  if (q.jobId) form.value.jobId = Number(q.jobId);
  if (q.difficulty) form.value.difficulty = String(q.difficulty);
  if (q.duration) form.value.duration = String(q.duration);

  try {
    jobs.value = await fetchJobs();
    if (!form.value.jobId && jobs.value.length) {
      form.value.jobId = jobs.value[0].id;
    }
  } catch (error) {
    ElMessage.error(error.message || "获取岗位列表失败");
  }
});

function startInterview(mode) {
  if (!form.value.jobId) {
    ElMessage.warning("请先选择意向岗位");
    return;
  }

  router.push({
    path: "/interview",
    query: {
      jobId: form.value.jobId,
      difficulty: form.value.difficulty,
      duration: form.value.duration,
      mode,
    },
  });
}

function goHome() {
  router.push("/home");
}
</script>

<style scoped>
.hero-layout {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.hero-copy {
  display: grid;
  gap: 14px;
}

.go-back {
  white-space: nowrap;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(320px, 0.92fr);
  gap: 20px;
}

.config-card {
  padding: 26px;
}

.card-head {
  display: grid;
  gap: 10px;
  margin-bottom: 20px;
}

.card-head h2,
.mode-card h2 {
  margin: 0;
  font-family: var(--font-display-alt);
  font-size: 1.65rem;
  color: var(--color-heading);
}

.full-width {
  width: 100%;
}

.interview-select {
  width: 100%;
  min-height: 46px;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-panel);
  padding: 0 14px;
  color: var(--color-heading);
  cursor: pointer;
  font-size: inherit;
  transition: all 0.2s ease;
}

.interview-select:hover {
  border-color: rgba(217, 119, 6, 0.3);
}

.interview-select:focus {
  outline: none;
  border-color: #d97706;
  box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.1);
}

.interview-select option:checked {
  background: linear-gradient(#d97706, #d97706);
  background-color: #d97706;
  color: #fff;
}

.chip-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.choice-card {
  min-height: 88px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: var(--color-panel);
  color: var(--color-text);
  display: grid;
  gap: 6px;
  align-content: center;
  padding: 14px 12px;
  cursor: pointer;
  transition:
    transform var(--duration-base) var(--ease-standard),
    border-color var(--duration-base) var(--ease-standard),
    background-color var(--duration-base) var(--ease-standard);
}

.choice-card span {
  font-weight: 600;
}

.choice-card small {
  color: var(--color-text-muted);
}

.choice-card.active {
  transform: translateY(-2px);
  border-color: rgb(217 119 6 / 0.32);
  background: linear-gradient(
    180deg,
    rgb(255 245 232) 0%,
    rgb(252 230 191) 100%
  );
}

.mode-stack {
  display: grid;
  gap: 18px;
}

.mode-card {
  padding: 24px;
  text-align: left;
  display: grid;
  gap: 14px;
  cursor: pointer;
  transition:
    transform var(--duration-base) var(--ease-standard),
    box-shadow var(--duration-base) var(--ease-standard);
}

.mode-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 24px 48px rgb(111 70 26 / 0.12);
}

.mode-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.mode-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #efb566 0%, #d97706 100%);
  color: #fff;
  font-size: 1.4rem;
  font-weight: 700;
}

.mode-card p {
  margin: 0;
  color: var(--color-text-muted);
  line-height: var(--leading-cn-normal);
}

.mode-card__footer {
  color: var(--color-accent-strong);
  font-weight: 600;
}

@media (max-width: 1040px) {
  .config-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .hero-layout {
    flex-direction: column;
    align-items: flex-start;
  }

  .chip-grid {
    grid-template-columns: 1fr;
  }
}
</style>
