package com.codebattlearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class AiProblemService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateProblem(String difficulty, String topic) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackProblem(difficulty, topic);
        }

        try {
            String prompt = createPrompt(difficulty, topic);
            String response = callOpenAiApi(prompt);
            return parseAiResponse(response, difficulty);
        } catch (Exception e) {
            System.err.println("AI 문제 생성 실패: " + e.getMessage());
            return generateFallbackProblem(difficulty, topic);
        }
    }

    private String createPrompt(String difficulty, String topic) {
        String levelDescription = getDifficultyDescription(difficulty);
        String topicHint = getTopicHint(topic);
        
        return String.format("""
            당신은 프로그래밍 교육 전문가입니다. 다음 조건에 맞는 Java 코딩 문제를 생성해주세요.

            난이도: %s (%s)
            주제: %s
            %s

            다음 JSON 형식으로 응답해주세요:
            {
                "title": "문제 제목",
                "description": "문제 설명 (상세하고 명확하게, 초보자도 이해할 수 있도록)",
                "example": "입력과 출력 예제 (최소 2개)",
                "hint": "문제 해결을 위한 힌트",
                "testCases": [
                    {"input": "입력값", "output": "예상출력"}
                ],
                "solution": "해답 코드 (참고용)"
            }

            문제는 Java 언어로 풀 수 있어야 하고, Scanner를 사용한 입력과 System.out.println을 사용한 출력을 기본으로 해주세요.
            """, difficulty, levelDescription, topic, topicHint);
    }

    private String callOpenAiApi(String prompt) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.7);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.path("choices").get(0).path("message").path("content").asText();
        } else {
            throw new RuntimeException("OpenAI API 호출 실패: " + response.getStatusCode());
        }
    }

    private Map<String, Object> parseAiResponse(String aiResponse, String difficulty) {
        try {
            // JSON 응답에서 중괄호만 추출
            String jsonContent = extractJsonFromResponse(aiResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", "ai_" + System.currentTimeMillis());
            problem.put("title", jsonNode.path("title").asText());
            problem.put("description", jsonNode.path("description").asText());
            problem.put("example", jsonNode.path("example").asText());
            problem.put("hint", jsonNode.path("hint").asText());
            problem.put("difficulty", difficulty);
            problem.put("points", getDifficultyPoints(difficulty));
            problem.put("solution", jsonNode.path("solution").asText());
            
            // 테스트 케이스 파싱
            List<Map<String, String>> testCases = new ArrayList<>();
            JsonNode testCasesNode = jsonNode.path("testCases");
            if (testCasesNode.isArray()) {
                for (JsonNode testCase : testCasesNode) {
                    Map<String, String> tc = new HashMap<>();
                    tc.put("input", testCase.path("input").asText());
                    tc.put("output", testCase.path("output").asText());
                    testCases.add(tc);
                }
            }
            problem.put("testCases", testCases);
            
            return problem;
        } catch (Exception e) {
            System.err.println("AI 응답 파싱 실패: " + e.getMessage());
            return generateFallbackProblem(difficulty, "기본");
        }
    }

    private String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        throw new RuntimeException("JSON 형식을 찾을 수 없습니다");
    }

    private String getDifficultyDescription(String difficulty) {
        switch (difficulty) {
            case "하": return "초보자 수준, 기본 문법 연습";
            case "중": return "중급자 수준, 알고리즘 사고력 필요";
            case "상": return "고급자 수준, 복잡한 로직 구현";
            default: return "중급자 수준";
        }
    }

    private String getTopicHint(String topic) {
        switch (topic) {
            case "기본": return "변수, 조건문, 반복문을 활용한 기초 문제";
            case "배열": return "배열 조작, 탐색, 정렬 관련 문제";
            case "문자열": return "문자열 처리, 변환, 분석 관련 문제";
            case "알고리즘": return "정렬, 탐색, 동적계획법 등 알고리즘 문제";
            case "수학": return "수학적 계산, 공식 활용 문제";
            default: return "프로그래밍 기초 문제";
        }
    }

    private int getDifficultyPoints(String difficulty) {
        switch (difficulty) {
            case "하": return 15;
            case "중": return 25;
            case "상": return 40;
            default: return 20;
        }
    }

    private Map<String, Object> generateFallbackProblem(String difficulty, String topic) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", "fallback_" + System.currentTimeMillis());
        problem.put("title", "두 수의 합 (AI 대체 문제)");
        problem.put("description", "두 정수 A와 B를 입력받아 A+B를 출력하는 프로그램을 작성하시오.\n\n(AI 서비스를 사용할 수 없어 기본 문제를 제공합니다)");
        problem.put("example", "입력:\n3 5\n\n출력:\n8");
        problem.put("hint", "Scanner를 사용하여 두 정수를 입력받고, + 연산자로 더한 후 결과를 출력하세요.");
        problem.put("difficulty", difficulty);
        problem.put("points", getDifficultyPoints(difficulty));
        problem.put("solution", "import java.util.Scanner;\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int a = sc.nextInt();\n        int b = sc.nextInt();\n        System.out.println(a + b);\n    }\n}");
        
        List<Map<String, String>> testCases = new ArrayList<>();
        testCases.add(Map.of("input", "3 5", "output", "8"));
        testCases.add(Map.of("input", "1 2", "output", "3"));
        problem.put("testCases", testCases);
        
        return problem;
    }
}
