package com.codebattlearena.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class OpenAIService {

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "AI 서비스가 설정되지 않았습니다. 관리자에게 문의하세요.";
        }

        try {
            Map<String, Object> requestBody = createChatRequest(message, "당신은 프로그래밍 학습을 도와주는 친절한 AI 튜터입니다. 한국어로 답변하고, 초보자도 이해하기 쉽게 설명해주세요.", 1000);
            return callOpenAI(requestBody);
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 오류: " + e.getMessage());
            return "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다.";
        }
    }

    public Map<String, Object> generateStructuredProblem(String difficulty) {
        if (apiKey == null || apiKey.isEmpty()) {
            return generateFallbackProblem(difficulty);
        }

        try {
            String prompt = String.format(
                "난이도 '%s'의 프로그래밍 문제를 생성해주세요. 반드시 JSON 형식으로만 응답하세요:\n" +
                "{\n" +
                "  \"title\": \"문제 제목 (간단명료하게)\",\n" +
                "  \"description\": \"문제 설명 (150자 이내, 구체적이고 명확하게)\",\n" +
                "  \"example\": \"입력 예시:\\n5\\n\\n출력 예시:\\n5\",\n" +
                "  \"constraints\": \"제약 조건\",\n" +
                "  \"points\": %d\n" +
                "}",
                difficulty, getPointsByDifficulty(difficulty)
            );

            Map<String, Object> requestBody = createChatRequest(prompt, "프로그래밍 문제 생성 전문가", 800);
            String response = callOpenAI(requestBody);
            
            try {
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                result.put("difficulty", difficulty);
                return result;
            } catch (Exception e) {
                return parseManuallyFromContent(response, difficulty);
            }

        } catch (Exception e) {
            System.err.println("구조화된 문제 생성 실패: " + e.getMessage());
            return generateFallbackProblem(difficulty);
        }
    }

    public String evaluateCode(String code, String problemDescription, String difficulty) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getBasicEvaluation(code, difficulty);
        }

        try {
            String prompt = String.format(
                "다음 코드를 평가해주세요:\n\n문제: %s\n\n제출된 코드:\n%s\n\n" +
                "평가 기준:\n1. 문제 해결 여부\n2. 코드 품질\n3. 효율성\n\n" +
                "100점 만점으로 점수를 매기고, 간단한 피드백을 한국어로 100자 이내로 제공해주세요.\n" +
                "형식: '점수: XX점\\n피드백: ...'",
                problemDescription, code
            );

            Map<String, Object> requestBody = createChatRequest(prompt, "코드 평가 전문가", 300);
            String response = callOpenAI(requestBody);
            
            return parseEvaluationResponse(response, difficulty);

        } catch (Exception e) {
            System.err.println("코드 평가 실패: " + e.getMessage());
            return getBasicEvaluation(code, difficulty);
        }
    }

    public String generateHint(String problemTitle, String problemDescription) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "단계별로 문제를 나누어 접근해보세요. 입력과 출력의 관계를 파악하는 것부터 시작하세요.";
        }

        try {
            String prompt = String.format(
                "코딩 문제 힌트를 제공해주세요:\n제목: %s\n설명: %s\n\n" +
                "답을 직접 주지 말고, 접근 방법이나 사고 과정을 3줄 이내로 간단히 안내해주세요.",
                problemTitle, problemDescription
            );

            Map<String, Object> requestBody = createChatRequest(prompt, "코딩 문제 힌트 제공자", 200);
            return callOpenAI(requestBody);

        } catch (Exception e) {
            System.err.println("힌트 생성 실패: " + e.getMessage());
            return "문제를 작은 단위로 나누어 생각해보세요. 입력과 출력의 관계를 파악하는 것부터 시작하세요.";
        }
    }

    public String generateBattleProblem(String difficulty) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getBattleFallbackProblem(difficulty);
        }

        try {
            String prompt = String.format(
                "코드배틀용 '%s' 난이도 문제를 생성해주세요. 빠른 구현이 가능한 문제여야 합니다.\n" +
                "JSON 형식으로 응답:\n{\n" +
                "  \"title\": \"문제 제목\",\n" +
                "  \"description\": \"문제 설명 (100자 이내)\",\n" +
                "  \"timeLimit\": %d\n}",
                difficulty, getBattleTimeLimit(difficulty)
            );

            Map<String, Object> requestBody = createChatRequest(prompt, "코드배틀 문제 생성자", 500);
            String response = callOpenAI(requestBody);
            
            try {
                return objectMapper.writeValueAsString(objectMapper.readValue(response, Map.class));
            } catch (Exception e) {
                return getBattleFallbackProblem(difficulty);
            }

        } catch (Exception e) {
            return getBattleFallbackProblem(difficulty);
        }
    }

    public String judgeBattleResult(String code1, String code2, String problemDescription) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getRandomBattleResult();
        }

        try {
            String prompt = String.format(
                "코드배틀 심판을 해주세요:\n문제: %s\n\n코드1:\n%s\n\n코드2:\n%s\n\n" +
                "정확성, 효율성, 코드 품질을 종합적으로 평가하여 승자를 결정해주세요.\n" +
                "'승자: 코드1' 또는 '승자: 코드2' 또는 '무승부' 형식으로만 응답해주세요.",
                problemDescription, code1, code2
            );

            Map<String, Object> requestBody = createChatRequest(prompt, "코드배틀 심판", 100);
            String response = callOpenAI(requestBody);
            
            return parseBattleResult(response);

        } catch (Exception e) {
            return getRandomBattleResult();
        }
    }

    private Map<String, Object> createChatRequest(String userMessage, String systemMessage, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", systemMessage);
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        requestBody.put("messages", messages);
        return requestBody;
    }

    private String callOpenAI(Map<String, Object> requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", request, Map.class);

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
        throw new RuntimeException("OpenAI API 응답 오류");
    }

    private Map<String, Object> parseManuallyFromContent(String content, String difficulty) {
        try {
            String title = extractJsonValue(content, "title");
            String description = extractJsonValue(content, "description");
            String example = extractJsonValue(content, "example");
            String constraints = extractJsonValue(content, "constraints");

            return Map.of(
                "title", title != null ? title : "AI 생성 문제",
                "description", description != null ? description : "AI가 생성한 코딩 문제입니다.",
                "difficulty", difficulty,
                "example", example != null ? example : "입력: 5\n출력: 5",
                "constraints", constraints != null ? constraints : "1 ≤ N ≤ 100",
                "points", getPointsByDifficulty(difficulty)
            );
        } catch (Exception e) {
            return generateFallbackProblem(difficulty);
        }
    }

    private String extractJsonValue(String content, String key) {
        try {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("JSON 값 추출 실패: " + key);
        }
        return null;
    }

    private String parseEvaluationResponse(String response, String difficulty) {
        try {
            Pattern scorePattern = Pattern.compile("점수\\s*:?\\s*(\\d+)");
            Matcher scoreMatcher = scorePattern.matcher(response);
            
            int score = 70; // 기본 점수
            if (scoreMatcher.find()) {
                score = Integer.parseInt(scoreMatcher.group(1));
            }
            
            Pattern feedbackPattern = Pattern.compile("피드백\\s*:?\\s*(.+)");
            Matcher feedbackMatcher = feedbackPattern.matcher(response);
            
            String feedback = "코드가 제출되었습니다.";
            if (feedbackMatcher.find()) {
                feedback = feedbackMatcher.group(1).trim();
            }
            
            return String.format("점수: %d점\n%s", score, feedback);
            
        } catch (Exception e) {
            return getBasicEvaluation("", difficulty);
        }
    }

    private String parseBattleResult(String response) {
        if (response.contains("코드1")) return "PLAYER1";
        if (response.contains("코드2")) return "PLAYER2";
        if (response.contains("무승부")) return "DRAW";
        return getRandomBattleResult();
    }

    private String getBasicEvaluation(String code, String difficulty) {
        int baseScore = switch(difficulty) {
            case "하" -> 80;
            case "중" -> 75;
            case "상" -> 70;
            default -> 75;
        };
        
        if (code.length() < 50) baseScore -= 10;
        if (code.length() > 500) baseScore += 5;
        
        return String.format("점수: %d점\n코드가 제출되었습니다. 강사님이 추가 검토해드리겠습니다.", baseScore);
    }

    private String getRandomBattleResult() {
        String[] results = {"PLAYER1", "PLAYER2", "DRAW"};
        return results[new Random().nextInt(results.length)];
    }

    private String getBattleFallbackProblem(String difficulty) {
        Map<String, Object> problem = Map.of(
            "title", "빠른 계산",
            "description", "주어진 수의 팩토리얼을 구하세요",
            "timeLimit", getBattleTimeLimit(difficulty)
        );
        try {
            return objectMapper.writeValueAsString(problem);
        } catch (Exception e) {
            return "{\"title\":\"빠른 계산\",\"description\":\"주어진 수의 팩토리얼을 구하세요\",\"timeLimit\":300}";
        }
    }

    private int getPointsByDifficulty(String difficulty) {
        return switch(difficulty) {
            case "하" -> 10;
            case "중" -> 20;
            case "상" -> 30;
            default -> 15;
        };
    }

    private int getBattleTimeLimit(String difficulty) {
        return switch(difficulty) {
            case "하" -> 600; // 10분
            case "중" -> 900; // 15분
            case "상" -> 1200; // 20분
            default -> 600;
        };
    }

    private Map<String, Object> generateFallbackProblem(String difficulty) {
        List<Map<String, Object>> problems = Arrays.asList(
            Map.of("title", "두 수의 합", "description", "두 정수 A와 B를 입력받아 A+B를 출력하는 프로그램을 작성하시오.", "difficulty", "하", "example", "입력:\\n1 2\\n\\n출력:\\n3", "constraints", "-1000 ≤ A, B ≤ 1000", "points", 10),
            Map.of("title", "배열의 최댓값", "description", "N개의 정수가 주어졌을 때, 이 중에서 가장 큰 값을 찾아 출력하는 프로그램을 작성하시오.", "difficulty", "중", "example", "입력:\\n5\\n3 7 2 9 1\\n\\n출력:\\n9", "constraints", "1 ≤ N ≤ 100", "points", 20),
            Map.of("title", "피보나치 수열", "description", "n번째 피보나치 수를 구하는 프로그램을 작성하시오.", "difficulty", "상", "example", "입력:\\n10\\n\\n출력:\\n55", "constraints", "1 ≤ n ≤ 45", "points", 30)
        );

        Map<String, Object> selected = problems.get(new Random().nextInt(problems.size()));
        Map<String, Object> result = new HashMap<>(selected);
        result.put("difficulty", difficulty);
        result.put("id", "fallback_" + System.currentTimeMillis());
        return result;
    }
}
