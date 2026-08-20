import api from "./index";

/**
 * 获取成长曲线数据
 * 返回按日期聚合的面试数据和评分趋势
 */
export async function fetchGrowthCurveData() {
  const resp = await api.get("/api/v1/scores/growth-curve");
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "获取成长曲线失败");
}
