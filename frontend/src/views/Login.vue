<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="brand-block">
        <p class="brand-tag">智面AI_MockMaster</p>
        <h1>让每一次模拟面试都更接近真实发挥</h1>
        <p>以专业练习为基，陪你吃透岗位知识、从容应对每一次面试。
        </p>
      </div>

      <div class="form-card">
        <div class="tab-header">
          <button
            class="tab-item"
            :class="{ active: isLogin }"
            @click="isLogin = true"
          >
            登录
          </button>
          <button
            class="tab-item"
            :class="{ active: !isLogin }"
            @click="isLogin = false"
          >
            注册
          </button>
        </div>

        <div v-if="isLogin" class="form-content">
          <input
            v-model.trim="loginForm.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
          />
          <div class="password-input-wrapper">
            <input
              v-model.trim="loginForm.password"
              class="form-input"
              :type="loginPasswordVisible ? 'text' : 'password'"
              placeholder="请输入密码"
            />
            <button
              type="button"
              class="password-toggle-btn"
              :aria-label="loginPasswordVisible ? '隐藏密码' : '显示密码'"
              @click="loginPasswordVisible = !loginPasswordVisible"
            >
              <svg
                v-if="loginPasswordVisible"
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg
                v-else
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            </button>
          </div>
          <button class="primary-btn" :disabled="loading" @click="handleLogin">
            {{ loading ? "登录中..." : "点此登入" }}
          </button>
        </div>

        <div v-else class="form-content">
          <input
            v-model.trim="registerForm.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
          />
          <div class="password-input-wrapper">
            <input
              v-model.trim="registerForm.password"
              class="form-input"
              :type="registerPasswordVisible ? 'text' : 'password'"
              placeholder="请输入密码"
            />
            <button
              type="button"
              class="password-toggle-btn"
              :aria-label="registerPasswordVisible ? '隐藏密码' : '显示密码'"
              @click="registerPasswordVisible = !registerPasswordVisible"
            >
              <svg
                v-if="registerPasswordVisible"
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg
                v-else
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            </button>
          </div>
          <div class="password-input-wrapper">
            <input
              v-model.trim="registerForm.confirmPwd"
              class="form-input"
              :type="confirmPasswordVisible ? 'text' : 'password'"
              placeholder="请再次输入密码"
            />
            <button
              type="button"
              class="password-toggle-btn"
              :aria-label="confirmPasswordVisible ? '隐藏密码' : '显示密码'"
              @click="confirmPasswordVisible = !confirmPasswordVisible"
            >
              <svg
                v-if="confirmPasswordVisible"
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg
                v-else
                class="eye-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            </button>
          </div>
          <input
            v-model.trim="registerForm.phone"
            class="form-input"
            type="tel"
            placeholder="请输入手机号（可选）"
          />
          <input
            v-model.trim="registerForm.email"
            class="form-input"
            type="email"
            placeholder="请输入邮箱（可选）"
          />
          <button
            class="primary-btn"
            :disabled="loading"
            @click="handleRegister"
          >
            {{ loading ? "提交中..." : "注册并登录" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { login, register } from "../api/auth";

const router = useRouter();
const isLogin = ref(true);
const loading = ref(false);

const loginPasswordVisible = ref(false);
const registerPasswordVisible = ref(false);
const confirmPasswordVisible = ref(false);

const loginForm = ref({
  username: "",
  password: "",
});

const registerForm = ref({
  username: "",
  password: "",
  confirmPwd: "",
  phone: "",
  email: "",
});

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning("请输入用户名和密码");
    return;
  }

  loading.value = true;
  try {
    const data = await login(
      loginForm.value.username,
      loginForm.value.password,
    );
    localStorage.setItem("token", data.token);
    localStorage.setItem("userInfo", JSON.stringify(data.user));
    ElMessage.success("登录成功");
    router.push("/home");
  } catch (error) {
    ElMessage.error(error.message || "登录失败");
  } finally {
    loading.value = false;
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning("请输入完整注册信息");
    return;
  }
  if (registerForm.value.password !== registerForm.value.confirmPwd) {
    ElMessage.warning("两次输入的密码不一致");
    return;
  }

  loading.value = true;
  try {
    const data = await register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone || null,
      email: registerForm.value.email || null,
    });
    localStorage.setItem("token", data.token);
    localStorage.setItem("userInfo", JSON.stringify(data.user));
    ElMessage.success("注册成功，已自动登录");
    router.push("/home");
  } catch (error) {
    ElMessage.error(error.message || "注册失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background:
    radial-gradient(
      circle at top left,
      rgba(255, 193, 118, 0.32),
      transparent 28%
    ),
    linear-gradient(135deg, #f7efe2 0%, #f1e4d2 50%, #e8d6c0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.login-panel {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 24px;
}

.brand-block,
.form-card {
  background: rgba(255, 252, 247, 0.92);
  border: 1px solid rgba(226, 203, 169, 0.7);
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(111, 70, 26, 0.08);
}

.brand-block {
  padding: 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-tag {
  margin: 0 0 14px;
  color: #a16207;
  font-size: 14px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.brand-block h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.2;
  color: #2d2010;
}

.brand-block p:last-child {
  margin: 18px 0 0;
  line-height: 1.8;
  color: #705a42;
  max-width: 520px;
}

.form-card {
  padding: 28px;
}

.tab-header {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 24px;
}

.tab-item {
  border: 0;
  border-radius: 14px;
  background: #f7efe5;
  color: #7f6342;
  min-height: 46px;
  cursor: pointer;
  font-size: 15px;
}

.tab-item.active {
  background: linear-gradient(135deg, #efb566 0%, #d97706 100%);
  color: #fff;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-input {
  width: 100%;
  border: 1px solid #dfc9ab;
  background: #fffdf9;
  border-radius: 14px;
  padding: 14px 16px;
  box-sizing: border-box;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.form-input:-webkit-autofill,
.form-input:-webkit-autofill:hover,
.form-input:-webkit-autofill:focus,
.form-input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px #fffdf9 inset !important;
  box-shadow: 0 0 0 30px #fffdf9 inset !important;
}

.form-input:-webkit-autofill {
  -webkit-text-fill-color: #2d2010 !important;
}

.form-input:focus {
  border-color: #d97706;
  box-shadow: 0 0 0 4px rgba(217, 119, 6, 0.08);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper .form-input {
  padding-right: 48px;
}

.password-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f6342;
  transition: color 0.2s ease;
  width: 32px;
  height: 32px;
}

.password-toggle-btn:hover {
  color: #d97706;
}

.password-toggle-btn:focus {
  outline: none;
  color: #d97706;
}

.eye-icon {
  width: 20px;
  height: 20px;
  stroke-width: 2;
}

.primary-btn {
  border: 0;
  border-radius: 14px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #efb566 0%, #d97706 100%);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 920px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .brand-block h1 {
    font-size: 32px;
  }
}
</style>
