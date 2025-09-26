package com.codebattlearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;

@Service
public class OpenAIService {
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String generateProblem(String difficulty) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API 키가 설정되지 않았습니다.");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", "gpt-3.5-turbo");
        request.put("messages", Map.of(
            "role", "user",
            "content", "국비학원 학생을 위한 " + difficulty + " 난이도의 자바 코딩 문제를 생성해주세요."
        ));
        request.put("max_tokens", 500);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            // OpenAI API 호출
            Map<String, Object> response = restTemplate.postForObject(
                "https://api.openai.com/v1/chat/completions", 
                entity, 
                Map.class
            );
            
            // 응답에서 텍스트 추출
            return response.toString(); // 실제로는 JSON 파싱 필요
            
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage());
        }
    }
    
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("${OPENAI_API_KEY:}");
    }
}
