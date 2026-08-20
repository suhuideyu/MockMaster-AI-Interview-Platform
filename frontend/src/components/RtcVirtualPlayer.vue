<template>
  <div class="avatar-container">
    <div class="player-wrapper">
      <div ref="playerWrapper" class="player-container"></div>
      <div class="status-bar">{{ statusText }}</div>
    </div>

    <div class="control-group">
      <button 
        class="btn start-btn" 
        @click="startAvatar" 
        :disabled="isLoading || isPlaying"
      >
        {{ isLoading ? '启动中...' : '启动虚拟人' }}
      </button>
      <button 
        class="btn stop-btn" 
        @click="stopAvatar" 
        :disabled="!isPlaying"
      >
        停止播放
      </button>
    </div>

    <div class="log-panel">
      <div 
        v-for="(log, idx) in logList" 
        :key="idx" 
        class="log-item"
        :class="log.type"
      >
        {{ log.time }} {{ log.msg }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, defineExpose } from 'vue'
import axios from 'axios'

import { RTCPlayer } from '../api/rtcplayer.esm.js'

const emit = defineEmits(['ai-question'])
const playerWrapper = ref(null)
const isLoading = ref(false)
const isPlaying = ref(false)
const logList = ref([])
const statusText = ref("等待连接")

let player = null
const API_PREFIX = 'http://localhost:8080/api/avatar'

function addLog(msg, type = 'info') {
  const time = new Date().toLocaleTimeString()
  logList.value.unshift({ time, msg, type })
}

async function startAvatar() {
  if (isLoading.value || isPlaying.value) return
  isLoading.value = true
  statusText.value = "启动中..."
  addLog("开始启动虚拟人")

  try {
    await axios.get(API_PREFIX + "/start")
    await new Promise(r => setTimeout(r, 1500))
    const res = await axios.get(API_PREFIX + "/stream")
    
    initPlayer(res.data.data)
    isPlaying.value = true
    addLog("✅ 启动完成，可以朗读", "success")
  } catch (e) {
    addLog("❌ 启动失败", "error")
    console.error(e)
  }

  isLoading.value = false
}

function initPlayer(stream) {
  if (player) {
    player.stop()
    player = null
  }

  player = new RTCPlayer()
  player.playerType = 12
  player.stream = {
    sid: stream.sid,
    appid: 1000000001,
    server: stream.server,
    auth: stream.auth,
    userId: stream.userId,
    roomId: stream.roomId,
    timeStr: stream.timeStr
  }

  player.videoSize = { width: 720, height: 1280 }
  player.container = playerWrapper.value
  player.on('playing', () => {
    statusText.value = "已连接"
    addLog("🎬 虚拟人已就绪", "success")
  })
  player.play()
}

async function sendText(text) {
  if (!text || !isPlaying.value) return
  try {
    await axios.post(API_PREFIX + "/send-text", { text })
    addLog(`📤 已发送：${text}`)
  } catch (e) {
    addLog("❌ 发送失败", "error")
  }
}

async function externalSpeak(text) {
  if (!text || !isPlaying.value) {
    addLog("⚠️ 请先启动虚拟人", "warning")
    return
  }
  await sendText(text)
  emit('ai-question', text)
}

function stopAvatar() {
  try {
    if (player) player.stop()
  } catch (e) {}

  isPlaying.value = false
  statusText.value = "已停止"
  addLog("🔌 已断开虚拟人", "success")
}

onUnmounted(() => {
  stopAvatar()
})

defineExpose({
  externalSpeak,
  isPlaying
})
</script>

<style scoped>
.avatar-container {
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
  padding: 10px;
  box-sizing: border-box;
}

.player-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.player-container {
  width: 320px;
  height: 480px;
  background: #000;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.status-bar {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}

.control-group {
  margin: 14px 0;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn {
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.start-btn {
  background: #42b983;
  color: white;
}

.stop-btn {
  background: #ff4757;
  color: white;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.log-panel {
  height: 180px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  background: #f9f9f9;
}

.log-item {
  margin-bottom: 4px;
  line-height: 1.3;
}

.success {
  color: #009e59;
}

.error {
  color: #e53935;
}

.warning {
  color: #ff8f00;
}
</style>