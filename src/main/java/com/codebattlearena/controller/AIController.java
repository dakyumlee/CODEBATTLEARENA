package com.codebattlearena.controller;

import com.codebattlearena.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithAI(@RequestBody ChatRequest request) {
        try {
            String response = openAIService.generateResponse(request.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("AI 채팅 오류: " + e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("message", "죄송합니다. 현재 AI 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요.");
            errorResult.put("timestamp", System.currentTimeMillis());
            errorResult.put("error", true);
            
            return ResponseEntity.ok(errorResult);
        }
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
