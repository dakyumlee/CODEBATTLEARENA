package com.codebattlearena.controller;

import com.codebattlearena.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("AI 채팅 요청: " + request.getMessage());
            
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("response", "메시지를 입력해주세요.");
                return response;
            }
            
            String aiResponse = openAIService.generateResponse(request.getMessage());
            System.out.println("AI 응답: " + aiResponse);
            
            response.put("success", true);
            response.put("response", aiResponse);
            
        } catch (Exception e) {
            System.err.println("AI 채팅 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("response", "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다.");
        }
        
        return response;
    }

    public static class ChatRequest {
        private String message;
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
