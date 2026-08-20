import { createRouter, createWebHistory } from "vue-router";
import Login from "../views/Login.vue";
import Home from "../views/Home.vue";
import InterviewSetting from "../views/InterviewSetting.vue";
import AiInterview from "../views/AiInterview.vue";
import InterviewHistory from "../views/InterviewHistory.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: Login },
  { path: "/home", name: "Home", component: Home },
  { path: "/setting", name: "InterviewSetting", component: InterviewSetting },
  {
    path: "/interview-select",
    name: "AiInterviewSelect",
    component: () => import("../views/AiInterviewSelect.vue"),
  },
  { path: "/interview", name: "AiInterview", component: AiInterview },
  {
    path: "/interview-history",
    name: "InterviewHistory",
    component: InterviewHistory,
  },
  {
    path: "/personal-center",
    name: "PersonalCenter",
    component: () => import("../views/PersonalCenter.vue"),
  },
  {
    path: "/simulator",
    name: "Simulator",
    component: () => import("../views/Simulator.vue"),
  },
  {
    path: "/score",
    name: "Score",
    component: () => import("../views/Score.vue"),
  },
  {
    path: "/question-bank",
    name: "QuestionBank",
    component: () => import("../views/QuestionBank.vue"),
  },
  {
    path: "/growth-curve",
    name: "GrowthCurve",
    component: () => import("../views/GrowthCurve.vue"),
  },
  {
    path: "/spark-chat",
    name: "SparkChat",
    component: () => import("../views/SparkChat.vue"),
    meta: { title: "讯飞星火聊天" },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  if (to.name !== "Login" && !token) {
    next({ name: "Login" });
    return;
  }
  next();
});

export default router;
