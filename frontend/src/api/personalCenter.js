import { me } from "./auth";
import { fetchInterviewHistory } from "./interview";

function safeParseUserInfo() {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "null") || {};
  } catch {
    return {};
  }
}

export async function fetchPersonalCenterData() {
  const [profile, history] = await Promise.all([
    me().catch(() => null),
    fetchInterviewHistory().catch(() => []),
  ]);

  const localUser = safeParseUserInfo();
  const completedList = Array.isArray(history) ? history : [];
  const voiceCount = completedList.filter(
    (item) => item.mode === "voice",
  ).length;
  const textCount = completedList.filter((item) => item.mode === "text").length;
  const latestInterview = completedList[0] || null;

  return {
    profile: {
      id: profile?.id ?? localUser.id ?? "",
      username: profile?.username ?? localUser.username ?? "未命名用户",
      phone: profile?.phone ?? localUser.phone ?? "",
      email: profile?.email ?? localUser.email ?? "",
      avatar: profile?.avatar ?? localUser.avatar ?? "",
      createTime: profile?.createTime ?? localUser.createTime ?? "",
    },
    stats: {
      totalCount: completedList.length,
      voiceCount,
      textCount,
      latestInterviewTime: latestInterview?.endTime || "",
    },
    recentHistory: completedList.slice(0, 3),
  };
}

export async function updateProfile(profileData) {
  const response = await fetch("/api/v1/users/me", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
    body: JSON.stringify(profileData),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "更新个人信息失败");
  }

  const result = await response.json();
  if (result.code === 200 && result.data) {
    // 更新本地存储
    const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
    const updated = {
      ...userInfo,
      ...result.data,
    };
    localStorage.setItem("userInfo", JSON.stringify(updated));
    return result.data;
  }
  throw new Error(result.message || "更新个人信息失败");
}

export function logoutAccount() {
  localStorage.removeItem("token");
  localStorage.removeItem("userInfo");
}
