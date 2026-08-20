package cn.xfyun.example.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ProtocolException;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AvatarWsUtil {
    public static  WebSocket webSocket;

    private static final AtomicBoolean isConnected = new AtomicBoolean(false);

    private static CountDownLatch countDownLatch;
    private static   CountDownLatch connect;

    public JSONObject jsonObject;

    public static String vmr_status = "0";
    public AvatarWsUtil(){

    }

    public AvatarWsUtil(String requestUrl) {
        Request wsRequest = (new Request.Builder()).url(requestUrl).build();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        connect = new CountDownLatch(1);
        this.webSocket = okHttpClient.newWebSocket(wsRequest, buildWebSocketListener());
    }

    public void start(JSONObject request, CountDownLatch countDownLatch) throws Exception {
        this.countDownLatch = countDownLatch;
        System.out.println("connect之前");
        connect.await();
        System.out.println("connect之后");
        send(request);
        System.out.println("send发送了start请求");
    }

    public void start(JSONObject request) throws Exception {
        System.out.println("connect之前");
        connect.await();
        System.out.println("connect之后");
        send(request);
    }

    public void send(JSONObject request) {
        if (isConnected.get()) {
            String jsonStr = JSONUtil.toJsonStr(request);
            log.info("send :{}", jsonStr);
            webSocket.send(jsonStr);
        }
    }


    public static WebSocketListener buildWebSocketListener() {
        return new WebSocketListener() {
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                log.info("onOpen");
                System.out.println("触发onOpen事件，连接上了");
                isConnected.set(true);
                connect.countDown();
            }
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                log.info("onMessage: {}", text);
                JSONObject jsonObject = JSON.parseObject(text);
                int code = jsonObject.getJSONObject("header").getIntValue("code");
                if (code != 0) {
                    onEvent(webSocket, 1002, jsonObject.getJSONObject("header").getString("message"), "server closed");
                    System.exit(0);
                    return;
                }
                JSONObject payload = jsonObject.getJSONObject("payload");
                if (payload != null) {
                    JSONObject avatar = payload.getJSONObject("avatar");
                    if (avatar != null) {
                        if(avatar.getString("stream_url") != null) {
                            if (!Objects.equals("", avatar.getString("stream_url"))) {
                                System.out.println("获取到了推流地址，start成功");
                                countDownLatch.countDown();
                            }
                        }
                    }
                }
            }
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                this.onEvent(webSocket, code, reason, "onClosing");
            }
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                this.onEvent(webSocket, code, reason, "onClosed");
            }
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable tx, Response response) {
                Object t;
                try {
                    String responseBody = response.body().string();
                    JSONObject body = JSON.parseObject(responseBody);
                    t = new ProtocolException(body.toString());
                } catch (IOException var6) {
                    t = var6;
                }
                log.info("onFailure:{}", t);
            }
            void onEvent(@NotNull WebSocket webSocket, int code, String reason, String event) {
                log.info("session {} . code:{}, reason:{}", event, code, reason);
                isConnected.set(false);
                countDownLatch.countDown();
                try {
                    webSocket.close(code, reason);
                } catch (Exception var6) {
                    log.error("{} error.{}", event, var6.getMessage());
                }
            }
        };
    }
    public void close() {
        this.webSocket.close(1000, "");
    }
}