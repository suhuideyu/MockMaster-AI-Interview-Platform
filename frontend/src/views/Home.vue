<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero hero-layout">
          <div class="hero-copy">
            <p class="eyebrow">Role selection</p>
            <h1>从目标岗位开始，像编辑一场真正的面试练习。</h1>
            <p>
              先选择意向岗位，再进入统一的面试配置页设置难度、时长和模式。
            </p>
          </div>
          <div class="hero-aside page-card">
            <div>
              <span class="data-label">当前能力路径</span>
              <div class="metric-number">{{ jobList.length }}</div>
              <p class="muted">个可用岗位方向，覆盖前端、后端、产品、数据与 AI 等主流职业角色。</p>
            </div>
          </div>
        </section>

        <section class="job-grid">
          <button
            v-for="job in jobList"
            :key="job.id"
            class="job-card"
            @click="selectJob(job.id)"
          >
            <div class="job-card__top">
              <span class="soft-badge">岗位 {{ String(job.id).padStart(2, "0") }}</span>
            </div>
            <div class="job-card__name">{{ job.jobName }}</div>
            <div class="job-card__desc">{{ job.jobDesc }}</div>
            <div class="job-card__footer">进入面试配置</div>
          </button>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import Sidebar from "../components/Sidebar.vue";
import { fetchJobs } from "../api/job";

const router = useRouter();
const jobList = ref([]);

onMounted(async () => {
  try {
    jobList.value = await fetchJobs();
  } catch (error) {
    ElMessage.error(error.message || "获取岗位失败");
  }
});

function selectJob(jobId) {
  router.push({ name: "AiInterviewSelect", query: { jobId } });
}
</script>

<style scoped>
.hero-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(280px, 0.82fr);
  gap: 22px;
}

.hero-copy {
  display: grid;
  gap: 14px;
}

.hero-aside {
  padding: 24px;
  display: grid;
  align-content: end;
}

.hero-aside p {
  margin-top: 10px;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.job-card {
  padding: 22px;
  text-align: left;
  border: 1px solid rgb(226 203 169 / 0.7);
  border-radius: 24px;
  background: rgb(255 252 247 / 0.94);
  box-shadow: 0 18px 40px rgb(111 70 26 / 0.08);
  cursor: pointer;
  display: grid;
  gap: 16px;
  transition:
    transform var(--duration-base) var(--ease-standard),
    box-shadow var(--duration-base) var(--ease-standard),
    border-color var(--duration-base) var(--ease-standard);
}

.job-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 24px 48px rgb(111 70 26 / 0.12);
  border-color: rgb(217 119 6 / 0.22);
}

.job-card__top {
  display: flex;
  justify-content: flex-start;
}

.job-card__name {
  font-family: var(--font-display-alt);
  font-size: 1.5rem;
  line-height: 1.2;
  color: var(--color-heading);
}

.job-card__desc {
  min-height: 74px;
  color: var(--color-text-muted);
  line-height: var(--leading-cn-normal);
}

.job-card__footer {
  color: var(--color-accent-strong);
  font-weight: 600;
}

@media (max-width: 980px) {
  .hero-layout {
    grid-template-columns: 1fr;
  }
}
</style>
