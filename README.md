# MockMaster · 智面AI

> 以练养技，以熟破局 —— 一款基于 AI 大模型与语音识别技术的**模拟面试练习平台**。

MockMaster（智面AI）通过 **讯飞虚拟人（RTC 实时数字人）** 充当「AI 面试官」，支持**语音面试**与**文本面试**两种模式，结合 **Whisper 语音识别 + 语义相似度模型**对回答进行多维自动评分，帮助求职者针对目标岗位反复练习、复盘与提升。

---

## ✨ 核心功能

- 🔐 **用户体系**：注册 / 登录 / 退出，JWT 无状态鉴权，个人资料编辑与头像上传。
- 🎯 **岗位选择**：内置前端、后端、产品、UI、测试、云计算、移动端、网络安全、数据分析、人工智能等 10 个岗位方向。
- ⚙️ **面试配置**：自由配置意向岗位、难度（简单 / 中等 / 困难）、时长与面试模式（语音 / 文本）。
- 🤖 **AI 模拟面试**：
  - 语音面试：讯飞虚拟人实时出题提问，考生录音作答；
  - 文本面试：文字问答交互；
  - 面试官支持逐轮追问，可暂停 / 提前退出。
- 🎙️ **智能评分**：Whisper 将语音转文字后，综合**语义准确度、专业关键词覆盖、表达流利度（语速）**三个维度加权打分。
- 📊 **成绩分析**：ECharts 雷达图展示各维度得分与总体表现。
- 📈 **成长曲线**：统计练习总场次、累计时长、平均分与分数趋势，见证进步。
- 📚 **题库系统**：按岗位、难度筛选真题，支持专项练习。
- 🕘 **历史记录**：查看已完成面试的文字稿、评分与音频。
- 💬 **讯飞星火聊天**：通过 WebSocket 直连讯飞星火大模型，随时答疑。

---

## 🏗️ 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3 · Vite 5 · Element Plus · Pinia · Vue Router · ECharts · Tailwind CSS · GSAP |
| 后端 | Java 17 · Spring Boot 3.3.5 · Spring Security · MyBatis-Plus 3.5.7 · MySQL · Hutool · Lombok · Java-WebSocket |
| AI 服务 | Python 3.12 · FastAPI · OpenAI Whisper · SentenceTransformers · jieba |
| 第三方 | 讯飞开放平台（虚拟人 RTC · 星火大模型） |

### 端口约定

| 服务 | 端口 |
| --- | --- |
| 前端（Vite） | `3000` |
| 后端（Spring Boot） | `8080` |
| AI 评分服务（FastAPI） | `8000` |

---

## 🗂️ 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                        浏览器（Vue 3）                        │
│   岗位选择 · 面试配置 · AI面试 · 评分 · 题库 · 成长曲线 ...    │
└──────────────┬──────────────────────────────┬───────────────┘
               │ HTTP /api (Vite 代理)        │ WebSocket
┌──────────────▼──────────────┐   ┌───────────▼──────────────────┐
│        Spring Boot 后端      │   │      讯飞虚拟人 RTC           │
│    JWT 鉴权 · 业务接口       │──▶│   （数字人 AI 面试官）        │
└──────────────┬──────────────┘   └──────────────────────────────┘
               │ HTTP POST /analyze (语音 + 文本)
┌──────────────▼──────────────┐
│      Python AI 评分服务      │
│  Whisper(ASR) → 语义相似度   │
│  → 关键词 → 流利度 → 总分    │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐   ┌───────────────┐
│           MySQL             │   │ 本地模型缓存    │
│  user/job/interview/message │   │ whisper small  │
│  resource/interview_detail  │   │ MiniLM-L12-v2  │
└─────────────────────────────┘   └───────────────┘
```

---

## 📁 项目结构

```
MockMaster/
├── frontend/                 # 前端（Vue 3 + Vite）
│   └── src/
│       ├── api/              # 接口封装（auth/job/interview/score/questionBank...）
│       ├── components/       # Sidebar / Chat / RtcVirtualPlayer / SparkChat
│       ├── views/            # 页面（Home/InterviewSetting/AiInterview/Score/题库/成长曲线...）
│       ├── router/           # 路由（含登录守卫）
│       ├── store/            # Pinia 状态
│       └── styles/           # 全局样式与字体
├── backend/                  # 后端（Spring Boot）
│   └── src/main/java/com/mockmaster/backend/
│       ├── controller/       # REST 接口
│       ├── service/          # 业务逻辑（含讯飞虚拟人 XfyunAvatarService）
│       ├── mapper/           # MyBatis-Plus Mapper
│       ├── entity/           # 实体类
│       ├── dto/              # 请求 / 响应对象
│       ├── security/         # JWT 鉴权
│       └── config/           # Security / CORS / WebMvc 配置
├── mock_ai_service/          # Python AI 服务（FastAPI）
│   └── ai_server.py          # 语音识别 + 语义评分接口（端口 8000）
├── database/                 # 数据库脚本与题库
│   ├── schema.sql            # 建库建表 + 岗位初始化数据
│   ├── questions.xlsx        # 题库 Excel
│   └── import_questions.py   # 题库导入脚本
├── models_cache/             # 本地模型缓存（whisper / MiniLM）
├── uploads/                  # 上传文件目录（头像、音频）
└── temp_audio/               # 语音评分临时文件
```

---

## ✅ 环境要求

- **JDK 17**（后端）
- **Node.js ≥ 18** 与 npm（前端）
- **MySQL 8.x**（数据库，本地 `localhost:3306`）
- **Python 3.12**（AI 服务，项目内置 `.venv` 虚拟环境）

已下载到本地的模型（存放于 `models_cache/`）：

- Whisper 语音识别模型 `small.pt`
- 语义相似度模型 `paraphrase-multilingual-MiniLM-L12-v2`

---

## 🚀 快速开始

### 第 0 步：初始化数据库

1. 执行建表脚本（会自动建库并插入岗位数据）：

   ```bash
   mysql -uroot -p < database/schema.sql
   ```

2. 检查两个数据库连接是否与本机一致（默认 `root / root`）：

   - `backend/src/main/resources/application.yml`
   - `database/import_questions.py`

3. 导入题库：

   ```bash
   cd database
   python import_questions.py
   ```

### 第 1 步：启动 AI 评分服务（端口 8000）

```bash
cd mock_ai_service
python ai_server.py
```

> 首次启动会加载 Whisper 与语义模型，需等待片刻。

### 第 2 步：启动后端（端口 8080）

使用 IDEA / Maven 运行主类 `MockMasterBackendApplication`，或在 `backend/` 下执行：

```bash
mvn spring-boot:run
```

### 第 3 步：启动前端（端口 3000）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 <http://localhost:3000> 即可。

---

## ⚙️ 配置说明

所有后端配置位于 [application.yml](backend/src/main/resources/application.yml)：

| 配置项 | 说明 |
| --- | --- |
| `spring.datasource` | MySQL 连接（默认 `root / root`，库名 `mockmaster`） |
| `mockmaster.jwt-secret` | JWT 签名密钥 |
| `mockmaster.jwt-expire-hours` | Token 有效期（默认 24h） |
| `mockmaster.upload-dir` | 上传文件存储目录 |
| `mockmaster.virtual-human` | 讯飞虚拟人会话语料配置 |
| `xfyun.avatar` | 讯飞交互平台 RTC 虚拟人参数（app-id / api-key / avatar-id 等） |

> ⚠️ 讯飞相关密钥需在[讯飞开放平台](https://www.xfyun.cn/)控制台申请并替换，请勿提交真实密钥。

前端开发代理位于 [vite.config.js](frontend/vite.config.js)：`/api` 前缀的请求会转发至 `http://localhost:8080`。

---

## 🔌 API 概览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | 注册 |
| POST | `/api/v1/auth/login` | 登录 |
| GET | `/api/v1/jobs` | 岗位列表 |
| GET | `/api/v1/resources/questions` | 题库（按岗位 / 难度筛选） |
| GET | `/api/v1/resources/{id}` | 题目详情 |
| POST | `/api/v1/interviews/start` | 开始面试 |
| POST | `/api/v1/interviews/{id}/messages` | 发送对话消息 |
| POST | `/api/v1/interviews/{id}/submitVoice` | 提交语音答案并评分 |
| POST | `/api/v1/interviews/{id}/submitText` | 提交文本答案并评分 |
| POST | `/api/v1/interviews/{id}/complete` | 完成面试 |
| POST | `/api/v1/interviews/{id}/abort` | 中止面试 |
| GET | `/api/v1/interviews/history` | 面试历史列表 |
| GET | `/api/v1/interviews/{id}` | 面试详情（含消息记录） |
| GET | `/api/v1/scores/overview` | 评分总览 |
| GET | `/api/v1/scores/growth-curve` | 成长曲线数据 |
| GET | `/api/v1/scores/interviews/{id}` | 单场面试详细评分 |
| GET / PUT | `/api/v1/users/me` | 个人信息查询 / 更新 |
| POST | `/api/v1/virtual-human/session` | 创建虚拟人会话 |
| POST | `/api/common/upload` | 文件上传（头像 / 音频） |
| — | `/api/avatar/**` | 讯飞 RTC 虚拟人流（公开） |

> 除 `/api/v1/auth/**`、`/api/avatar/**`、`/upload/**` 外，其余接口均需携带 `Authorization: Bearer <token>`。

AI 评分服务独立接口（端口 8000）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/analyze` | 语音 / 文本答案评分，返回 `user_text`、`score_accuracy`、`score_professional`、`score_logic`、`total_score` |

---

## 🧩 核心实现说明

- **AI 面试官**：后端通过讯飞交互平台创建虚拟人会话，前端使用 `RtcVirtualPlayer` 组件建立 RTC 实时流，虚拟人播报题目并监听考生语音。
- **语音评分流程**：考生录音 → 上传 `/submitVoice` → 后端转发 Python `/analyze` → Whisper 转写文字 → 与标准答案计算语义余弦相似度、jieba 关键词命中率、按语速评估流利度 → 按 `50% 语义 + 30% 专业 + 20% 流利` 加权得总分。
- **题库导入**：`database/questions.xlsx` 通过 `import_questions.py`（pandas + SQLAlchemy）批量写入 `resource` 表。

---

## 📝 运行步骤速查

```text
1. 初始化数据库：执行 schema.sql，并运行 database/import_questions.py 导入题库
2. 启动 AI 服务：  cd mock_ai_service && python ai_server.py     （:8000）
3. 启动后端：      mvn spring-boot:run                            （:8080）
4. 启动前端：      cd frontend && npm run dev                     （:3000）
```

---

## 📄 License

本项目为内部练习 / 学习项目，仅作个人参考，请勿用于商业用途。第三方接口（讯飞开放平台）的使用请遵循对应平台服务条款。
