import api from "./index";

export async function startInterview(payload) {
  const resp = await api.post("/api/v1/interviews/start", payload);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "创建面试失败");
}

export async function sendInterviewMessage(interviewId, payload) {
  const resp = await api.post(
    `/api/v1/interviews/${interviewId}/messages`,
    payload,
  );
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "发送消息失败");
}

/**
 * ===== NEW: 提交语音回答 =====
 * 调用后端 /api/v1/interviews/{interviewId}/submitVoice
 * @param {Number} interviewId - 面试ID
 * @param {Number} questionId - 题目ID
 * @param {File} audioBlob - 音频文件
 */
export async function submitVoiceAnswer(interviewId, questionId, audioBlob) {
  const formData = new FormData();
  formData.append("file", audioBlob, "answer.wav");
  
  const resp = await api.post(
    `/api/v1/interviews/${interviewId}/submitVoice?questionId=${questionId}`,
    formData,
  );
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "提交语音回答失败");
}

/**
 * ===== NEW: 提交文本回答 =====
 * 调用后端 /api/v1/interviews/{interviewId}/submitText
 * @param {Number} interviewId - 面试ID
 * @param {Number} questionId - 题目ID
 * @param {String} text - 用户的文本回答
 */
export async function submitTextAnswer(interviewId, questionId, text) {
  const resp = await api.post(
    `/api/v1/interviews/${interviewId}/submitText?questionId=${questionId}&text=${encodeURIComponent(text)}`,
    null,
  );
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "提交文本回答失败");
}

export async function completeInterview(interviewId) {
  const resp = await api.post(`/api/v1/interviews/${interviewId}/complete`);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "完成面试失败");
}

export async function abortInterview(interviewId) {
  const resp = await api.post(`/api/v1/interviews/${interviewId}/abort`);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "结束面试失败");
}

export async function fetchInterviewHistory(params = {}) {
  const resp = await api.get("/api/v1/interviews/history", { params });
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "获取历史记录失败");
}

export async function deleteInterview(interviewId) {
  const resp = await api.delete(`/api/v1/interviews/${interviewId}`);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "删除记录失败");
}
