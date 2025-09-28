package com.codebattlearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiProblemService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Map<String, Map<String, Object>> problemCache = new ConcurrentHashMap<>();
    private final Map<String, String> chatCache = new ConcurrentHashMap<>();

    public Map<String, Object> generateSimpleProblem() {
        String[] languages = {"java", "python", "cpp"};
        String randomLang = languages[(int) (Math.random() * languages.length)];
        return generateSimpleProblemWithLanguage(randomLang);
    }

    public Map<String, Object> generateSimpleProblemWithLanguage(String language) {
        String cacheKey = "simple_daily_" + language;
        if (problemCache.containsKey(cacheKey)) {
            Map<String, Object> cached = new HashMap<>(problemCache.get(cacheKey));
            cached.put("id", "daily_" + System.currentTimeMillis());
            return cached;
        }

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackProblem("중", "기본", language);
        }

        try {
            String langName = getLanguageName(language);
            String prompt = String.format("""
                간단한 %s 코딩 문제 1개를 JSON으로 생성하세요. 초보자용이고 5분 내에 풀 수 있어야 합니다.
                
                형식:
                {
                  "title": "문제제목",
                  "description": "문제설명 (2-3줄)",
                  "example": "예제 입출력",
                  "hint": "힌트 1줄"
                }
                """, langName);
            
            String response = callOpenAiApiFast(prompt);
            Map<String, Object> problem = parseSimpleResponse(response, language);
            problemCache.put(cacheKey, problem);
            return problem;
        } catch (Exception e) {
            return generateFallbackProblem("중", "기본", language);
        }
    }

    public Map<String, Object> generateProblem(String difficulty, String topic) {
        return generateProblemWithLanguage(difficulty, topic, "java");
    }

    public Map<String, Object> generateProblemWithLanguage(String difficulty, String topic, String language) {
        String cacheKey = difficulty + "_" + topic + "_" + language;
        if (problemCache.containsKey(cacheKey)) {
            Map<String, Object> cached = new HashMap<>(problemCache.get(cacheKey));
            cached.put("id", "ai_" + System.currentTimeMillis());
            return cached;
        }

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackProblem(difficulty, topic, language);
        }

        try {
            String prompt = createOptimizedProblemPrompt(difficulty, topic, language);
            String response = callOpenAiApi(prompt);
            Map<String, Object> problem = parseAiResponse(response, difficulty, language);
            problemCache.put(cacheKey, problem);
            return problem;
        } catch (Exception e) {
            System.err.println("AI 문제 생성 실패: " + e.getMessage());
            return generateFallbackProblem(difficulty, topic, language);
        }
    }

    public Map<String, Object> gradeAnswer(String problemId, String answer, String language) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackFeedback(answer, language);
        }

        try {
            String prompt = createOptimizedGradingPrompt(answer, language);
            String response = callOpenAiApiFast(prompt);
            return parseGradingResponse(response);
        } catch (Exception e) {
            System.err.println("AI 채점 실패: " + e.getMessage());
            return generateFallbackFeedback(answer, language);
        }
    }

    public String chatWithAI(String userMessage, String conversationHistory) {
        String cacheKey = userMessage.toLowerCase().trim();
        if (chatCache.containsKey(cacheKey)) {
            return chatCache.get(cacheKey);
        }

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackChatResponse(userMessage);
        }

        try {
            String prompt = createOptimizedChatPrompt(userMessage, conversationHistory);
            String response = callOpenAiApiFast(prompt);
            
            if (chatCache.size() < 50) {
                chatCache.put(cacheKey, response);
            }
            
            return response;
        } catch (Exception e) {
            System.err.println("AI 채팅 실패: " + e.getMessage());
            return generateFallbackChatResponse(userMessage);
        }
    }

    private String createOptimizedProblemPrompt(String difficulty, String topic, String language) {
        return String.format("""
            %s 난이도 %s %s 문제를 간단히 생성하세요.
            
            JSON 형식:
            {
                "title": "제목",
                "description": "설명 (3줄 이내)",
                "example": "예제",
                "hint": "힌트",
                "solution": "%s 해답코드"
            }
            """, difficulty, language, topic, language);
    }

    private String createOptimizedGradingPrompt(String answer, String language) {
        return String.format("""
            %s 코드를 빠르게 채점하세요:
            
            %s
            
            JSON 응답:
            {
                "score": 0-100,
                "isCorrect": true/false,
                "feedback": "피드백 (2줄)"
            }
            """, language, answer);
    }

    private String createOptimizedChatPrompt(String userMessage, String history) {
        return String.format("""
            프로그래밍 튜터로서 간단명확하게 답변하세요 (2-3줄):
            
            질문: %s
            
            답변:
            """, userMessage);
    }

    private String callOpenAiApiFast(String prompt) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.1);
        
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

    private String callOpenAiApi(String prompt) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("max_tokens", 1200);
        requestBody.put("temperature", 0.3);
        
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

    private Map<String, Object> parseSimpleResponse(String aiResponse, String language) {
        try {
            String jsonContent = extractJsonFromResponse(aiResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", "daily_" + System.currentTimeMillis());
            problem.put("title", jsonNode.path("title").asText());
            problem.put("description", jsonNode.path("description").asText());
            problem.put("example", jsonNode.path("example").asText());
            problem.put("hint", jsonNode.path("hint").asText());
            problem.put("difficulty", "중");
            problem.put("language", language);
            problem.put("points", 20);
            problem.put("timeLimit", 15);
            problem.put("category", "AI 일일 문제");
            
            return problem;
        } catch (Exception e) {
            return generateFallbackProblem("중", "기본", language);
        }
    }

    private String getLanguageName(String language) {
        switch (language.toLowerCase()) {
            case "java": return "자바";
            case "python": return "파이썬";
            case "cpp":
            case "c++": return "C++";
            case "javascript": return "자바스크립트";
            case "c": return "C";
            default: return "프로그래밍";
        }
    }

    private Map<String, Object> parseAiResponse(String aiResponse, String difficulty, String language) {
        try {
            String jsonContent = extractJsonFromResponse(aiResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", "ai_" + System.currentTimeMillis());
            problem.put("title", jsonNode.path("title").asText());
            problem.put("description", jsonNode.path("description").asText());
            problem.put("example", jsonNode.path("example").asText());
            problem.put("hint", jsonNode.path("hint").asText());
            problem.put("difficulty", difficulty);
            problem.put("language", language);
            problem.put("points", getDifficultyPoints(difficulty));
            problem.put("solution", jsonNode.path("solution").asText());
            problem.put("template", getLanguageTemplate(language));
            problem.put("timeLimit", 20);
            problem.put("category", "AI 문제");
            
            List<Map<String, String>> testCases = new ArrayList<>();
            testCases.add(Map.of("input", "예제입력", "output", "예제출력"));
            problem.put("testCases", testCases);
            
            return problem;
        } catch (Exception e) {
            System.err.println("AI 응답 파싱 실패: " + e.getMessage());
            return generateFallbackProblem(difficulty, "기본", language);
        }
    }

    private Map<String, Object> parseGradingResponse(String aiResponse) {
        try {
            String jsonContent = extractJsonFromResponse(aiResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("score", jsonNode.path("score").asInt());
            feedback.put("isCorrect", jsonNode.path("isCorrect").asBoolean());
            feedback.put("feedback", jsonNode.path("feedback").asText());
            feedback.put("suggestions", jsonNode.path("suggestions").asText());
            feedback.put("correct", jsonNode.path("isCorrect").asBoolean());
            
            return feedback;
        } catch (Exception e) {
            System.err.println("채점 응답 파싱 실패: " + e.getMessage());
            return Map.of(
                "score", 50,
                "isCorrect", false,
                "correct", false,
                "feedback", "자동 채점에 실패했습니다. 수동으로 검토해주세요."
            );
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

    private String getLanguageTemplate(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "import java.util.Scanner;\\npublic class Solution {\\n    public static void main(String[] args) {\\n        // 여기에 코드 작성\\n    }\\n}";
            case "python":
                return "# 여기에 코드 작성\\n";
            case "c++":
            case "cpp":
                return "#include <iostream>\\nusing namespace std;\\n\\nint main() {\\n    // 여기에 코드 작성\\n    return 0;\\n}";
            default:
                return "// 여기에 코드 작성";
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

    private Map<String, Object> generateFallbackProblem(String difficulty, String topic, String language) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", "fallback_" + System.currentTimeMillis());
        problem.put("title", "두 수의 합");
        problem.put("description", "두 정수 A와 B를 입력받아 A+B를 출력하는 프로그램을 작성하시오.");
        problem.put("example", "입력: 3 5\\n출력: 8");
        problem.put("hint", "두 정수를 입력받고 더한 후 결과를 출력하세요.");
        problem.put("difficulty", difficulty);
        problem.put("language", language);
        problem.put("points", getDifficultyPoints(difficulty));
        problem.put("template", getLanguageTemplate(language));
        problem.put("solution", getFallbackSolution(language));
        problem.put("timeLimit", 15);
        problem.put("category", "기본 문제");
        
        List<Map<String, String>> testCases = new ArrayList<>();
        testCases.add(Map.of("input", "3 5", "output", "8"));
        problem.put("testCases", testCases);
        
        return problem;
    }

    private String getFallbackSolution(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "import java.util.Scanner;\\npublic class Solution {\\n    public static void main(String[] args) {\\n        Scanner sc = new Scanner(System.in);\\n        int a = sc.nextInt();\\n        int b = sc.nextInt();\\n        System.out.println(a + b);\\n    }\\n}";
            case "python":
                return "a, b = map(int, input().split())\\nprint(a + b)";
            case "c++":
                return "#include <iostream>\\nusing namespace std;\\n\\nint main() {\\n    int a, b;\\n    cin >> a >> b;\\n    cout << a + b << endl;\\n    return 0;\\n}";
            default:
                return "// 두 수를 입력받아 합을 출력하는 코드";
        }
    }

    private Map<String, Object> generateFallbackFeedback(String answer, String language) {
        int score = 70;
        boolean hasBasicStructure = false;
        
        if (answer.contains("Scanner") || answer.contains("input") || answer.contains("cin")) {
            hasBasicStructure = true;
            score += 15;
        }
        
        if (answer.contains("println") || answer.contains("print") || answer.contains("cout")) {
            hasBasicStructure = true;
            score += 15;
        }
        
        String feedback = hasBasicStructure ? 
            "기본적인 입출력 구조가 잘 갖춰져 있습니다!" : 
            "입출력 부분을 다시 확인해보세요.";
        
        return Map.of(
            "score", Math.min(score, 100),
            "isCorrect", score >= 80,
            "correct", score >= 80,
            "feedback", feedback
        );
    }

    private String generateFallbackChatResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("안녕") || message.contains("hello")) {
            return "안녕하세요! 프로그래밍 관련 질문이 있으시면 언제든 물어보세요.";
        }
        
        if (message.contains("자바") || message.contains("java")) {
            if (message.contains("배열")) {
                return "자바 배열: `int[] arr = new int[5];` 또는 `int[] arr = {1,2,3,4,5};`로 선언합니다.";
            }
            if (message.contains("반복문") || message.contains("for")) {
                return "자바 for문: `for(int i=0; i<10; i++) { }` 형태로 사용합니다.";
            }
            return "자바 관련해서 구체적으로 어떤 부분이 궁금하신가요?";
        }
        
        if (message.contains("파이썬") || message.contains("python")) {
            return "파이썬은 간결하고 읽기 쉬운 문법이 특징입니다. 어떤 기능이 궁금하신가요?";
        }
        
        return "구체적인 프로그래밍 질문을 해주시면 더 정확한 답변을 드릴 수 있습니다.";
    }
}