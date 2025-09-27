@GetMapping("/submissions/{id}/details")
public Map<String, Object> getSubmissionDetails(@PathVariable Long id, HttpServletRequest request) {
    try {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            return Map.of("error", "Unauthorized");
        }

        Submission submission = submissionRepository.findById(id).orElse(null);
        if (submission == null) {
            return Map.of("error", "제출물을 찾을 수 없습니다.");
        }

        Problem problem = problemRepository.findById(submission.getProblemId()).orElse(null);
        if (problem == null || !problem.getTeacherId().equals(teacherId)) {
            return Map.of("error", "권한이 없습니다.");
        }

        User student = userRepository.findById(submission.getUserId()).orElse(null);
        if (student == null) {
            return Map.of("error", "학생 정보를 찾을 수 없습니다.");
        }

        Map<String, Object> submissionData = new HashMap<>();
        submissionData.put("id", submission.getId());
        submissionData.put("answer", submission.getAnswer());
        submissionData.put("fileUrl", submission.getFileUrl());
        submissionData.put("fileName", submission.getFileName());
        submissionData.put("status", submission.getStatus());
        submissionData.put("score", submission.getScore());
        submissionData.put("feedback", submission.getFeedback());
        submissionData.put("submittedAt", submission.getSubmittedAt());

        Map<String, Object> problemData = new HashMap<>();
        problemData.put("id", problem.getId());
        problemData.put("title", problem.getTitle());
        problemData.put("description", problem.getDescription());
        problemData.put("difficulty", problem.getDifficulty());
        problemData.put("type", problem.getType());
        problemData.put("timeLimit", problem.getTimeLimit());
        problemData.put("points", problem.getPoints());
        problemData.put("optionA", problem.getOptionA());
        problemData.put("optionB", problem.getOptionB());
        problemData.put("optionC", problem.getOptionC());
        problemData.put("optionD", problem.getOptionD());
        problemData.put("correctAnswer", problem.getCorrectAnswer());
        problemData.put("createdAt", problem.getCreatedAt());

        Map<String, Object> studentData = new HashMap<>();
        studentData.put("id", student.getId());
        studentData.put("name", student.getName());
        studentData.put("email", student.getEmail());

        return Map.of(
            "submission", submissionData,
            "problem", problemData,
            "student", studentData
        );

    } catch (Exception e) {
        return Map.of("error", "상세 정보 로드 실패: " + e.getMessage());
    }
}
