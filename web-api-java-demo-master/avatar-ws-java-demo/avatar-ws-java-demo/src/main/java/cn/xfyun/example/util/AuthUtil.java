package cn.xfyun.example.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import okhttp3.RequestBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuthUtil {

    public static String assembleRequestUrl(String requestUrl, String apiKey, String apiSecret) {
        return assembleRequestUrl(requestUrl, apiKey, apiSecret, "GET");
    }

    public static String assembleRequestUrl(String requestUrl, String apiKey, String apiSecret, String method) {
        URL url = null;
        String httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://", "https://");
        try {
            url = new URL(httpRequestUrl);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = format.format(new Date());
            String host = url.getHost();
            System.out.println("host:"+host);
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append(method).append(" ").append(url.getPath()).append(" HTTP/1.1");

            System.out.println(builder.toString());
            System.out.println("--------------");
            Charset charset = Charset.forName("UTF-8");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            String authorization = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            System.out.println("signature:"+sha);
            System.out.println("----------------------------");
            System.out.println("authorization:"+authorization);
            System.out.println("--------------------");
            System.out.println("authBase:"+authBase);
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
        }
    }

    /**
     * 计算签名所需要的header参数
     * @param requestUrl like 'http://rest-api.xfyun.cn/v2/iat'
     * @param apiKey
     * @param apiSecret
     * @method request method  POST/GET/PATCH/DELETE etc....
     * @param body   http request body
     * @return header map ，contains all headers should be set when access api
     */
    public static Map<String ,String> assembleRequestHeader(String requestUrl, String apiKey, String apiSecret, String method, byte[] body){
        URL url = null;
        try {
            url = new URL(requestUrl);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = format.format(new Date());
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(body);
            String  digest = "SHA256="+ Base64.getEncoder().encodeToString(instance.digest());
            String host = url.getHost();
            int port = url.getPort();
            if (port > 0){
                host = host +":"+port;
            }
            String  path = url.getPath();
            if ("".equals(path) || path == null){
                path = "/";
            }
            StringBuilder builder = new StringBuilder().
                    append("host: ").append(host).append("\n").//
                            append("date: ").append(date).append("\n").//
                            append(method).append(" ").append(path).append(" HTTP/1.1").append("\n").
                    append("digest: ").append(digest);
            System.out.println("builder:"+builder);
            Charset charset = Charset.forName("UTF-8");

            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            String authorization = String.format("hmac-auth api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line digest", sha);
            Map<String,String > header = new HashMap<String ,String>();
            System.out.println();

            header.put("authorization",authorization);
            header.put("host",host);
            header.put("date",date);
            header.put("digest",digest);
            System.out.println("header " + header.toString());
            return header;
        } catch (Exception e) {
            throw new RuntimeException("assemble requestHeader  error:"+e.getMessage());
        }
    }
}