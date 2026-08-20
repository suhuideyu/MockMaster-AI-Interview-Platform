package com.mockmaster.backend.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;

public class IflytekAuthUtil {

    public static Map<String, String> generateAuthHeaders(
            String requestUrl,
            String apiKey,
            String apiSecret,
            String method,
            byte[] body) {
        
        try {
            if (requestUrl == null || requestUrl.isBlank()) {
                throw new IllegalArgumentException("讯飞 API URL 未配置");
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("讯飞 API Key 未配置");
            }
            if (apiSecret == null || apiSecret.isBlank()) {
                throw new IllegalArgumentException("讯飞 API Secret 未配置");
            }
            
            URL url = new URL(requestUrl);
            
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = format.format(new Date());

            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(body);
            String digest = "SHA256=" + Base64.getEncoder().encodeToString(instance.digest());
            
            String host = url.getHost();
            int port = url.getPort();
            if (port > 0) {
                host = host + ":" + port;
            }
            
            String path = url.getPath();
            if ("".equals(path) || path == null) {
                path = "/";
            }
            
            StringBuilder builder = new StringBuilder()
                    .append("host: ").append(host).append("\n")
                    .append("date: ").append(date).append("\n")
                    .append(method).append(" ").append(path).append(" HTTP/1.1").append("\n")
                    .append("digest: ").append(digest);
            
            Charset charset = Charset.forName("UTF-8");
            
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            
            String authorization = String.format(
                    "hmac-auth api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line digest", sha);

            Map<String, String> headers = new HashMap<>();
            headers.put("authorization", authorization);
            headers.put("host", host);
            headers.put("date", date);
            headers.put("digest", digest);
            headers.put("Content-Type", "application/json");
            
            return headers;
        } catch (Exception e) {
            throw new RuntimeException("生成讯飞认证头失败: " + e.getMessage(), e);
        }
    }

    public static String assembleWebSocketUrl(String wsUrl, String apiKey, String apiSecret) {
        try {
            String httpUrl = wsUrl.replace("wss://", "https://").replace("ws://", "http://");
            URL url = new URL(httpUrl);

            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = format.format(new Date());
            
            String host = url.getHost();
            
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n")
                    .append("date: ").append(date).append("\n")
                    .append("GET ").append(url.getPath()).append(" HTTP/1.1");
            
            Charset charset = Charset.forName("UTF-8");
            
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);

            String authorization = String.format(
                    "hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey, "hmac-sha256", "host date request-line", sha);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            
            return String.format("%s?authorization=%s&host=%s&date=%s",
                    wsUrl,
                    java.net.URLEncoder.encode(authBase, "UTF-8"),
                    java.net.URLEncoder.encode(host, "UTF-8"),
                    java.net.URLEncoder.encode(date, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("assembleWebSocketUrl 错误: " + e.getMessage(), e);
        }
    }
}