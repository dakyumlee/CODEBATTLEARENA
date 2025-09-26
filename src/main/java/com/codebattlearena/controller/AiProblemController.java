package com.codebattlearena.controller;

import com.codebattlearena.service.AiProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiProblemController {

    @Autowired
    private AiProblemService aiProblemService;

    @PostMapping("/generate-problem")
    public Map<String, Object> generateProblem(@RequestBody Map<String, String> request) {
        String difficulty = request.getOrDefault("difficulty", "중");
        String topic = request.getOrDefault("topic", "기본");
        
        return aiProblemService.generateProblem(difficulty, topic);
    }

    @PostMapping("/analyze-code")
    public Map<String, Object> analyzeCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String problemId = request.get("problemId");
        
        // 간단한 코드 분석 (실제로는 더 정교한 분석 필요)
        Map<String, Object> analysis = Map.of(
            "score", analyzeCodeScore(code),
            "feedback", generateFeedback(code),
            "suggestions", generateSuggestions(code)
        );
        
        return analysis;
    }

    private int analyzeCodeScore(String code) {
        int score = 0;
        
        if (code.contains("Scanner")) score += 20;
        if (code.contains("System.out")) score += 20;
        if (code.contains("if") || code.contains("for") || code.contains("while")) score += 20;
        if (code.contains("+") || code.contains("-") || code.contains("*") || code.contains("/")) score += 20;
        if (code.length() > 100) score += 20;
        
        return Math.min(score, 100);
    }

    private String generateFeedback(String code) {
        if (code.length() < 50) {
            return "코드가 너무 짧습니다. 더 자세히 구현해보세요.";
        } else if (!code.contains("Scanner")) {
            return "입력을 받는 부분이 없습니다. Scanner를 사용해보세요.";
        } else if (!code.contains("System.out")) {
            return "출력하는 부분이 없습니다. System.out.println을 사용해보세요.";
        } else {
            return "좋은 코드입니다! 계속 발전시켜보세요.";
        }
    }

    private String generateSuggestions(String code) {
        if (!code.contains("Scanner")) {
            return "Scanner sc = new Scanner(System.in); 으로 입력을 받아보세요.";
        } else if (!code.contains("System.out")) {
            return "System.out.println()으로 결과를 출력해보세요.";
        } else {
            return "변수명을 더 명확하게 하거나 주석을 추가해보세요.";
        }
    }
}
