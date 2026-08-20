<template>
  <div class="app-shell">
    <Sidebar />
    <main class="app-main">
      <div class="page-stack">
        <section class="page-hero hero-layout">
          <div class="hero-left">
            <p class="eyebrow">Personal center</p>
          </div>
          <div class="hero-right">
            <div class="page-card hero-side">
              <span class="data-label">当前账号</span>
              <div class="metric-number">
                {{ profile.username || "未命名用户" }}
              </div>
              <p class="muted">用户 ID：{{ profile.id || "暂无" }}</p>
            </div>
            <button class="logout-btn-top" @click="handleLogout">
              退出当前账号
            </button>
          </div>
        </section>

        <section class="content-grid">
          <article class="page-card profile-card">
            <div class="section-head">
              <span class="soft-badge">资料</span>
              <button
                v-if="!isEditing"
                class="edit-btn"
                @click="handleEditClick"
                title="编辑个人信息"
              >
                编辑
              </button>
            </div>
            <div class="profile-main">
              <div v-if="!isEditing" class="avatar">
                <img
                  v-if="profile.avatar"
                  :src="profile.avatar"
                  class="avatar-preview"
                />
                <span v-else>{{ profile.username?.slice(0, 1) || "M" }}</span>
              </div>

              <div v-else class="avatar-upload">
                <input
                  type="file"
                  ref="avatarInput"
                  accept="image/jpeg,image/png"
                  style="display: none"
                  @change="handleAvatarUpload"
                />
                <div class="upload-btn" @click="$refs.avatarInput.click()">
                  <img
                    v-if="previewAvatar"
                    :src="previewAvatar"
                    class="avatar-preview"
                  />
                  <span v-else>点击上传头像</span>
                </div>
                <p class="upload-tip">支持 JPG / PNG</p>
              </div>

              <div class="profile-fields">
                <div v-if="!isEditing" class="field-row">
                  <span class="field-label">用户名</span>
                  <strong>{{ profile.username || "暂无" }}</strong>
                </div>
                <div v-else class="field-row">
                  <span class="field-label">用户名</span>
                  <input
                    v-model.trim="editForm.username"
                    class="field-input"
                    type="text"
                    placeholder="请输入用户名"
                  />
                </div>

                <div v-if="!isEditing" class="field-row">
                  <span class="field-label">手机号</span>
                  <span>{{ profile.phone || "暂无" }}</span>
                </div>
                <div v-else class="field-row">
                  <span class="field-label">手机号</span>
                  <input
                    v-model.trim="editForm.phone"
                    class="field-input"
                    type="tel"
                    placeholder="请输入手机号"
                  />
                </div>

                <div v-if="!isEditing" class="field-row">
                  <span class="field-label">邮箱</span>
                  <span>{{ profile.email || "暂无" }}</span>
                </div>
                <div v-else class="field-row">
                  <span class="field-label">邮箱</span>
                  <input
                    v-model.trim="editForm.email"
                    class="field-input"
                    type="email"
                    placeholder="请输入邮箱"
                  />
                </div>

                <div class="field-row">
                  <span class="field-label">注册时间</span>
                  <span>{{ profile.createTime || "暂无" }}</span>
                </div>
              </div>

              <div v-if="isEditing" class="field-actions">
                <button
                  class="pill-button is-primary"
                  :disabled="isSaving"
                  @click="handleSaveClick"
                >
                  {{ isSaving ? "保存中..." : "确定" }}
                </button>
                <button
                  class="pill-button is-secondary"
                  :disabled="isSaving"
                  @click="handleCancelClick"
                >
                  取消
                </button>
              </div>
            </div>
          </article>

          <article class="page-card stats-card">
            <div class="section-head">
              <span class="soft-badge">统计信息</span>
            </div>
            <div class="stats-grid">
              <div class="stat-box">
                <span class="data-label">已完成面试</span>
                <strong>{{ stats.totalCount }}</strong>
              </div>
              <div class="stat-box">
                <span class="data-label">语音模式</span>
                <strong>{{ stats.voiceCount }}</strong>
              </div>
              <div class="stat-box">
                <span class="data-label">文本模式</span>
                <strong>{{ stats.textCount }}</strong>
              </div>
              <div class="stat-box">
                <span class="data-label">最近完成时间</span>
                <strong>{{ stats.latestInterviewTime || "暂无记录" }}</strong>
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
import { useRouter } from "vue-router";
import Sidebar from "../components/Sidebar.vue";
import {
  fetchPersonalCenterData,
  logoutAccount,
  updateProfile,
} from "../api/personalCenter";
import axiosInstance from "../api/index";

const router = useRouter();
const profile = ref({});
const stats = ref({
  totalCount: 0,
  voiceCount: 0,
  textCount: 0,
  latestInterviewTime: "",
});
const isEditing = ref(false);
const isSaving = ref(false);

const editForm = ref({
  username: "",
  phone: "",
  email: "",
  avatar: "",
});

const previewAvatar = ref("");
const avatarInput = ref(null);

onMounted(async () => {
  try {
    const data = await fetchPersonalCenterData();
    profile.value = data.profile;
    stats.value = data.stats;
  } catch (error) {
    ElMessage.error(error.message || "获取个人中心信息失败");
  }
});

function handleEditClick() {
  editForm.value = {
    username: profile.value.username || "",
    phone: profile.value.phone || "",
    email: profile.value.email || "",
    avatar: profile.value.avatar || "",
  };
  previewAvatar.value = profile.value.avatar || "";
  isEditing.value = true;
}

function handleCancelClick() {
  isEditing.value = false;
  previewAvatar.value = "";
  editForm.value = {
    username: "",
    phone: "",
    email: "",
    avatar: "",
  };
}

async function handleAvatarUpload(event) {
  const file = event.target.files[0];
  if (!file) return;

  if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
    ElMessage.error('仅支持 JPG/PNG 格式');
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过 5MB');
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    previewAvatar.value = e.target.result;
  };
  reader.readAsDataURL(file);

  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await axiosInstance.post("/api/common/upload", formData);
    const path = res.data.data.url;
    editForm.value.avatar = `http://localhost:8080${path}`;
    ElMessage.success("头像上传成功");
  } catch (err) {
    ElMessage.error("头像上传失败");
    previewAvatar.value = "";
  }
}

async function handleSaveClick() {
  if (!editForm.value.username?.trim()) {
    ElMessage.warning("用户名不能为空");
    return;
  }

  isSaving.value = true;
  try {
    const profileData = {
      username: editForm.value.username.trim(),
      phone: editForm.value.phone || null,
      email: editForm.value.email || null,
      avatar: editForm.value.avatar || null,
    };

    const updated = await updateProfile(profileData);
    profile.value = updated;
    isEditing.value = false;
    ElMessage.success("保存成功");
  } catch (error) {
    ElMessage.error("保存失败");
  } finally {
    isSaving.value = false;
  }
}

function handleLogout() {
  logoutAccount();
  ElMessage.success("已退出登录");
  router.replace({ name: "Login" });
}
</script>

<style scoped>
.hero-layout,
.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 20px;
}

.hero-layout {
  display: flex;
  flex-direction: row-reverse;
  gap: 20px;
  align-items: start;
}

.hero-left {
  flex: 1;
}

.hero-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-side,
.profile-card,
.stats-card {
  padding: 24px;
}

.profile-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #efb566 0%, #d97706 100%);
  color: #fff;
  font-family: var(--font-display);
  font-size: 2.4rem;
  overflow: hidden;
}

.avatar-upload {
  width: 96px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-btn {
  width: 96px;
  height: 96px;
  border-radius: 24px;
  border: 2px dashed var(--color-border);
  background: var(--color-bg-soft);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  color: var(--color-primary);
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-btn:hover {
  border-color: var(--color-primary);
  background: rgba(59, 130, 246, 0.05);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin: 0;
  white-space: nowrap;
}

.profile-fields {
  display: grid;
  gap: 14px;
}

.field-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--color-bg-soft);
}

.field-row strong {
  color: var(--color-heading);
}

.field-label {
  color: var(--color-text-muted);
  white-space: nowrap;
}

.section-head {
  margin-bottom: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-btn {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.edit-btn:hover {
  background: var(--color-bg-soft);
  color: var(--color-primary);
}

.field-input {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  background: var(--color-bg);
  color: var(--color-text);
}

.field-input::placeholder {
  color: var(--color-text-muted);
}

.field-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.field-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.pill-button {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.is-primary {
  background: #d97706 !important;
  color: white !important;
  font-weight: bold;
}

.is-primary:hover:not(:disabled) {
  background: #b86600 !important;
}

.is-secondary {
  background: var(--color-bg-soft);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.is-secondary:hover:not(:disabled) {
  background: var(--color-bg-lighter);
}

.pill-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.stats-grid {
  display: grid;
  gap: 14px;
}

.stat-box {
  padding: 18px;
  border-radius: 20px;
  background: var(--color-bg-soft);
  display: grid;
  gap: 10px;
}

.stat-box strong {
  font-family: var(--font-display-alt);
  font-size: 1.5rem;
  color: var(--color-heading);
}

/* 退出按钮样式 */
.logout-btn-top {
  padding: 10px 22px;
  border: none;
  border-radius: 16px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #b93829;
  color: #ffffff;
  white-space: nowrap;
}

.logout-btn-top:hover {
  background: #a02e21;
  transform: translateY(-1px);
}

@media (max-width: 980px) {
  .hero-layout {
    flex-direction: column;
  }
  .hero-right {
    width: 100%;
    justify-content: space-between;
  }

  .content-grid,
  .profile-main {
    grid-template-columns: 1fr;
  }
}
</style>