import axios from 'axios'

export async function sendUserMessage(message) {
  const resp = await axios.post('/api/v1/ai/respond', { message })
  if (resp && resp.data && resp.data.code === 200) return resp.data.data.reply
  throw new Error('AI 接口返回异常')
}

export async function fetchQuestions() {
  const resp = await axios.get('/api/v1/question')
  if (resp && resp.data && resp.data.code === 200) return resp.data.data
  return []
}

export async function sendVoiceAudio(formData) {
  const resp = await axios.post('/api/v1/ai/voice', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  if (resp && resp.data && resp.data.code === 200) return resp.data.data
  throw new Error('语音接口返回异常')
}
