package com.codebattlearena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/announce")
    public void sendAnnouncement(Map<String, Object> announcement) {
        announcement.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/notifications", announcement);
    }

    @MessageMapping("/share-material")
    public void shareMaterial(Map<String, Object> material) {
        material.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/notifications", material);
    }

    @MessageMapping("/create-problem")
    public void createProblem(Map<String, Object> problem) {
        problem.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/notifications", problem);
    }

    @MessageMapping("/start-quiz")
    public void startQuiz(Map<String, Object> quiz) {
        quiz.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/notifications", quiz);
    }
}
