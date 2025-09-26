package com.codebattlearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "AI 서비스가 설정되지 않았습니다. 관리자에게 문의하세요.";
        }

        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 1000);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 프로그래밍 학습을 도와주는 친절한 AI 튜터입니다. 한국어로 답변하고, 초보자도 이해하기 쉽게 설명해주세요.");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    List choices = (List) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = (Map) choices.get(0);
                        Map messageObj = (Map) choice.get("message");
                        return (String) messageObj.get("content");
                    }
                }
            }

            return "죄송합니다. AI 서비스에서 응답을 받을 수 없습니다.";

        } catch (Exception e) {
            System.err.println("OpenAI API 호출 오류: " + e.getMessage());
            return "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다.";
        }
    }

    public String generateProblem(String difficulty, String topic) {
        String prompt = String.format(
            "%s 난이도의 %s 관련 프로그래밍 문제를 만들어주세요. " +
            "문제 제목, 설명, 예시 입력/출력을 포함해서 작성해주세요.",
            difficulty, topic
        );
        return generateResponse(prompt);
    }
}
