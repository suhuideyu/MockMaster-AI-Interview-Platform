import api from "./index";

export async function login(username, password) {
  const resp = await api.post("/api/v1/auth/login", { username, password });
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "登录失败");
}

export async function register(payload) {
  const resp = await api.post("/api/v1/auth/register", payload);
  if (resp?.data?.code === 200) return resp.data.data;
  throw new Error(resp?.data?.msg || "注册失败");
}

export async function me() {
  const resp = await api.get("/api/v1/users/me");
  if (resp?.data?.code === 200) return resp.data.data;
  return null;
}
