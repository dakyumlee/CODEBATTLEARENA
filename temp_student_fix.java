// StudentController의 getTeacherProblems 메서드 수정
@GetMapping("/teacher-problems")
public List<Map<String, Object>> getTeacherProblems(HttpServletRequest request) {
    try {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        List<Problem> problems = problemRepository.findAll();
        List<Map<String, Object>> problemList = new ArrayList<>();
        
        for (Problem problem : problems) {
            Map<String, Object> problemData = new HashMap<>();
            problemData.put("id", problem.getId());
            problemData.put("title", problem.getTitle());
            problemData.put("description", problem.getDescription());
            problemData.put("difficulty", problem.getDifficulty());
            problemData.put("timeLimit", problem.getTimeLimit());
            problemData.put("points", problem.getPoints());
            problemData.put("type", problem.getType());
            
            // 퀴즈 옵션들 추가
            if ("QUIZ".equals(problem.getType())) {
                problemData.put("optionA", problem.getOptionA());
                problemData.put("optionB", problem.getOptionB());
                problemData.put("optionC", problem.getOptionC());
                problemData.put("optionD", problem.getOptionD());
            }
            
            // 제출 상태 확인 - 제출되고 채점된 경우만 완료로 표시
            Submission submission = submissionRepository.findByUserIdAndProblemId(userId, problem.getId()).orElse(null);
            boolean isSubmitted = submission != null;
            boolean isGraded = submission != null && "GRADED".equals(submission.getStatus());
            
            problemData.put("isSubmitted", isSubmitted);
            problemData.put("isGraded", isGraded);
            if (submission != null) {
                problemData.put("score", submission.getScore());
                problemData.put("feedback", submission.getFeedback());
            }
            
            problemList.add(problemData);
        }
        
        return problemList;
    } catch (Exception e) {
        return new ArrayList<>();
    }
}
