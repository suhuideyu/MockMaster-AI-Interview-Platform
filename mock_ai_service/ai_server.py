import os
import asyncio

import uuid
import wave
import logging
import numpy as np
import shutil

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CACHE_DIR = os.path.join(BASE_DIR, "models_cache")
os.makedirs(CACHE_DIR, exist_ok=True)

from fastapi import FastAPI, Form, UploadFile, File, BackgroundTasks
import whisper
from sentence_transformers import SentenceTransformer, util
import jieba.analyse
import uvicorn

app = FastAPI(title="MockMaster 语音评分服务")

logger.info("Loading Whisper 语音识别模型...")
asr_model = whisper.load_model("small", download_root=CACHE_DIR)

logger.info("Loading 语义模型（从本地缓存加载）...")
model_path = os.path.join(
    CACHE_DIR,
    "models--sentence-transformers--paraphrase-multilingual-MiniLM-L12-v2",
    "snapshots",
    "e8f8c211226b894fcb81acc59f3b34ba3efd5f42"
)
nlp_model = SentenceTransformer(model_path)

logger.info("( ‵▽′)ψ所有模型加载完成！")

TEMP_DIR = os.path.join(BASE_DIR, "temp_audio")
os.makedirs(TEMP_DIR, exist_ok=True)

INTERVIEW_PROMPT = "这是一场前端面试，包含Vue、React、闭包、原型链、跨域、Flex布局、CSS优先级、响应式原理等技术术语。"

def get_wav_duration(file_path):
    try:
        with wave.open(file_path, 'rb') as wav_file:
            sample_rate = wav_file.getframerate()
            n_frames = wav_file.getnframes()
            return n_frames / sample_rate
    except Exception as e:
        logger.error(f"时长获取失败: {e}")
        return 1.0

def load_wav_to_numpy(file_path):
    with wave.open(file_path, 'rb') as wf:
        n_channels = wf.getnchannels()
        sample_width = wf.getsampwidth()
        audio_data = wf.readframes(wf.getnframes())

    if sample_width == 2:
        audio_np = np.frombuffer(audio_data, dtype=np.int16)
    else:
        raise ValueError(f"不支持的位深: {sample_width}")

    if n_channels > 1:
        audio_np = audio_np[::n_channels]

    return audio_np.astype(np.float32) / 32768.0

@app.post("/analyze")
async def analyze(
    background_tasks: BackgroundTasks,
    mode: str = Form(...),
    standard: str = Form(...),
    text: str = Form(None),
    file: UploadFile = File(None)
):
    user_content = text
    duration = 1.0
    temp_path = None

    try:
        if mode == "voice":
            if not file:
                return {"error": "请上传语音文件"}

            unique_filename = f"{uuid.uuid4()}.wav"
            temp_path = os.path.join(TEMP_DIR, unique_filename)
            
            with open(temp_path, "wb") as buffer:
                shutil.copyfileobj(file.file, buffer)

            duration = get_wav_duration(temp_path)
            audio_np = load_wav_to_numpy(temp_path)

            result = await asyncio.to_thread(
                asr_model.transcribe,
                audio_np,
                language="zh",
                fp16=False,
                initial_prompt=INTERVIEW_PROMPT,
                beam_size=5,
                temperature=0
            )
            user_content = result["text"].strip()
            logger.info(f"识别结果: {user_content}")

        emb_user = await asyncio.to_thread(nlp_model.encode, user_content)
        emb_standard = await asyncio.to_thread(nlp_model.encode, standard)
        score_accuracy = float(util.cos_sim(emb_user, emb_standard)) * 100

        keys = jieba.analyse.extract_tags(standard, topK=8)
        hits = [w for w in keys if w.lower() in user_content.lower()]
        score_professional = (len(hits) / len(keys)) * 100 if keys else 100

        if duration > 1:
            wpm = (len(user_content) / duration) * 60
            if 130 < wpm < 210:
                score_logic = 100
            else:
                score_logic = max(50, 100 - abs(wpm - 160) * 0.5)
        else:
            score_logic = 80

        total_score = score_accuracy * 0.5 + score_professional * 0.3 + score_logic * 0.2

        return {
            "user_text": user_content,
            "score_accuracy": round(score_accuracy, 2),
            "score_professional": round(score_professional, 2),
            "score_logic": round(score_logic, 2),
            "total_score": round(total_score, 2)
        }

    finally:
        if temp_path and os.path.exists(temp_path):
            background_tasks.add_task(os.remove, temp_path)

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)