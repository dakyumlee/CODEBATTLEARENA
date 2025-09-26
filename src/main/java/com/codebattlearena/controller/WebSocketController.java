package com.codebattlearena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/student-activity")
    @SendTo("/topic/activity")
    public Map<String, Object> handleStudentActivity(Map<String, Object> activity) {
        activity.put("timestamp", LocalDateTime.now().toString());
        return activity;
    }

    @MessageMapping("/notification")
    public void handleNotification(Map<String, Object> notification) {
        // 모든 학생에게 알림 전송
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    @MessageMapping("/battle-update")
    @SendTo("/topic/battle")
    public Map<String, Object> handleBattleUpdate(Map<String, Object> battleData) {
        return battleData;
    }

    public void sendNotificationToAll(String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
