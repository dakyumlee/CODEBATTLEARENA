package com.codebattlearena.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notification")
    @SendTo("/topic/notifications")
    public Map<String, Object> sendNotification(Map<String, Object> notification) {
        return notification;
    }

    @MessageMapping("/activity")
    @SendTo("/topic/activities")
    public Map<String, Object> sendActivity(Map<String, Object> activity) {
        return activity;
    }

    public void sendProblemNotification(String problemTitle, String problemType) {
        Map<String, Object> notification = Map.of(
            "type", "NEW_PROBLEM",
            "title", "새로운 " + problemType + " 출제됨",
            "message", problemTitle,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
