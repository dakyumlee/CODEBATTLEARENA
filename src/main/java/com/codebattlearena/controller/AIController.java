package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }

            String userMessage = request.getMessage();
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Map.of("error", "메시지가 비어있습니다.");
            }

            // AI API가 설정되지 않은 경우 기본 응답 제공
            String aiResponse = generateMockResponse(userMessage);
            
            return Map.of("response", aiResponse);
            
        } catch (Exception e) {
            return Map.of("error", "AI 서비스 오류: " + e.getMessage());
        }
    }

    private String generateMockResponse(String userMessage) {
        // 간단한 키워드 기반 응답
        String message = userMessage.toLowerCase();
        
        if (message.contains("안녕") || message.contains("hello") || message.contains("hi")) {
            return "안녕하세요! 코딩 학습에 관해 궁금한 것이 있으시면 언제든 물어보세요.";
        }
        
        if (message.contains("자바") || message.contains("java")) {
            return "자바는 객체지향 프로그래밍 언어입니다. 클래스와 객체 개념을 이해하는 것이 중요해요. 어떤 부분이 궁금하신가요?";
        }
        
        if (message.contains("파이썬") || message.contains("python")) {
            return "파이썬은 문법이 간단하고 배우기 쉬운 언어입니다. 데이터 분석, 웹 개발, AI 등 다양한 분야에서 사용됩니다.";
        }
        
        if (message.contains("알고리즘")) {
            return "알고리즘은 문제를 해결하는 단계별 방법입니다. 정렬, 탐색, 동적 프로그래밍 등 다양한 알고리즘이 있어요. 어떤 알고리즘에 대해 알고 싶으신가요?";
        }
        
        if (message.contains("반복문") || message.contains("for") || message.contains("while")) {
            return "반복문은 같은 작업을 여러 번 수행할 때 사용합니다. for문은 횟수가 정해진 경우, while문은 조건에 따라 반복할 때 사용해요.";
        }
        
        if (message.contains("배열") || message.contains("array")) {
            return "배열은 같은 타입의 데이터를 연속적으로 저장하는 자료구조입니다. 인덱스를 통해 각 요소에 접근할 수 있어요.";
        }
        
        if (message.contains("함수") || message.contains("메서드") || message.contains("method")) {
            return "함수는 특정 작업을 수행하는 코드 블록입니다. 재사용성을 높이고 코드를 구조화하는 데 도움이 됩니다.";
        }
        
        if (message.contains("데이터베이스") || message.contains("db") || message.contains("sql")) {
            return "데이터베이스는 데이터를 체계적으로 저장하고 관리하는 시스템입니다. SQL을 사용해서 데이터를 조회하고 조작할 수 있어요.";
        }
        
        if (message.contains("오류") || message.contains("에러") || message.contains("error")) {
            return "오류가 발생했다면 에러 메시지를 자세히 읽어보세요. 문법 오류, 런타임 오류, 논리 오류로 나눌 수 있습니다. 구체적인 오류 내용을 알려주시면 더 자세히 도와드릴게요.";
        }
        
        if (message.contains("공부") || message.contains("학습") || message.contains("배우")) {
            return "프로그래밍 학습은 꾸준함이 가장 중요합니다. 이론 학습과 실습을 병행하고, 작은 프로젝트부터 시작해보세요. 어떤 분야를 집중적으로 공부하고 싶으신가요?";
        }
        
        // 기본 응답
        return "흥미로운 질문이네요! 프로그래밍과 관련된 구체적인 질문을 해주시면 더 자세한 답변을 드릴 수 있습니다. 예를 들어 자바, 파이썬, 알고리즘, 데이터구조 등에 대해 물어보세요.";
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
