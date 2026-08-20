<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <p class="brand-kicker">智面AI_MockMaster</p>
      <strong>AI 面试练习室</strong>
      <span>以练养技，以熟破局</span>
    </div>

    <nav class="sidebar-nav" aria-label="主导航">
      <button
        v-for="item in navItems"
        :key="item.name"
        class="nav-item"
        :class="{ active: isActive(item.name) }"
        @click="navigateTo(item.name)"
      >
        <span class="nav-label">{{ item.label }}</span>
        <small class="nav-meta">{{ item.meta }}</small>
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();

const navItems = [
  { name: "Home", label: "岗位选择", meta: "选择目标岗位" },
  { name: "AiInterviewSelect", label: "面试设置", meta: "难度、时长与模式" },
  { name: "Score", label: "评分", meta: "训练表现概览" },
  { name: "QuestionBank", label: "题库", meta: "岗位题目入口" },
  { name: "GrowthCurve", label: "成长曲线", meta: "历史训练趋势" },
  { name: "InterviewHistory", label: "历史记录", meta: "已完成面试" },
  { name: "PersonalCenter", label: "个人中心", meta: "账号信息与退出登录" },
];

const activeGroup = computed(() => {
  if (route.name === "InterviewSetting") return "AiInterviewSelect";
  if (route.name === "AiInterview") return "AiInterviewSelect";
  if (route.name === "Simulator") return "AiInterviewSelect";
  return route.name;
});

function isActive(name) {
  return activeGroup.value === name;
}

function navigateTo(name) {
  router.push({ name, query: { ...route.query } });
}
</script>

<style scoped>
.sidebar {
  width: 224px;
  min-height: 100vh;
  position: fixed;
  inset: 0 auto 0 0;
  padding: 18px 14px;
  box-sizing: border-box;
  background: rgb(255 251 245 / 0.84);
  border-right: 1px solid rgb(226 203 169 / 0.7);
  backdrop-filter: blur(14px);
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 18px;
  z-index: var(--z-sticky);
  overflow-y: auto;
}

.sidebar-brand {
  border: 1px solid rgb(226 203 169 / 0.7);
  background: rgb(255 252 247 / 0.92);
  border-radius: 24px;
  box-shadow: 0 18px 40px rgb(111 70 26 / 0.08);
  padding: 18px 18px 20px;
  display: grid;
  gap: 6px;
}

.brand-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--color-accent-strong);
}

.sidebar-brand strong {
  font-family: var(--font-display);
  font-size: 1.6rem;
  line-height: 1.1;
  color: var(--color-heading);
}

.sidebar-brand span {
  color: var(--color-text-muted);
  font-size: 0.84rem;
  line-height: 1.6;
}

.sidebar-nav {
  display: grid;
  align-content: start;
  gap: 8px;
  padding-bottom: 12px;
}

.nav-item {
  width: 100%;
  text-align: left;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--color-text-muted);
  display: grid;
  gap: 4px;
  cursor: pointer;
  transition:
    transform var(--duration-base) var(--ease-standard),
    background-color var(--duration-base) var(--ease-standard),
    border-color var(--duration-base) var(--ease-standard),
    color var(--duration-base) var(--ease-standard);
}

.nav-item:hover {
  transform: translateY(-1px);
  background: rgb(255 247 237 / 0.9);
  border-color: rgb(226 203 169 / 0.5);
}

.nav-item.active {
  background: linear-gradient(
    135deg,
    rgb(255 242 220) 0%,
    rgb(247 212 155) 100%
  );
  border-color: rgb(217 119 6 / 0.18);
  color: #7c4914;
}

.nav-label {
  font-size: 0.95rem;
  font-weight: 600;
}

.nav-meta {
  font-size: 0.75rem;
  opacity: 0.88;
}

@media (max-width: 900px) {
  .sidebar {
    display: none;
  }
}
</style>
