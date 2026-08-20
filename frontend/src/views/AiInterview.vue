<template>
  <div class="interview-page">
    <section class="session-shell">
      <header class="session-head">
        <div class="session-copy">
          <p class="eyebrow">
            {{ currentMode === "voice" ? "语音面试" : "文本面试" }}
          </p>
          <h1>{{ currentJobName }}</h1>
          <p>
            {{ difficultyText }} · {{ plannedDuration }} 分钟 ·
            {{ currentModeLabel }}
          </p>
        </div>
        <div class="session-meta">
          <div class="time-pill">{{ formatTime(timer) }}</div>
          <button class="pill-button" @click="exitInterview">提前退出</button>
        </div>
      </header>

      <section v-if="currentMode === 'voice'" class="voice-layout">
        <div class="voice-panel page-card">
          <div class="panel-head">
            <span class="soft-badge">AI 面试官</span>
            <button class="pill-button" @click="togglePause">
              {{ isPaused ? "继续面试" : "暂停面试" }}
            </button>
          </div>

          <RtcVirtualPlayer
            ref="virtualPlayerRef"
            :session-config="simulatorSession"
            title="接入中"
            @ai-question="onVoiceAiQuestion"
          />
        </div>

        <aside class="transcript-panel page-card">
          <div class="panel-head">
            <span class="soft-badge">面试文字稿</span>
          </div>

          <div class="chat-list">
            <article
              v-for="(msg, index) in chatList"
              :key="index"
              class="chat-card"
              :class="msg.role === 'AI' ? 'ai-card' : 'user-card'"
            >
              <span class="chat-role">{{
                msg.role === "AI" ? "AI 面试官" : "你的回答"
              }}</span>
              <p>{{ msg.content }}</p>
            </article>
          </div>

          <div class="voice-recording-status" v-if="isRecording">
            <span class="recording-indicator"></span>
            <span>正在录音... {{ formatTime(recordingDuration) }}</span>
          </div>

          <div class="voice-actions">
            <button
              class="flat-rounded-button"
              :class="{ 'is-recording': isRecording }"
              @click="toggleRecording"
              :disabled="isPaused || finished || isSpeaking"
            >
              {{ isRecording ? "停止录音" : "开始录音" }}
            </button>

            <button
              class="flat-rounded-button"
              @click="handleSubmitVoiceAnswer"
              :disabled="
                !recordingBlob ||
                sending ||
                isPaused ||
                finished ||
                isSpeaking ||
                isRecording
              "
            >
              {{
                sending
                  ? "提交中..."
                  : recordingBlob
                    ? "提交本轮回答"
                    : "请先录音"
              }}
            </button>
          </div>
        </aside>
      </section>

      <section v-else class="text-layout">
        <div class="dialog-panel page-card">
          <div class="panel-head">
            <span class="soft-badge">对话</span>
            <button class="pill-button" @click="togglePause">
              {{ isPaused ? "继续面试" : "暂停面试" }}
            </button>
          </div>

          <div class="chat-list" ref="chatListRef">
            <article
              v-for="(msg, index) in chatList"
              :key="index"
              class="chat-card"
              :class="msg.role === 'AI' ? 'ai-card' : 'user-card'"
            >
              <span class="chat-role">{{
                msg.role === "AI" ? "AI 面试官" : "你的回答"
              }}</span>
              <p>{{ msg.content }}</p>
            </article>
          </div>
        </div>

        <aside class="answer-panel page-card">
          <div class="panel-head">
            <span class="soft-badge">回答</span>
            <span class="muted">{{
              sending ? "正在生成回答..." : "可多行输入，Enter 发送"
            }}</span>
          </div>

          <div class="prompt-card">
            <span class="data-label">当前问题</span>
            <p>{{ currentQuestion }}</p>
          </div>

          <textarea
            v-model="inputText"
            class="answer-textarea"
            placeholder="输入你的回答，尽量交代背景、行动、结果和复盘。"
            :disabled="isPaused || finished || sending"
            @keydown.enter.exact.prevent="sendMessage"
          />

          <div class="answer-actions">
            <button
              class="pill-button is-primary"
              :disabled="!inputText.trim() || isPaused || finished || sending"
              @click="sendMessage"
            >
              {{ sending ? "生成中..." : "发送回答" }}
            </button>
          </div>
        </aside>
      </section>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { fetchJobs } from "../api/job";
import {
  abortInterview,
  completeInterview,
  sendInterviewMessage,
  startInterview,
  submitVoiceAnswer,
  submitTextAnswer,
} from "../api/interview";
import { fetchSimulatorSession } from "../api/simulator";
import RtcVirtualPlayer from "../components/RtcVirtualPlayer.vue";
import CryptoJS from "crypto-js";
import { Base64 } from "js-base64";

const APPID = "2c46d962";
const API_SECRET = "MzU2Nzc3NjU2Y2NkMzM3ZjVjZDY1NjAx";
const API_KEY = "37aa1faaabbdaa9439ca7d89a259b3f5";

const SPARK_CONFIG = {
  url: "wss://spark-api.xf-yun.com/v1/x1",
  domain: "spark-x",
};

const router = useRouter();
const route = useRoute();
const virtualPlayerRef = ref(null);

const currentMode = ref(route.query.mode || "text");
const plannedDuration = ref(Number(route.query.duration || 20));
const difficulty = ref(String(route.query.difficulty || "medium"));
const jobId = ref(Number(route.query.jobId || 1));
const jobs = ref([]);
const interviewId = ref(null);
const sending = ref(false);
const finished = ref(false);
const isPaused = ref(false);
const timer = ref(0);
const timerInterval = ref(null);
const simulatorSession = ref(null);

const chatList = ref([]);
const inputText = ref("");
const chatListRef = ref(null);

const audioStream = ref(null);
const isRecording = ref(false);
const recordingDuration = ref(0);
const recordingTimer = ref(null);
const recordingBlob = ref(null);
const isSpeaking = ref(false);
const currentQuestionId = ref(null);

let pcmData = [];
let audioContext = null;
let sourceNode = null;
let scriptNode = null;

const difficultyText = computed(
  () =>
    ({
      easy: "简单难度",
      medium: "中等难度",
      hard: "困难难度",
    })[difficulty.value] || "中等难度",
);

const currentModeLabel = computed(() =>
  currentMode.value === "voice" ? "语音面试" : "文本面试",
);

const currentJobName = computed(
  () =>
    jobs.value.find((item) => item.id === jobId.value)?.jobName || "目标岗位",
);

const currentQuestion = computed(() => {
  const latestAi = [...chatList.value]
    .reverse()
    .find((item) => item.role === "AI");
  return latestAi?.content || "AI 正在准备提问...";
});

onMounted(async () => {
  try {
    jobs.value = await fetchJobs();
    await initInterview();
  } catch (error) {
    ElMessage.error(error.message || "面试初始化失败");
    router.replace({ name: "AiInterviewSelect", query: { ...route.query } });
  }
});

onUnmounted(() => {
  clearInterval(timerInterval.value);
  clearInterval(recordingTimer.value);
  stopRecording();
  if (audioStream.value) {
    audioStream.value.getTracks().forEach((track) => track.stop());
  }
});

watch(
  () => route.query,
  async (query) => {
    currentMode.value = query.mode || "text";
    plannedDuration.value = Number(route.query.duration || 20);
    difficulty.value = String(route.query.difficulty || "medium");
    jobId.value = Number(route.query.jobId || 1);
    await initInterview();
  },
);

async function initInterview() {
  resetInterviewState();
  const result = await startInterview({
    jobId: jobId.value,
    difficulty: difficulty.value,
    duration: plannedDuration.value,
    mode: currentMode.value,
  });
  interviewId.value = result.interviewId;
  currentQuestionId.value = result.questionId;
  chatList.value = [{ role: "AI", content: result.openingQuestion }];

  if (currentMode.value === "voice") {
    simulatorSession.value = await fetchSimulatorSession().catch(() => null);
  }

  timer.value = plannedDuration.value * 60;
  startTimer();
  scrollToBottom();
}

function startTimer() {
  clearInterval(timerInterval.value);
  timerInterval.value = setInterval(async () => {
    if (isPaused.value || finished.value) return;
    timer.value -= 1;
    if (timer.value <= 0) {
      await finalizeInterview();
    }
  }, 1000);
}

function resetInterviewState() {
  clearInterval(timerInterval.value);
  clearInterval(recordingTimer.value);
  interviewId.value = null;
  sending.value = false;
  finished.value = false;
  isPaused.value = false;
  isSpeaking.value = false;
  timer.value = 0;
  simulatorSession.value = null;
  chatList.value = [];
  inputText.value = "";
  isRecording.value = false;
  recordingDuration.value = 0;
  recordingBlob.value = null;
  currentQuestionId.value = null;
  stopRecording();
}

function formatTime(seconds) {
  const min = Math.floor(seconds / 60)
    .toString()
    .padStart(2, "0");
  const sec = (seconds % 60).toString().padStart(2, "0");
  return `${min}:${sec}`;
}

function togglePause() {
  if (finished.value) return;
  isPaused.value = !isPaused.value;
  if (isRecording.value && isPaused.value) {
    stopRecording();
    isRecording.value = false;
  }
}

async function initAudioRecording() {
  try {
    audioStream.value = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true,
      },
    });

    audioContext = new AudioContext({ sampleRate: 16000 });
    sourceNode = audioContext.createMediaStreamSource(audioStream.value);
    scriptNode = audioContext.createScriptProcessor(4096, 1, 1);

    pcmData = [];

    scriptNode.onaudioprocess = (event) => {
      const channelData = event.inputBuffer.getChannelData(0);
      const int16Array = new Int16Array(channelData.length);
      for (let i = 0; i < channelData.length; i++) {
        const s = channelData[i];
        int16Array[i] = s < 0 ? s * 32768 : s * 32767;
      }
      pcmData.push(...int16Array);
    };

    sourceNode.connect(scriptNode);
    scriptNode.connect(audioContext.destination);
  } catch (error) {
    ElMessage.error("无法获取麦克风权限: " + error.message);
    throw error;
  }
}

async function toggleRecording() {
  if (!isRecording.value) {
    try {
      await initAudioRecording();
      recordingBlob.value = null;
      recordingDuration.value = 0;
      isRecording.value = true;

      clearInterval(recordingTimer.value);
      recordingTimer.value = setInterval(() => {
        recordingDuration.value += 1;
      }, 1000);
    } catch (error) {
      ElMessage.error("启动录音失败");
      isRecording.value = false;
    }
  } else {
    stopRecording();
    isRecording.value = false;
  }
}

function stopRecording() {
  clearInterval(recordingTimer.value);

  if (audioContext) {
    audioContext.close();
    audioContext = null;
  }
  if (audioStream.value) {
    audioStream.value.getTracks().forEach((track) => track.stop());
    audioStream.value = null;
  }

  recordingBlob.value = createWavBlob(pcmData, 16000, 1);
}

function createWavBlob(pcmData, sampleRate = 16000, numChannels = 1) {
  const bitsPerSample = 16;
  const bytePerSample = bitsPerSample / 8;
  const dataLength = pcmData.length * bytePerSample;

  const buffer = new ArrayBuffer(44 + dataLength);
  const view = new DataView(buffer);

  function writeString(pos, str) {
    for (let i = 0; i < str.length; i++) {
      view.setUint8(pos + i, str.charCodeAt(i));
    }
  }

  writeString(0, "RIFF");
  view.setUint32(4, 36 + dataLength, true);
  writeString(8, "WAVE");

  writeString(12, "fmt ");
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * numChannels * bytePerSample, true);
  view.setUint16(32, numChannels * bytePerSample, true);
  view.setUint16(34, bitsPerSample, true);

  writeString(36, "data");
  view.setUint32(40, dataLength, true);

  let offset = 44;
  for (let sample of pcmData) {
    view.setInt16(offset, sample, true);
    offset += 2;
  }

  return new Blob([buffer], { type: "audio/wav" });
}

async function speakQuestion(text) {
  return new Promise((resolve) => {
    if (!window.speechSynthesis) {
      resolve();
      return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "zh-CN";
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    utterance.onend = () => {
      isSpeaking.value = false;
      recordingBlob.value = null;
      recordingDuration.value = 0;
      resolve();
    };
    utterance.onerror = () => {
      isSpeaking.value = false;
      resolve();
    };
    window.speechSynthesis.speak(utterance);
  });
}

function getWebsocketUrl() {
  return new Promise((resolve, reject) => {
    try {
      const urlObj = new URL(SPARK_CONFIG.url.replace("wss:", "https:"));
      const host = urlObj.host;
      const path = urlObj.pathname;
      const date = new Date().toGMTString();
      const algorithm = "hmac-sha256";
      const headers = "host date request-line";

      const signatureOrigin = `host: ${host}\ndate: ${date}\nGET ${path} HTTP/1.1`;
      const signatureSha = CryptoJS.HmacSHA256(signatureOrigin, API_SECRET);
      const signature = CryptoJS.enc.Base64.stringify(signatureSha);
      const authorizationOrigin = `api_key="${API_KEY}", algorithm="${algorithm}", headers="${headers}", signature="${signature}"`;
      const authorization = Base64.encode(authorizationOrigin);

      const finalUrl = `${SPARK_CONFIG.url}?authorization=${encodeURIComponent(authorization)}&date=${encodeURIComponent(date)}&host=${encodeURIComponent(host)}`;
      resolve(finalUrl);
    } catch (err) {
      reject(new Error("签名失败：" + err.message));
    }
  });
}

async function sendToSpark(question) {
  return new Promise((resolve, reject) => {
    let totalRes = "";
    let ws = null;

    getWebsocketUrl()
      .then((url) => {
        ws = new WebSocket(url);
        ws.onopen = () => {
          const params = {
            header: { app_id: APPID, uid: `user_${Date.now()}` },
            parameter: {
              chat: {
                domain: SPARK_CONFIG.domain,
                temperature: 0.5,
                max_tokens: 1024,
              },
            },
            payload: {
              message: {
                text: [{ role: "user", content: question }],
              },
            },
          };
          ws.send(JSON.stringify(params));
        };
        ws.onmessage = (e) => {
          try {
            const data = JSON.parse(e.data);
            if (data.header.code !== 0)
              throw new Error(`(${data.header.code}) ${data.header.message}`);
            const content = data.payload?.choices?.text[0]?.content || "";
            totalRes += content;
            if (data.header.status === 2) ws.close();
          } catch (err) {
            ws.close();
            reject(err);
          }
        };
        ws.onclose = () => resolve(totalRes);
        ws.onerror = () => reject(new Error("WebSocket 连接失败"));
      })
      .catch(reject);
  });
}

async function sendMessage() {
  const content = inputText.value.trim();
  if (!content || sending.value || isPaused.value || finished.value) return;

  chatList.value.push({ role: "USER", content });
  inputText.value = "";
  sending.value = true;
  scrollToBottom();

  try {
    if (currentMode.value === "text") {
      const aiResponse = await submitTextAnswer(
        interviewId.value,
        currentQuestionId.value,
        content,
      );

      chatList.value.push({
        role: "SCORE",
        content: `✓ 本轮评分：${aiResponse.totalScore.toFixed(2)} 分`,
      });

      if (aiResponse.nextQuestionId && aiResponse.nextTitle) {
        chatList.value.push({ role: "AI", content: aiResponse.nextTitle });
        currentQuestionId.value = aiResponse.nextQuestionId;
        if (virtualPlayerRef.value) {
          await virtualPlayerRef.value.externalSpeak(aiResponse.nextTitle);
        }
      } else {
        chatList.value.push({
          role: "AI",
          content: aiResponse.nextTitle || "面试已结束",
        });
        await finalizeInterview();
      }
    } else {
      const res = await sendInterviewMessage(interviewId.value, {
        content,
        mode: "voice",
      });
      chatList.value.push({ role: "AI", content: res.reply });
    }
    scrollToBottom();
  } catch (error) {
    ElMessage.error(error.message || "发送失败");
  } finally {
    sending.value = false;
  }
}

async function handleSubmitVoiceAnswer() {
  if (
    !recordingBlob.value ||
    sending.value ||
    isPaused.value ||
    finished.value ||
    isSpeaking.value ||
    isRecording.value
  ) {
    return;
  }

  sending.value = true;

  try {
    const res = await submitVoiceAnswer(
      interviewId.value,
      currentQuestionId.value,
      recordingBlob.value,
    );

    chatList.value.push({
      role: "USER",
      content: res.userText || "已完成语音回答",
    });

    chatList.value.push({
      role: "SCORE",
      content: `✓ 本轮评分：${res.totalScore.toFixed(2)} 分`,
    });

    if (res.nextTitle && res.nextQuestionId) {
      chatList.value.push({ role: "AI", content: res.nextTitle });
      currentQuestionId.value = res.nextQuestionId;

      if (virtualPlayerRef.value) {
        await virtualPlayerRef.value.externalSpeak(res.nextTitle);
      }

      recordingBlob.value = null;
      recordingDuration.value = 0;
    } else {
      ElMessage.info(res.nextTitle || "面试已结束");
      await finalizeInterview();
    }

    scrollToBottom();
  } catch (error) {
    ElMessage.error(error.message || "提交失败");
  } finally {
    sending.value = false;
  }
}

function onVoiceAiQuestion(question) {
  if (!question) return;

  const exists = chatList.value.some(
    (m) => m.role === "AI" && m.content === question,
  );
  if (!exists) {
    chatList.value.push({ role: "AI", content: question });
    scrollToBottom();
  }
}

async function finalizeInterview() {
  if (finished.value || !interviewId.value) return;
  finished.value = true;
  clearInterval(timerInterval.value);

  try {
    await completeInterview(interviewId.value);
    ElMessage.success("面试已完成");
    router.replace({ name: "InterviewHistory" });
  } catch (error) {
    finished.value = false;
    startTimer();
    ElMessage.error("保存失败");
  }
}

async function exitInterview() {
  clearInterval(timerInterval.value);
  stopRecording();
  if (audioStream.value) {
    audioStream.value.getTracks().forEach((track) => track.stop());
  }
  if (interviewId.value && !finished.value) {
    try {
      await abortInterview(interviewId.value);
      ElMessage.info("已提前退出");
    } catch (error) {
      ElMessage.error("退出失败");
    }
  }
  router.replace({ name: "AiInterviewSelect", query: { ...route.query } });
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatListRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

watch(
  () => virtualPlayerRef.value?.isPlaying,
  async (isPlaying) => {
    if (isPlaying && currentQuestion.value && currentMode.value === "voice") {
      await virtualPlayerRef.value.externalSpeak(currentQuestion.value);
    }
  },
);
</script>

<style scoped>
.interview-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgb(201 111 59 / 0.1), transparent 24%),
    radial-gradient(
      circle at top right,
      rgb(234 215 196 / 0.4),
      transparent 26%
    ),
    var(--color-bg);
  box-sizing: border-box;
  overflow: visible;
  display: grid;
  grid-template-rows: 1fr;
}

.session-shell {
  width: min(1280px, 100%);
  height: 100%;
  margin: 0 auto;
  display: grid;
  gap: 16px;
  grid-template-rows: auto 1fr;
  min-height: 0;
}

.session-head {
  padding: 20px 24px;
  border-radius: 28px;
  background: rgb(255 252 247 / 0.92);
  border: 1px solid rgb(226 203 169 / 0.7);
  box-shadow: 0 20px 50px rgb(111 70 26 / 0.08);
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
}

.session-copy h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 2.8rem);
  line-height: 1.05;
  color: var(--color-heading);
}

.session-copy p:last-child {
  margin: 6px 0 0;
  color: var(--color-text-muted);
}

.session-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.time-pill {
  min-width: 112px;
  min-height: 48px;
  padding: 0 18px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: #fff2dc;
  color: #8f5417;
  font-family: var(--font-display-alt);
  font-size: 1.45rem;
}

.voice-layout,
.text-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 16px;
  min-height: auto;
}

.text-layout {
  height: 100%;
  min-height: 0;
}

.voice-panel {
  min-height: 0;
  height: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: visible;
}

.dialog-panel {
  min-height: 0;
  height: auto;
  padding: 18px 18px 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  overflow: visible;
}

/* 只作用于文本模式，语音完全不变 */
.text-layout .dialog-panel {
  height: 65vh;
  display: flex;
  flex-direction: column;
}
.text-layout .dialog-panel .chat-list {
  flex: 1;
  overflow-y: auto;
}

.transcript-panel {
  min-height: 0;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 600px;
  overflow: hidden;
}

.transcript-panel .chat-list {
  overflow-y: auto;
  overflow-x: hidden;
}

/* 自定义滚动条样式 */
.transcript-panel .chat-list::-webkit-scrollbar {
  width: 6px;
}

.transcript-panel .chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.transcript-panel .chat-list::-webkit-scrollbar-thumb {
  background: rgb(203 174 131 / 0.4);
  border-radius: 3px;
  transition: background 0.3s ease;
}

.transcript-panel .chat-list::-webkit-scrollbar-thumb:hover {
  background: rgb(203 174 131 / 0.6);
}

.answer-panel {
  min-height: 0;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: visible;
}

.transcript-panel .panel-head {
  display: block !important;
  text-align: left !important;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.prompt-card {
  padding: 14px 16px;
  border-radius: 20px;
  background: var(--color-bg-soft);
}

.prompt-card p {
  margin: 8px 0 0;
  color: var(--color-text);
  line-height: var(--leading-cn-normal);
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
  padding-right: 8px;
}

.chat-card {
  padding: 16px 18px;
  border-radius: 20px;
  display: grid;
  gap: 8px;
}

.ai-card {
  background: var(--color-bg-soft);
}

.user-card {
  background: linear-gradient(
    135deg,
    rgb(255 243 224) 0%,
    rgb(249 227 196) 100%
  );
}

.chat-role {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-accent-strong);
}

.chat-card p {
  margin: 0;
  color: var(--color-text);
  line-height: var(--leading-cn-normal);
}

.answer-textarea {
  width: 100%;
  min-height: 220px;
  height: auto;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: var(--color-panel);
  padding: 16px 18px;
  resize: vertical;
  color: var(--color-text);
  line-height: var(--leading-cn-normal);
  outline: none;
}

.answer-textarea:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 4px rgb(217 119 6 / 0.08);
}

.answer-actions,
.voice-actions {
  display: flex;
  justify-content: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.flat-rounded-button {
  width: fit-content !important;
  padding: 8px 16px !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, rgb(255 180 77) 0%, rgb(255 152 0) 100%);
  color: white;
  border: none;
  font-size: 0.9rem !important;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  line-height: 1.2 !important;
}

.flat-rounded-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgb(255 152 0 / 0.3);
}

.flat-rounded-button:active:not(:disabled) {
  transform: translateY(0);
}

.flat-rounded-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.voice-recording-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgb(255 193 7 / 0.1);
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--color-text);
}

.recording-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff5252;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.flat-rounded-button.is-recording {
  background: linear-gradient(135deg, rgb(255 82 82) 0%, rgb(255 52 52) 100%);
}

.flat-rounded-button.is-recording:hover:not(:disabled) {
  box-shadow: 0 4px 8px rgb(255 52 52 / 0.3);
}

.voice-panel :deep(.player-shell) {
  gap: 8px;
}

.voice-panel :deep(.player-container),
.voice-panel :deep(.player-placeholder) {
  width: min(100%, 220px);
}

.voice-panel :deep(.placeholder-stage) {
  min-height: 220px;
}

@media (max-width: 1024px) {
  .voice-layout,
  .text-layout {
    grid-template-columns: 1fr;
  }

  .session-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .session-meta {
    align-self: flex-start;
  }
}
</style>
