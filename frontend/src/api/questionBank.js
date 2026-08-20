import { fetchJobs } from "./job";

export async function fetchQuestionBankData() {
  const jobs = await fetchJobs().catch(() => []);
  return {
    jobs: Array.isArray(jobs) ? jobs : [],
    questions: [],
  };
}

export async function getQuestions(jobId, difficulty) {
  const params = new URLSearchParams();
  if (jobId) {
    params.append("jobId", jobId);
  }
  if (difficulty) {
    params.append("difficulty", difficulty);
  }

  const response = await fetch(
    `/api/v1/resources/questions?${params.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    },
  );

  if (!response.ok) {
    throw new Error("获取题库失败");
  }

  const result = await response.json();
  if (result.code === 200 && Array.isArray(result.data)) {
    return result.data;
  }
  throw new Error(result.message || "获取题库失败");
}
