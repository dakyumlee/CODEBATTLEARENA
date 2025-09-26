package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class NotificationController {

    @PostMapping("/notification")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "공지사항이 전송되었습니다.");
            response.put("sentTo", "전체 학생");
            response.put("content", request.getMessage());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "공지사항 전송에 실패했습니다."));
        }
    }

    public static class NotificationRequest {
        private String message;
        private String target;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }
}
