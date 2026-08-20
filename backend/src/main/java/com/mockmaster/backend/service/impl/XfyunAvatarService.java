package com.mockmaster.backend.service.impl;

import com.mockmaster.backend.util.AuthUtil;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class XfyunAvatarService {
    @Value("${xfyun.avatar.app-id}")
    private String appId;
    @Value("${xfyun.avatar.api-key}")
    private String apiKey;
    @Value("${xfyun.avatar.api-secret}")
    private String apiSecret;
    @Value("${xfyun.avatar.interface-service-id}")
    private String sceneId;
    @Value("${xfyun.avatar.avatar-id}")
    private String avatarId;
    @Value("${xfyun.avatar.vcn}")
    private String vcn;
    @Value("${xfyun.avatar.ws-url}")
    private String wsUrl;

    private WebSocketClient webSocketClient;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private CountDownLatch connectLatch;
    private Timer heartBeatTimer;

    @Getter
    private String sid;
    @Getter
    private String userSign; 
    @Getter
    private String server;
    @Getter
    private String roomId;
    @Getter
    private final String userId = UUID.randomUUID().toString();

    public void startAvatar() throws Exception {
        if (isConnected.get()) {
            log.info("✅ 虚拟人WebSocket已连接，无需重复启动");
            return;
        }
        connectLatch = new CountDownLatch(1);

        String authUrl = AuthUtil.assembleRequestUrl(wsUrl, apiKey, apiSecret);
        log.info("🔗 讯飞鉴权URL：{}", authUrl);

        webSocketClient = new WebSocketClient(new URI(authUrl)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("✅ 讯飞虚拟人WebSocket连接成功");
                isConnected.set(true);
                connectLatch.countDown();
                sendStartCommand();
                startHeartBeat();
            }

            @Override
            public void onMessage(String message) {
                log.info("📥 收到讯飞消息：{}", message);
                parseStreamInfo(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.error("❌ WebSocket关闭，code：{}，reason：{}", code, reason);
                resetState();
            }

            @Override
            public void onError(Exception ex) {
                log.error("❌ WebSocket错误", ex);
                resetState();
                connectLatch.countDown();
            }
        };

        webSocketClient.connect();
        boolean isSuccess = connectLatch.await(12, TimeUnit.SECONDS);

        if (!isSuccess || !isConnected.get()) {
            throw new RuntimeException("虚拟人WebSocket连接失败，请检查配置/网络/鉴权参数");
        }
    }
    private void sendStartCommand() {
        try {
            JSONObject header = new JSONObject()
                    .put("app_id", appId)
                    .put("ctrl", "start")
                    .put("request_id", UUID.randomUUID().toString())
                    .put("scene_id", sceneId);
            JSONObject stream = new JSONObject()
                    .put("protocol", "xrtc")
                    .put("fps", 25)
                    .put("bitrate", 5000)
                    .put("alpha", 0);

            JSONObject avatar = new JSONObject()
                    .put("avatar_id", avatarId)
                    .put("width", 720)
                    .put("height", 1280)
                    .put("stream", stream);

            JSONObject tts = new JSONObject()
                    .put("speed", 50)
                    .put("vcn", vcn);

            JSONObject parameter = new JSONObject()
                    .put("avatar", avatar)
                    .put("tts", tts);

            JSONObject startReq = new JSONObject()
                    .put("header", header)
                    .put("parameter", parameter);

            log.info("📤 发送启动指令：{}", startReq);
            webSocketClient.send(startReq.toString());
        } catch (Exception e) {
            log.error("发送启动指令失败", e);
        }
    }

    private void parseStreamInfo(String message) {
        try {
            JSONObject resp = new JSONObject(message);
            int code = resp.getJSONObject("header").optInt("code", -1);
            if (code != 0) {
                log.error("❌ 讯飞接口返回错误：code={}, msg={}", code, resp.getJSONObject("header").optString("message"));
                return;
            }

            JSONObject payload = resp.optJSONObject("payload");
            if (payload == null) return;
            JSONObject avatar = payload.optJSONObject("avatar");
            if (avatar == null) return;

            String eventType = avatar.optString("event_type", "");
            if (!"stream_info".equals(eventType)) {
                log.info("📥 忽略非流信息事件：{}", eventType);
                return;
            }

            this.sid = resp.getJSONObject("header").optString("sid");
            JSONObject streamExtend = avatar.optJSONObject("stream_extend");
            if (streamExtend != null) {
                this.userSign = streamExtend.optString("user_sign");
            }
            String streamUrl = avatar.optString("stream_url");

            if (streamUrl != null && (streamUrl.startsWith("xrtc://") || streamUrl.startsWith("xrtcs://"))) {
                String[] split = streamUrl.replace("xrtc://", "").replace("xrtcs://", "").split("/", 2);
                if (split.length >= 2) {
                    this.server = "https://" + split[0];
                    this.roomId = split[1];
                }
            }

            log.info("✅ 流信息解析成功：sid={}, server={}, roomId={}", sid, server, roomId);
        } catch (Exception e) {
            log.error("解析流信息失败", e);
        }
    }

    public void sendTextDriver(String text) {
        if (!isConnected.get() || webSocketClient == null) {
            throw new RuntimeException("虚拟人未连接，无法发送文本");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("发送文本不能为空");
        }

        try {
            JSONObject header = new JSONObject()
                    .put("app_id", appId)
                    .put("ctrl", "text_driver")
                    .put("request_id", UUID.randomUUID().toString());

            JSONObject textObj = new JSONObject().put("content", text.trim());
            JSONObject payload = new JSONObject().put("text", textObj);

            JSONObject textReq = new JSONObject()
                    .put("header", header)
                    .put("payload", payload);

            log.info("📤 发送文本指令：{}，内容：{}", textReq, text);
            webSocketClient.send(textReq.toString());
        } catch (Exception e) {
            log.error("发送文本指令失败", e);
            throw new RuntimeException("文本发送失败：" + e.getMessage());
        }
    }

    private void startHeartBeat() {
        if (heartBeatTimer != null) {
            heartBeatTimer.cancel();
        }

        heartBeatTimer = new Timer();
        heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected.get() && webSocketClient != null) {
                    JSONObject pingReq = new JSONObject()
                            .put("header", new JSONObject()
                                    .put("app_id", appId)
                                    .put("ctrl", "ping")
                                    .put("request_id", UUID.randomUUID().toString()));
                    webSocketClient.send(pingReq.toString());
                }
            }
        }, 0, 5000);
    }

    private void resetState() {
        isConnected.set(false);
        this.sid = null;
        this.userSign = null;
        this.server = null;
        this.roomId = null;

        if (heartBeatTimer != null) {
            heartBeatTimer.cancel();
            heartBeatTimer = null;
        }
    }

    public void stopAvatar() {
        log.info("🔌 【页面关闭】强制停止虚拟人WebSocket连接");
        resetState();
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }
        webSocketClient = null;
    }

    @PreDestroy
    public void close() {
        log.info("🔌 关闭虚拟人资源");
        stopAvatar();
    }

    public boolean isConnected() {
        return isConnected.get();
    }
}