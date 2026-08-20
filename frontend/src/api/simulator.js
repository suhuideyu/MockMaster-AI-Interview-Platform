import api from "./index";

export async function fetchSimulatorSession() {
  try {
    const resp = await api.get("/api/v1/virtual-human/session");
    if (resp?.data?.code === 200) return resp.data.data || null;
    return null;
  } catch {
    return null;
  }
}
