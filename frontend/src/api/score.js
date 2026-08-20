import api from "./index";

/**
 * 获取评分概览
 * 返回各维度的平均分、总分、模式统计等
 */
export async function fetchScoreOverview() {
  const resp = await api.get("/api/v1/scores/overview");
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "获取评分概览失败");
}

/**
 * 获取单场面试的详细评分
 */
export async function fetchInterviewScoreDetail(interviewId) {
  const resp = await api.get(`/api/v1/scores/interviews/${interviewId}`);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "获取评分详情失败");
}
