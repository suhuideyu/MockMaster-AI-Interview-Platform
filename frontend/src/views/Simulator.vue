<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero hero-layout">
          <div>
            <p class="eyebrow">Simulator</p>
            <h1>模拟人页面只做展示和请求</h1>
          </div>
          <div class="page-card side-card">
            <span class="data-label">当前状态</span>
            <div class="metric-number">后端待接入</div>
            <p class="muted">现在前端会优先展示可用状态。</p>
          </div>
        </section>

        <section class="simulator-grid">
          <article class="page-card player-card">
            <div class="section-head">
              <span class="soft-badge">Preview</span>
            </div>
            <RtcVirtualPlayer
              :session-config="sessionConfig"
              title="后端尚未下发虚拟人播放参数"
              description="等后端完成第三方会话创建后，这里会自动替换成真实虚拟人流。"
            />
          </article>

          <article class="page-card architecture-card">
            <div class="section-head">
              <span class="soft-badge">Architecture</span>
            </div>
            <div class="step-list">
              <div class="step-item">
                <strong>1. 前端</strong>
                <p>向你自己的后端请求“创建虚拟人会话”。</p>
              </div>
              <div class="step-item">
                <strong>2. 后端</strong>
                <p>保存第三方密钥、生成签名、调用第三方 API，并把安全的播放参数返回给前端。</p>
              </div>
              <div class="step-item">
                <strong>3. 播放器</strong>
                <p>收到完整会话参数后再初始化 RTC 播放。</p>
              </div>
            </div>
          </article>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import Sidebar from "../components/Sidebar.vue";
import RtcVirtualPlayer from "../components/RtcVirtualPlayer.vue";
import { fetchSimulatorSession } from "../api/simulator";

const sessionConfig = ref(null);

onMounted(async () => {
  try {
    sessionConfig.value = await fetchSimulatorSession().catch(() => null);
  } catch (error) {
    ElMessage.error(error.message || "获取模拟人配置失败");
  }
});
</script>

<style scoped>
.hero-layout,
.simulator-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.side-card,
.player-card,
.architecture-card {
  padding: 24px;
}

.section-head {
  margin-bottom: 18px;
}

.step-list {
  display: grid;
  gap: 14px;
}

.step-item {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-bg-soft);
}

.step-item p {
  margin: 8px 0 0;
  color: var(--color-text-muted);
}

@media (max-width: 980px) {
  .hero-layout,
  .simulator-grid {
    grid-template-columns: 1fr;
  }
}
</style>
