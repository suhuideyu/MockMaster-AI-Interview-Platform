import api from "./index";

export async function fetchJobs() {
  const resp = await api.get("/api/v1/jobs");
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "获取岗位失败");
}
