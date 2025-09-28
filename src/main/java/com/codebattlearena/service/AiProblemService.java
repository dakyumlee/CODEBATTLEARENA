package com.codebattlearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiProblemService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateProblem(String difficulty, String topic) {
        return generateProblemWithLanguage(difficulty, topic, "java");
    }

    public Map<String, Object> generateProblemWithLanguage(String difficulty, String topic, String language) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackProblem(difficulty, topic, language);
        }

        try {
            String prompt = createProblemPrompt(difficulty, topic, language);
            String response = callOpenAiApi(prompt);
            return parseAiResponse(response, difficulty, language);
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
            String prompt = createGradingPrompt(answer, language);
            String response = callOpenAiApi(prompt);
            return parseGradingResponse(response);
        } catch (Exception e) {
            System.err.println("AI 채점 실패: " + e.getMessage());
            return generateFallbackFeedback(answer, language);
        }
    }

    public String chatWithAI(String userMessage, String conversationHistory) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackChatResponse(userMessage);
        }

        try {
            String prompt = createChatPrompt(userMessage, conversationHistory);
            return callOpenAiApi(prompt);
        } catch (Exception e) {
            System.err.println("AI 채팅 실패: " + e.getMessage());
            return generateFallbackChatResponse(userMessage);
        }
    }

    private String createProblemPrompt(String difficulty, String topic, String language) {
        String levelDescription = getDifficultyDescription(difficulty);
        String topicHint = getTopicHint(topic);
        String langTemplate = getLanguageTemplate(language);
        
        return String.format("""
            당신은 프로그래밍 교육 전문가입니다. 다음 조건에 맞는 %s 코딩 문제를 생성해주세요.

            언어: %s
            난이도: %s (%s)
            주제: %s
            %s

            다음 JSON 형식으로 응답해주세요:
            {
                "title": "문제 제목",
                "description": "문제 설명 (상세하고 명확하게, 초보자도 이해할 수 있도록)",
                "example": "입력과 출력 예제 (최소 2개)",
                "hint": "문제 해결을 위한 힌트",
                "template": "%s",
                "testCases": [
                    {"input": "입력값", "output": "예상출력"}
                ],
                "solution": "해답 코드 (참고용)"
            }

            %s
            """, language, language, difficulty, levelDescription, topic, topicHint, 
            langTemplate, getLanguageSpecificInstructions(language));
    }

    private String createGradingPrompt(String answer, String language) {
        return String.format("""
            다음 %s 코드를 채점해주세요:

            코드:
            %s

            다음 JSON 형식으로 응답해주세요:
            {
                "score": 점수(0-100),
                "isCorrect": true/false,
                "feedback": "상세한 피드백",
                "suggestions": "개선 제안",
                "syntaxErrors": "문법 오류 (있다면)",
                "logicErrors": "논리 오류 (있다면)"
            }

            채점 기준:
            1. 문법 정확성 (30점)
            2. 논리 정확성 (40점)
            3. 코드 효율성 (20점)
            4. 가독성 (10점)
            """, language, answer);
    }

    private String createChatPrompt(String userMessage, String history) {
        return String.format("""
            당신은 친근하고 전문적인 프로그래밍 튜터입니다.
            
            대화 규칙:
            1. 한국어로 친근하게 답변
            2. 2-3문장 정도로 간결하게
            3. 구체적이고 실용적인 조언 제공
            4. 격려와 동기부여 포함
            
            이전 대화: %s
            
            학생 질문: %s
            
            답변:
            """, history != null ? history : "없음", userMessage);
    }

    private String callOpenAiApi(String prompt) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
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
            problem.put("template", jsonNode.path("template").asText());
            problem.put("timeLimit", 30);
            problem.put("category", "AI 문제");
            
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
            feedback.put("syntaxErrors", jsonNode.path("syntaxErrors").asText());
            feedback.put("logicErrors", jsonNode.path("logicErrors").asText());
            
            return feedback;
        } catch (Exception e) {
            System.err.println("채점 응답 파싱 실패: " + e.getMessage());
            return Map.of(
                "score", 50,
                "isCorrect", false,
                "feedback", "죄송합니다. 자동 채점에 실패했습니다.",
                "suggestions", "코드를 다시 확인해보세요.",
                "syntaxErrors", "",
                "logicErrors", ""
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
            case "c":
                return "#include <stdio.h>\\n\\nint main() {\\n    // 여기에 코드 작성\\n    return 0;\\n}";
            default:
                return "// 여기에 코드 작성";
        }
    }

    private String getLanguageSpecificInstructions(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "Scanner를 사용한 입력과 System.out.println을 사용한 출력을 기본으로 해주세요.";
            case "python":
                return "input() 함수를 사용한 입력과 print() 함수를 사용한 출력을 기본으로 해주세요.";
            case "c++":
            case "cpp":
                return "cin을 사용한 입력과 cout을 사용한 출력을 기본으로 해주세요.";
            case "c":
                return "scanf를 사용한 입력과 printf를 사용한 출력을 기본으로 해주세요.";
            default:
                return "해당 언어의 표준 입출력을 사용해주세요.";
        }
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

    private Map<String, Object> generateFallbackProblem(String difficulty, String topic, String language) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", "fallback_" + System.currentTimeMillis());
        problem.put("title", "두 수의 합 (AI 대체 문제)");
        problem.put("description", "두 정수 A와 B를 입력받아 A+B를 출력하는 프로그램을 작성하시오.\n\n(AI 서비스를 사용할 수 없어 기본 문제를 제공합니다)");
        problem.put("example", "입력:\n3 5\n\n출력:\n8");
        problem.put("hint", "두 정수를 입력받고 더한 후 결과를 출력하세요.");
        problem.put("difficulty", difficulty);
        problem.put("language", language);
        problem.put("points", getDifficultyPoints(difficulty));
        problem.put("template", getLanguageTemplate(language));
        problem.put("solution", getFallbackSolution(language));
        problem.put("timeLimit", 30);
        problem.put("category", "기본 문제");
        
        List<Map<String, String>> testCases = new ArrayList<>();
        testCases.add(Map.of("input", "3 5", "output", "8"));
        testCases.add(Map.of("input", "1 2", "output", "3"));
        testCases.add(Map.of("input", "10 20", "output", "30"));
        problem.put("testCases", testCases);
        
        return problem;
    }

    private String getFallbackSolution(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "import java.util.Scanner;\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int a = sc.nextInt();\n        int b = sc.nextInt();\n        System.out.println(a + b);\n    }\n}";
            case "python":
                return "a, b = map(int, input().split())\nprint(a + b)";
            case "c++":
            case "cpp":
                return "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}";
            case "c":
                return "#include <stdio.h>\n\nint main() {\n    int a, b;\n    scanf(\"%d %d\", &a, &b);\n    printf(\"%d\\n\", a + b);\n    return 0;\n}";
            default:
                return "// 두 수를 입력받아 합을 출력하는 코드";
        }
    }

    private Map<String, Object> generateFallbackFeedback(String answer, String language) {
        int score = 70;
        boolean hasBasicStructure = false;
        String feedback = "AI 채점 서비스를 사용할 수 없어 기본 피드백을 제공합니다.\n\n";
        
        if (answer.contains("Scanner") || answer.contains("input") || answer.contains("cin") || answer.contains("scanf")) {
            hasBasicStructure = true;
            score += 15;
        }
        
        if (answer.contains("println") || answer.contains("print") || answer.contains("cout") || answer.contains("printf")) {
            hasBasicStructure = true;
            score += 15;
        }
        
        if (hasBasicStructure) {
            feedback += "기본적인 입출력 구조가 잘 갖춰져 있습니다.";
        } else {
            feedback += "입출력 부분을 다시 확인해보세요.";
        }
        
        return Map.of(
            "score", Math.min(score, 100),
            "isCorrect", score >= 80,
            "feedback", feedback,
            "suggestions", "코드 구조와 문법을 다시 한번 확인해보세요.",
            "syntaxErrors", "",
            "logicErrors", ""
        );
    }

    private String generateFallbackChatResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("안녕") || message.contains("hello")) {
            return "안녕하세요! 코딩 공부하다가 막히는 부분이 있으면 언제든 물어보세요!";
        }
        
        if (message.contains("자바") || message.contains("java")) {
            if (message.contains("어려워") || message.contains("힘들어")) {
                return "자바는 처음엔 어렵지만 차근차근 하면 분명 늘어요! 어떤 부분이 가장 어려우신가요?";
            }
            return "자바 관련해서 궁금한 게 있으시군요! 구체적으로 어떤 부분이 궁금하신가요?";
        }
        
        if (message.contains("파이썬") || message.contains("python")) {
            return "파이썬은 문법이 간단해서 배우기 좋은 언어예요. 어떤 것이 궁금하신가요?";
        }
        
        if (message.contains("c++") || message.contains("c언어")) {
            return "C/C++는 기초가 탄탄해야 하는 언어예요. 포인터나 메모리 관리 부분이 특히 중요하죠.";
        }
        
        if (message.contains("알고리즘")) {
            return "알고리즘은 문제를 단계별로 분해해서 생각하는 게 핵심이에요. 어떤 알고리즘이 궁금하신가요?";
        }
        
        if (message.contains("오류") || message.contains("에러")) {
            return "오류가 났군요! 오류 메시지를 정확히 알려주시면 더 구체적으로 도와드릴 수 있어요.";
        }
        
        return "구체적으로 어떤 부분이 궁금하신지 알려주시면 더 정확한 답변을 드릴 수 있어요!";
    }
}