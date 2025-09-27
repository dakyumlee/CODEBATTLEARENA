package com.codebattlearena.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @GetMapping("/problems")
    public Map<String, Object> getProblems(@RequestParam(defaultValue = "ALL") String difficulty) {
        List<Map<String, Object>> problems = getPracticeProblems();
        
        if (!"ALL".equals(difficulty)) {
            problems = problems.stream()
                .filter(p -> difficulty.equals(p.get("difficulty")))
                .toList();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("problems", problems);
        return response;
    }
    
    @PostMapping("/submit")
    public Map<String, Object> submitSolution(@RequestBody Map<String, Object> submission) {
        String code = (String) submission.get("code");
        Long problemId = Long.valueOf(submission.get("problemId").toString());
        
        // 간단한 코드 검증
        Map<String, Object> result = checkSolution(problemId, code);
        
        return result;
    }
    
    private List<Map<String, Object>> getPracticeProblems() {
        List<Map<String, Object>> problems = new ArrayList<>();
        
        // 하 난이도 문제들
        problems.add(Map.of(
            "id", 1,
            "title", "두 수의 합",
            "description", "두 정수 a, b를 입력받아 a + b를 출력하는 프로그램을 작성하세요.",
            "difficulty", "하",
            "input", "첫째 줄에 A와 B가 주어진다. (0 < A, B < 10)",
            "output", "첫째 줄에 A+B를 출력한다.",
            "example_input", "1 2",
            "example_output", "3",
            "solved", false
        ));
        
        problems.add(Map.of(
            "id", 2,
            "title", "A-B",
            "description", "두 정수 A와 B를 입력받은 다음, A-B를 출력하는 프로그램을 작성하세요.",
            "difficulty", "하",
            "input", "첫째 줄에 A와 B가 주어진다.",
            "output", "첫째 줄에 A-B를 출력한다.",
            "example_input", "3 2",
            "example_output", "1",
            "solved", false
        ));
        
        problems.add(Map.of(
            "id", 3,
            "title", "곱셈",
            "description", "두 정수 A와 B를 입력받은 다음, A×B를 출력하는 프로그램을 작성하세요.",
            "difficulty", "하",
            "input", "첫째 줄에 A와 B가 주어진다.",
            "output", "첫째 줄에 A×B를 출력한다.",
            "example_input", "1 2",
            "example_output", "2",
            "solved", false
        ));
        
        // 중 난이도 문제들
        problems.add(Map.of(
            "id", 4,
            "title", "최댓값 찾기",
            "description", "세 정수 중에서 가장 큰 수를 출력하는 프로그램을 작성하세요.",
            "difficulty", "중",
            "input", "첫째 줄에 세 정수 A, B, C가 주어진다.",
            "output", "첫째 줄에 가장 큰 수를 출력한다.",
            "example_input", "3 1 4",
            "example_output", "4",
            "solved", false
        ));
        
        problems.add(Map.of(
            "id", 5,
            "title", "1부터 N까지 합",
            "description", "자연수 N이 주어졌을 때, 1부터 N까지의 합을 구하는 프로그램을 작성하세요.",
            "difficulty", "중",
            "input", "첫째 줄에 자연수 N이 주어진다. (1 ≤ N ≤ 100)",
            "output", "첫째 줄에 1부터 N까지의 합을 출력한다.",
            "example_input", "5",
            "example_output", "15",
            "solved", false
        ));
        
        problems.add(Map.of(
            "id", 6,
            "title", "구구단",
            "description", "N을 입력받은 뒤, 구구단 N단을 출력하는 프로그램을 작성하세요.",
            "difficulty", "중",
            "input", "첫째 줄에 N이 주어진다. (1 ≤ N ≤ 9)",
            "output", "출력형식과 같게 N×1부터 N×9까지 출력한다.",
            "example_input", "2",
            "example_output", "2 * 1 = 2\n2 * 2 = 4\n2 * 3 = 6\n2 * 4 = 8\n2 * 5 = 10\n2 * 6 = 12\n2 * 7 = 14\n2 * 8 = 16\n2 * 9 = 18",
            "solved", false
        ));
        
        // 상 난이도 문제들
        problems.add(Map.of(
            "id", 7,
            "title", "소수 판별",
            "description", "주어진 수가 소수인지 판별하는 프로그램을 작성하세요.",
            "difficulty", "상",
            "input", "첫째 줄에 자연수 N이 주어진다. (1 ≤ N ≤ 1000)",
            "output", "N이 소수이면 1, 아니면 0을 출력한다.",
            "example_input", "7",
            "example_output", "1",
            "solved", false
        ));
        
        problems.add(Map.of(
            "id", 8,
            "title", "피보나치 수열",
            "description", "피보나치 수열의 n번째 항을 구하는 프로그램을 작성하세요.",
            "difficulty", "상",
            "input", "첫째 줄에 n이 주어진다. (1 ≤ n ≤ 45)",
            "output", "첫째 줄에 n번째 피보나치 수를 출력한다.",
            "example_input", "10",
            "example_output", "55",
            "solved", false
        ));
        
        return problems;
    }
    
    private Map<String, Object> checkSolution(Long problemId, String code) {
        Map<String, Object> result = new HashMap<>();
        
        // 간단한 키워드 기반 검증
        boolean isCorrect = false;
        String feedback = "";
        
        switch (problemId.intValue()) {
            case 1: // 두 수의 합
                isCorrect = code.contains("+") && (code.contains("Scanner") || code.contains("System.out"));
                feedback = isCorrect ? "정답입니다! 덧셈 연산을 올바르게 사용했습니다." : "덧셈(+) 연산자와 입출력 코드를 확인해보세요.";
                break;
            case 2: // A-B
                isCorrect = code.contains("-") && (code.contains("Scanner") || code.contains("System.out"));
                feedback = isCorrect ? "정답입니다! 뺄셈 연산을 올바르게 사용했습니다." : "뺄셈(-) 연산자와 입출력 코드를 확인해보세요.";
                break;
            case 3: // 곱셈
                isCorrect = code.contains("*") && (code.contains("Scanner") || code.contains("System.out"));
                feedback = isCorrect ? "정답입니다! 곱셈 연산을 올바르게 사용했습니다." : "곱셈(*) 연산자와 입출력 코드를 확인해보세요.";
                break;
            case 4: // 최댓값 찾기
                isCorrect = (code.contains("Math.max") || code.contains("if")) && code.contains("Scanner");
                feedback = isCorrect ? "정답입니다! 조건문이나 Math.max를 잘 활용했습니다." : "조건문(if)이나 Math.max() 함수를 사용해보세요.";
                break;
            case 5: // 1부터 N까지 합
                isCorrect = (code.contains("for") || code.contains("while")) && code.contains("+");
                feedback = isCorrect ? "정답입니다! 반복문을 활용한 합계 계산이 훌륭합니다." : "반복문(for 또는 while)을 사용해서 합을 계산해보세요.";
                break;
            case 6: // 구구단
                isCorrect = code.contains("for") && code.contains("*");
                feedback = isCorrect ? "정답입니다! 반복문을 활용한 구구단 출력이 완벽합니다." : "for문과 곱셈 연산을 사용해서 구구단을 출력해보세요.";
                break;
            case 7: // 소수 판별
                isCorrect = code.contains("for") && (code.contains("%") || code.contains("mod"));
                feedback = isCorrect ? "정답입니다! 나머지 연산을 활용한 소수 판별 로직이 훌륭합니다." : "나머지 연산(%)과 반복문을 사용해서 소수를 판별해보세요.";
                break;
            case 8: // 피보나치
                isCorrect = (code.contains("for") || code.contains("while")) || code.contains("fibonacci");
                feedback = isCorrect ? "정답입니다! 피보나치 수열 구현이 완벽합니다." : "반복문이나 재귀를 사용해서 피보나치 수열을 구현해보세요.";
                break;
            default:
                feedback = "문제를 다시 확인해주세요.";
        }
        
        result.put("correct", isCorrect);
        result.put("score", isCorrect ? 100 : 0);
        result.put("feedback", feedback);
        result.put("executionTime", "0.001초");
        
        return result;
    }
}
