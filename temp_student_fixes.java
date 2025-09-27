// StudentController에 추가
@GetMapping("/ai-problems")
public Map<String, Object> getAiProblems(HttpServletRequest request) {
    try {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Map.of("error", "Unauthorized");
        }
        
        List<Map<String, Object>> aiProblems = Arrays.asList(
            Map.of(
                "id", "ai-1",
                "title", "배열의 최댓값 찾기",
                "description", "주어진 정수 배열에서 최댓값을 찾는 함수를 작성하세요.\n\n입력: [3, 1, 4, 1, 5, 9, 2, 6]\n출력: 9",
                "difficulty", "하",
                "category", "배열",
                "timeLimit", 30,
                "points", 100
            ),
            Map.of(
                "id", "ai-2", 
                "title", "문자열 뒤집기",
                "description", "주어진 문자열을 뒤집어 반환하는 함수를 작성하세요.\n\n입력: \"Hello\"\n출력: \"olleH\"",
                "difficulty", "하",
                "category", "문자열",
                "timeLimit", 20,
                "points", 80
            ),
            Map.of(
                "id", "ai-3", 
                "title", "피보나치 수열",
                "description", "n번째 피보나치 수를 구하는 함수를 작성하세요.\n\n입력: 10\n출력: 55",
                "difficulty", "중",
                "category", "DP",
                "timeLimit", 45,
                "points", 150
            )
        );
        
        return Map.of("success", true, "problems", aiProblems);
    } catch (Exception e) {
        return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
    }
}
