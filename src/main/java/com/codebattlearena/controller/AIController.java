package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithAI(@RequestBody ChatRequest request) {
        try {
            // 간단한 AI 응답 시뮬레이션 (실제로는 Claude/GPT API 호출)
            String response = generateAIResponse(request.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI 응답 생성에 실패했습니다."));
        }
    }

    private String generateAIResponse(String userMessage) {
        // 간단한 키워드 기반 응답
        String message = userMessage.toLowerCase();
        
        if (message.contains("자바") || message.contains("java")) {
            return "자바는 객체지향 프로그래밍 언어입니다. 어떤 자바 개념에 대해 궁금하신가요?";
        } else if (message.contains("배열") || message.contains("array")) {
            return "배열은 같은 타입의 데이터를 연속적으로 저장하는 자료구조입니다. int[] arr = new int[5]; 이런 식으로 선언할 수 있어요.";
        } else if (message.contains("반복문") || message.contains("for") || message.contains("while")) {
            return "반복문에는 for문, while문, do-while문이 있습니다. 각각의 사용 상황이 다른데, 구체적으로 어떤 것이 궁금하신가요?";
        } else if (message.contains("알고리즘")) {
            return "알고리즘은 문제를 해결하기 위한 단계적 절차입니다. 어떤 알고리즘에 대해 알고 싶으신가요? (정렬, 탐색, 동적계획법 등)";
        } else {
            return "좋은 질문이네요! 구체적으로 어떤 부분이 궁금하신지 자세히 말씀해주시면 더 정확한 답변을 드릴 수 있습니다.";
        }
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
