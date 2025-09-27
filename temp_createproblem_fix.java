@PostMapping("/problems")
public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpServletRequest request) {
    try {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            return Map.of("success", false, "message", "인증이 필요합니다.");
        }

        Problem problem = new Problem();
        problem.setTeacherId(teacherId);
        problem.setTitle((String) problemData.get("title"));
        problem.setDescription((String) problemData.get("description"));
        problem.setDifficulty((String) problemData.get("difficulty"));
        problem.setType((String) problemData.get("type"));
        problem.setTimeLimit(problemData.get("timeLimit") != null ? 
            Integer.parseInt(problemData.get("timeLimit").toString()) : 60);
        problem.setPoints(problemData.get("points") != null ? 
            Integer.parseInt(problemData.get("points").toString()) : 100);
        
        // 퀴즈인 경우 4지선다 데이터 추가
        if ("QUIZ".equals(problem.getType())) {
            problem.setOptionA((String) problemData.get("optionA"));
            problem.setOptionB((String) problemData.get("optionB"));
            problem.setOptionC((String) problemData.get("optionC"));
            problem.setOptionD((String) problemData.get("optionD"));
            problem.setCorrectAnswer((String) problemData.get("correctAnswer"));
        }
        
        problem.setCreatedAt(LocalDateTime.now());

        Problem savedProblem = problemRepository.save(problem);

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "QUIZ".equals(problem.getType()) ? "NEW_QUIZ" : "NEW_PROBLEM");
        notification.put("title", "QUIZ".equals(problem.getType()) ? "새로운 퀴즈가 출제되었습니다!" : "새로운 문제가 출제되었습니다!");
        notification.put("message", savedProblem.getTitle());
        notification.put("problemId", savedProblem.getId());
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/notifications", notification);

        return Map.of("success", true, "message", "QUIZ".equals(problem.getType()) ? "퀴즈가 성공적으로 생성되었습니다." : "문제가 성공적으로 출제되었습니다.", "problem", savedProblem);
    } catch (Exception e) {
        return Map.of("success", false, "message", "오류: " + e.getMessage());
    }
}
