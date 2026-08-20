package com.mockmaster.backend.controller;

import com.mockmaster.backend.service.impl.XfyunAvatarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/avatar")
@CrossOrigin
@Slf4j
public class AvatarController {
    private final XfyunAvatarService avatarService;

    public AvatarController(XfyunAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/start")
    public Map<String, Object> startAvatar() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始启动虚拟人...");
            avatarService.startAvatar();
            result.put("code", 0);
            result.put("msg", "虚拟人启动成功");
        } catch (Exception e) {
            log.error("启动虚拟人失败", e);
            result.put("code", 500);
            result.put("msg", "启动失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/stream")
    public Map<String, Object> getStreamInfo() {
        Map<String, Object> result = new HashMap<>();
        boolean isConnected = avatarService.isConnected();
        log.info("查询流信息，连接状态：{}", isConnected);

        if (!isConnected) {
            result.put("code", 400);
            result.put("msg", "虚拟人未连接，请先启动");
            return result;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sid", avatarService.getSid());
        data.put("server", avatarService.getServer());
        data.put("auth", avatarService.getUserSign());
        data.put("appid", "2c46d962");
        data.put("userId", avatarService.getUserId());
        data.put("roomId", avatarService.getRoomId());
        data.put("timeStr", String.valueOf(System.currentTimeMillis()));

        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", data);
        return result;
    }

    @PostMapping("/send-text")
    public Map<String, Object> sendText(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String text = request.get("text");
            log.info("接收发送文本：{}", text);
            avatarService.sendTextDriver(text);
            result.put("code", 0);
            result.put("msg", "文本发送成功");
        } catch (Exception e) {
            log.error("发送文本失败", e);
            result.put("code", 500);
            result.put("msg", "发送失败：" + e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/stop")
    public Map<String, Object> stopAvatar() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("前端关闭页面，调用stop断开虚拟人");
            avatarService.stopAvatar();
            result.put("code", 0);
            result.put("msg", "已停止");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "停止失败");
        }
        return result;
    }
}