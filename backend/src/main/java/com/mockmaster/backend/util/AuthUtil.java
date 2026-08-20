package com.mockmaster.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;
import java.util.TimeZone;

public class AuthUtil {
    public static String assembleRequestUrl(String wsUrl, String apiKey, String apiSecret) throws Exception {

        String urlNoProtocol = wsUrl.replace("ws://", "").replace("wss://", "");
        int pathStart = urlNoProtocol.indexOf("/");
        String host = urlNoProtocol.substring(0, pathStart);
        String path = urlNoProtocol.substring(pathStart);

        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(System.currentTimeMillis());

        String preStr = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "GET " + path + " HTTP/1.1";

        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] signatureBytes = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        String authorizationOrigin = String.format(
                "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey, signature
        );
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        return String.format("%s?authorization=%s&date=%s&host=%s",
                wsUrl,
                URLEncoder.encode(authorization, "UTF-8"),
                URLEncoder.encode(date, "UTF-8"),
                URLEncoder.encode(host, "UTF-8")
        );
    }
}