<template>
  <div class="spark-chat-container">
    <div class="input-area">
      <textarea
        v-model="inputText"
        placeholder="请输入您的问题..."
        class="input-textarea"
      ></textarea>
      <button
        @click="sendMessage"
        :disabled="!inputText.trim() || isLoading"
        class="submit-btn"
      >
        {{ isLoading ? '发送中...' : '立即提问' }}
      </button>
    </div>
    <div class="result-area" v-html="resultText"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import CryptoJS from 'crypto-js';
import { Base64 } from 'js-base64';

const appId = "2c46d962";
const apiKey = "123";
const apiSecret = "123";

const host = "spark-api.xf-yun.com";
const path = "/v1.1/chat";

const inputText = ref('');
const resultText = ref('');
const isLoading = ref(false);

async function getWebsocketUrl() {
  const date = new Date().toUTCString();
  const signatureOrigin = `host: ${host}\ndate: ${date}\nGET ${path} HTTP/1.1`;
  const signatureSha = CryptoJS.HmacSHA256(signatureOrigin, apiSecret);
  const signature = CryptoJS.enc.Base64.stringify(signatureSha);

  const authorizationOrigin = `api_key="${apiKey}", algorithm="hmac-sha256", headers="host date request-line", signature="${signature}"`;
  const authStr = Base64.encode(authorizationOrigin);

  return `wss://${host}${path}?authorization=${encodeURIComponent(authStr)}&date=${encodeURIComponent(date)}&host=${host}`;
}

async function sendMessage() {
  const text = inputText.value.trim();
  if (!text) {
    alert("请输入内容！");
    return;
  }

  resultText.value = "连接中...";
  isLoading.value = true;

  try {
    const url = await getWebsocketUrl();
    const ws = new WebSocket(url);

    ws.onopen = () => {
      const params = {
        header: { app_id: appId, uid: "12345" },
        parameter: {
          chat: { domain: "general", temperature: 0.7, max_tokens: 1024 }
        },
        payload: {
          message: {
            text: [{ role: "user", content: text }]
          }
        }
      };
      ws.send(JSON.stringify(params));
    };

    ws.onmessage = (e) => {
      const data = JSON.parse(e.data);
      if (data.header.code !== 0) {
        let errorText = `接口错误：${data.header.message} (${data.header.code})`;
        if (data.header.code === 10100013 || /AppIdNoAuthError/i.test(data.header.message)) {
          errorText += "。AppId 未开通或未授权，请前往讯飞控制台检查应用权限";
        }
        resultText.value = errorText;
        ws.close();
        return;
      }
      const content = data.payload.choices.text[0].content;
      resultText.value += content;
      if (data.payload.choices.status === 2) {
        ws.close();
        resultText.value += "\n\n--- 回答结束 ---";
      }
    };

    ws.onerror = (e) => {
      resultText.value = `连接失败：密钥错误 / 接口权限未开 / 版本不匹配`;
    };

    ws.onclose = () => {
      isLoading.value = false;
    };

  } catch (err) {
    resultText.value = "生成签名失败：" + err.message;
    isLoading.value = false;
  }
}
</script>

<style scoped>
.spark-chat-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.input-area {
  margin-bottom: 20px;
}

.input-textarea {
  width: 100%;
  height: 80px;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.submit-btn {
  padding: 10px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}

.submit-btn:hover:not(:disabled) {
  background: #0056b3;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result-area {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  min-height: 300px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>