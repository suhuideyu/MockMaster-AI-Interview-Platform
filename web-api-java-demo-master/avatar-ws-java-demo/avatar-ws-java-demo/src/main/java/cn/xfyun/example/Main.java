package cn.xfyun.example;

import cn.xfyun.example.util.AuthUtil;
import cn.xfyun.example.util.AvatarWsUtil;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;


public class Main extends AvatarWsUtil {
    private static final String avatarUrl = "wss://avatar.cn-huadong-1.xf-yun.com/v1/interact";
    private static final String apiKey = "123";
    private static final String apiSecret = "ZDg3NjBjMTRhMGE4ZjI3YjZkMDVlZjNm";
    private static final String appId = "648bce89";
    private static final String avatarId = "110117005";
    public static final String TTE = "UTF8";
    public static final String VCN = "x4_lingxiaoying_assist";
    public static final String TEXT = "欢迎来到讯飞开放平台";


    public static void main(String[] args) throws Exception {
        String requestUrl = AuthUtil.assembleRequestUrl(avatarUrl, apiKey, apiSecret);
        System.out.println("requestUrl:"+requestUrl);
        long l = System.currentTimeMillis();
        System.out.println("时间戳："+l);
        AvatarWsUtil avatarWsUtil = new AvatarWsUtil(requestUrl);

        System.out.println("开始发送start协议");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            avatarWsUtil.start(buildStartRequest(), countDownLatch);
        }catch (Exception e){
            e.printStackTrace();
        }
        countDownLatch.await();

        Timer timer = new Timer();
        CompletableFuture.runAsync(()->{
            TimerTask timeoutTask = new TimerTask() {
                @Override
                public void run() {
                    avatarWsUtil.send(buildPingRequest());
                }
            };
            timer.scheduleAtFixedRate(timeoutTask, 0,5000);
        });

        Thread.sleep(15000);
        avatarWsUtil.send(buildTextRequest("这是一个文本驱动，文本驱动不会进行理解"));
        Thread.sleep(10000);
        avatarWsUtil.send(buildTextinteractRequest("请说一段大会主持开场词"));
        Thread.sleep(50000);

        File audio = new File("src/main/java/cn/xfyun/example/util/Test.pcm");
        try(InputStream inputStream = new FileInputStream(audio)) {
            byte[] bytes = new byte[1024*10];
            int len = 0;
            int status = 0;
            String requestId = UUID.randomUUID().toString();
            while ((len = inputStream.read(bytes)) != -1) {
                System.out.println("status="+status);
                if(len == -1){
                    status = 2;
                }
                Thread.sleep(50);
                String audioData = Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, 0, len));
                avatarWsUtil.send(buildAudioRequest(requestId, status, audioData));
                Arrays.fill(bytes, (byte) 0);
                status = 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static JSONObject buildStartRequest() {
        JSONObject header = new JSONObject()
                .fluentPut("app_id", appId)
                .fluentPut("ctrl", "start")
                .fluentPut("request_id", UUID.randomUUID().toString())
                .fluentPut("scene_id","77213753883627520");
        JSONObject parameter = new JSONObject()
                .fluentPut("avatar", new JSONObject()
                        .fluentPut("avatar_id", avatarId)
                        .fluentPut("width",720)
                        .fluentPut("height",1280)
                        .fluentPut("stream", new JSONObject()
                                .fluentPut("protocol", "xrtc")
                                .fluentPut("fps",25)
                                .fluentPut("bitrate",5000)
                                .fluentPut("alpha",0)))
                .fluentPut("tts",new JSONObject()
                        .fluentPut("speed",50)
                        .fluentPut("vcn",VCN))
                .fluentPut("subtitle",new JSONObject()
                        .fluentPut("subtitle",0)
                        .fluentPut("font_color","#FF0000")
                        .fluentPut("font_size",10)
                        .fluentPut("position_x",0)
                        .fluentPut("position_y",0)
                        .fluentPut("font_name","mainTitle")
                        .fluentPut("width",100)
                        .fluentPut("height",100));

        JSONObject payload = new JSONObject()
                .fluentPut("background", new JSONObject()
                        .fluentPut("data", "22SLM2teIw+aqR6Xsm2JbH6Ng310kDam2NiCY/RQ9n6dw47gMO+7gGUJfWWfkqD39k/jtk/Fvh2qUdAMg95LKXJdf+GT2P87IVSiPrS4CQe/" +
                                "8M0oZzXUOpsQDliaYLHx6CR8se7TmCPOIdKE/isWXd5V7yz7RVQZt9tyHB564SChy6qUAOD2Akp2kSXtbEBT3uWRW2Xo+srd7tCGKD+aahzcQGVP6WZJ7X9" +
                                "piPt1BXRR39jVpxn9Dtxxvnsd/BBwZpJ/q5B1pOKms80DFg6vCBJHXqZ48LLVBbFoapH74cAHNg8qrXWoLfLFUejMiIwNEBJ4JJ4nuBiDExkuOUlLN19jw8" +
                                "abGUJarzfK26OSpfY="));
        return new JSONObject().fluentPut("header",header).fluentPut("parameter",parameter).fluentPut("payload",payload);
    }

    private static JSONObject buildTextRequest(String text) {
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","text_driver")
                .fluentPut("request_id", UUID.randomUUID().toString());

        JSONObject parameter = new JSONObject()
                .fluentPut("avatar_dispatch",new JSONObject()
                        .fluentPut("interactive_mode",0))
                .fluentPut("tts",new JSONObject()
                        .fluentPut("vcn",VCN)
                        .fluentPut("speed",50)
                        .fluentPut("pitch",50)
                        .fluentPut("volume",50))
                .fluentPut("air",new JSONObject()
                        .fluentPut("air",1)
                        .fluentPut("add_nonsemantic",1));

        JSONObject payload = new JSONObject()
                .fluentPut("text",new JSONObject()
                        .fluentPut("content",text));
        return new JSONObject().fluentPut("header",header).fluentPut("parameter",parameter).fluentPut("payload",payload);
    }

    private static JSONObject buildPingRequest() {
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","ping")
                .fluentPut("request_id", UUID.randomUUID().toString());
        return new JSONObject().fluentPut("header",header);
    }
    private static JSONObject buildTextinteractRequest(String text){
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","text_interact")
                .fluentPut("request_id",UUID.randomUUID().toString());

        JSONObject parameter = new JSONObject()
                .fluentPut("tts",new JSONObject()
                        .fluentPut("vcn",VCN)
                        .fluentPut("speed",50)
                        .fluentPut("pitch",50)
                        .fluentPut("audio",new JSONObject()
                                .fluentPut("sample_rate",16000)))
                .fluentPut("air",new JSONObject()
                        .fluentPut("air",1)
                        .fluentPut("add_nonsemantic",1));

        JSONObject payload = new JSONObject()
                .fluentPut("text",new JSONObject()
                        .fluentPut("content",text));
        return new JSONObject().fluentPut("header",header).fluentPut("parameter",parameter).fluentPut("payload",payload);
    }

    private static JSONObject buildAudioRequest(String requestid,int status,String content ) throws IOException {
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","audio_driver")
                .fluentPut("request_id",requestid);
        JSONObject parameter = new JSONObject()
                .fluentPut("avatar_dispatch",new JSONObject()
                        .fluentPut("audio_mode",0));
        JSONObject payload = new JSONObject()
                .fluentPut("audio",new JSONObject()
                        .fluentPut("status",status)
                        .fluentPut("audio",content));

        return new JSONObject().fluentPut("header",header).fluentPut("parameter",parameter).fluentPut("payload",payload);
    }

    private static JSONObject buildAudioInteractRequest(int status,String str) throws IOException {
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","audio_interact")
                .fluentPut("request_id",UUID.randomUUID().toString());
        JSONObject parameter = new JSONObject()
                .fluentPut("avatar_dispatch",new JSONObject()
                        .fluentPut("full_duplex",0));
        JSONObject payload = new JSONObject()
                .fluentPut("audio",new JSONObject()
                        .fluentPut("encoding","raw")
                        .fluentPut("sample_rate",16000)
                        .fluentPut("channels",1)
                        .fluentPut("bit_depth",16)
                        .fluentPut("status",status)
                        .fluentPut("seq",1)
                        .fluentPut("audio",str)
                        .fluentPut("frame_size",0)
                );
        return new JSONObject().fluentPut("header",header).fluentPut("parameter",parameter).fluentPut("payload",payload);
    }

    private static JSONObject buildCmdRequest(String dongzuo){
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","cmd")
                .fluentPut("request_id",UUID.randomUUID().toString());
        JSONObject payload = new JSONObject()
                .fluentPut("cmd_text",new JSONObject()
                        .fluentPut("avatar",new JSONObject()
                                .fluentPut("type","action")
                                .fluentPut("value","A_RLH_puzzle_0")));
        return new JSONObject().fluentPut("header",header).fluentPut("payload",payload);
    }

    private static JSONObject buildResetRequest(){
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","reset")
                .fluentPut("request_id",UUID.randomUUID().toString());
        return new JSONObject().fluentPut("header",header);
    }

    private static JSONObject buildStopRequest(){
        JSONObject header = new JSONObject()
                .fluentPut("app_id",appId)
                .fluentPut("ctrl","stop")
                .fluentPut("request_id",UUID.randomUUID().toString());
        return new JSONObject().fluentPut("header",header);
    }
}
