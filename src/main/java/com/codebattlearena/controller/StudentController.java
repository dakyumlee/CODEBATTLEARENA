package com.codebattlearena.controller;

import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import com.codebattlearena.service.AiProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProblemRepository problemRepository;
    
    @Autowired
    private StudyNoteRepository studyNoteRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private AiProblemService aiProblemService;

    private final Path fileStorageLocation = Paths.get(System.getProperty("java.io.tmpdir"), "uploads").toAbsolutePath().normalize();

    private Long getUserIdFromSession(HttpSession session) {
        try {
            Object userId = session.getAttribute("userId");
            Object userRole = session.getAttribute("userRole");
            
            if (userId != null && "STUDENT".equals(userRole)) {
                return (Long) userId;
            }
        } catch (Exception e) {
            System.err.println("세션 확인 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return Map.of("error", "User not found");
            }
            
            return Map.of("name", user.getName(), "email", user.getEmail(), "role", user.getRole().toString());
        } catch (Exception e) {
            return Map.of("error", "Failed to load profile: " + e.getMessage());
        }
    }

    @GetMapping("/ai-problems/today")
    public Map<String, Object> getTodayAiProblem(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Map<String, Object> problem = Map.of(
                "id", "daily_" + System.currentTimeMillis(),
                "title", "오늘의 AI 문제",
                "description", "두 수를 입력받아 더한 결과를 출력하는 프로그램을 작성하세요.",
                "difficulty", "하",
                "timeLimit", 10,
                "language", "java"
            );
            
            return Map.of("success", true, "problem", problem);
        } catch (Exception e) {
            return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/ai-problems")
    public Map<String, Object> getAiProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Map<String, Object>> problems = new ArrayList<>();
            
            String[] difficulties = {"하", "중", "상"};
            String[] titles = {
                "두 수의 합", "배열 최대값", "문자열 뒤집기",
                "팩토리얼 계산", "소수 판별", "피보나치 수열",
                "이진 탐색", "정렬 알고리즘", "동적 계획법"
            };
            String[] descriptions = {
                "두 정수를 입력받아 합을 구하는 프로그램",
                "배열에서 가장 큰 값을 찾는 프로그램",
                "주어진 문자열을 뒤집는 프로그램",
                "주어진 수의 팩토리얼을 계산하는 프로그램",
                "주어진 수가 소수인지 판별하는 프로그램",
                "n번째 피보나치 수를 구하는 프로그램",
                "정렬된 배열에서 특정 값을 찾는 프로그램",
                "배열을 오름차순으로 정렬하는 프로그램",
                "최적 부분 구조를 이용한 문제 해결 프로그램"
            };
            
            for (int i = 0; i < 9; i++) {
                Map<String, Object> problem = Map.of(
                    "id", "ai_" + (i + 1),
                    "title", titles[i],
                    "description", descriptions[i],
                    "difficulty", difficulties[i % 3],
                    "timeLimit", (i % 3 + 1) * 10,
                    "category", "AI 문제",
                    "solved", false
                );
                problems.add(problem);
            }
            
            return Map.of("problems", problems);
        } catch (Exception e) {
            return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/ai-problem/generate")
    public Map<String, Object> generateAiProblem(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            String difficulty = request.getOrDefault("difficulty", "중");
            String topic = request.getOrDefault("topic", "기본");
            String language = request.getOrDefault("language", "java");
            
            Map<String, Object> problem = Map.of(
                "id", "gen_" + System.currentTimeMillis(),
                "title", difficulty + " 난이도 " + topic + " 문제",
                "description", "AI가 생성한 " + language + " " + topic + " 문제입니다.",
                "difficulty", difficulty,
                "timeLimit", 15,
                "language", language
            );
            
            return Map.of("success", true, "problem", problem);
        } catch (Exception e) {
            return Map.of("error", "AI 문제 생성 실패: " + e.getMessage());
        }
    }

    @PostMapping("/ai-problem/submit")
public Map<String, Object> submitAiProblem(
    @RequestParam("problemId") String problemId,
    @RequestParam("answer") String answer,
    @RequestParam("language") String language,
    @RequestParam(value = "timeSpent", required = false) Integer timeSpent,
    HttpSession session) {
    
    try {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            return Map.of("success", false, "message", "인증이 필요합니다.");
        }
        
        Map<String, Object> gradingResult = aiProblemService.gradeAnswer(problemId, answer, language);
        
        int score = (Integer) gradingResult.getOrDefault("score", 50);
        boolean isCorrect = (Boolean) gradingResult.getOrDefault("correct", false);
        String feedback = (String) gradingResult.getOrDefault("feedback", "채점 완료");
        
        saveDailyProblemRecord(userId, problemId, answer, score, feedback, timeSpent);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("score", score);
        responseData.put("correct", isCorrect);
        responseData.put("isCorrect", isCorrect);
        responseData.put("feedback", feedback);
        responseData.put("autoClose", true);
        
        return Map.of("success", true, "feedback", responseData);
    } catch (Exception e) {
        System.err.println("AI 문제 제출 오류: " + e.getMessage());
        e.printStackTrace();
        return Map.of("success", false, "message", "채점 실패: " + e.getMessage());
    }
}

private void saveDailyProblemRecord(Long userId, String problemId, String answer, int score, String feedback, Integer timeSpent) {
    try {
        String today = LocalDateTime.now().toLocalDate().toString();
        String title = "AI 일일 문제 - " + today;
        
        Optional<StudyNote> existingNote = studyNoteRepository.findByUserIdAndTitle(userId, title);
        StudyNote dailyRecord;
        
        if (existingNote.isPresent()) {
            dailyRecord = existingNote.get();
            String existingContent = dailyRecord.getContent();
            String newEntry = String.format("\n\n=== 문제 %s ===\n답안: %s\n점수: %d점\n피드백: %s\n소요시간: %d초\n제출시간: %s", 
                problemId, answer.length() > 100 ? answer.substring(0, 100) + "..." : answer, 
                score, feedback, timeSpent != null ? timeSpent : 0, LocalDateTime.now());
            dailyRecord.setContent(existingContent + newEntry);
            dailyRecord.setUpdatedAt(LocalDateTime.now());
        } else {
            dailyRecord = new StudyNote();
            dailyRecord.setUserId(userId);
            dailyRecord.setTitle(title);
            String content = String.format("=== 문제 %s ===\n답안: %s\n점수: %d점\n피드백: %s\n소요시간: %d초\n제출시간: %s", 
                problemId, answer.length() > 100 ? answer.substring(0, 100) + "..." : answer, 
                score, feedback, timeSpent != null ? timeSpent : 0, LocalDateTime.now());
            dailyRecord.setContent(content);
            dailyRecord.setCreatedAt(LocalDateTime.now());
            dailyRecord.setUpdatedAt(LocalDateTime.now());
        }
        
        studyNoteRepository.save(dailyRecord);
    } catch (Exception e) {
        System.err.println("일일 문제 기록 저장 실패: " + e.getMessage());
    }
}

    @GetMapping("/daily-stats")
    public Map<String, Object> getDailyStats(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            String today = LocalDateTime.now().toLocalDate().toString();
            String title = "AI 일일 문제 - " + today;
            
            Optional<StudyNote> todayNote = studyNoteRepository.findByUserIdAndTitle(userId, title);
            
            int problemsSolved = 0;
            int totalScore = 0;
            String lastFeedback = "아직 문제를 풀지 않았습니다.";
            
            if (todayNote.isPresent()) {
                String content = todayNote.get().getContent();
                String[] problems = content.split("=== 문제");
                problemsSolved = problems.length - 1;
                
                if (problemsSolved > 0) {
                    String lastProblem = problems[problems.length - 1];
                    if (lastProblem.contains("점수: ")) {
                        String scoreStr = lastProblem.substring(lastProblem.indexOf("점수: ") + 3);
                        scoreStr = scoreStr.substring(0, scoreStr.indexOf("점"));
                        try {
                            totalScore = Integer.parseInt(scoreStr);
                        } catch (NumberFormatException e) {
                            totalScore = 0;
                        }
                    }
                    
                    if (lastProblem.contains("피드백: ")) {
                        String feedback = lastProblem.substring(lastProblem.indexOf("피드백: ") + 4);
                        feedback = feedback.substring(0, feedback.indexOf("\n"));
                        lastFeedback = feedback;
                    }
                }
            }
            
            return Map.of(
                "problemsSolved", problemsSolved,
                "averageScore", problemsSolved > 0 ? totalScore : 0,
                "lastFeedback", lastFeedback,
                "todayDate", today
            );
        } catch (Exception e) {
            return Map.of("error", "통계를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/practice-problems")
    public Map<String, Object> getPracticeProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Problem> problems = problemRepository.findAll();
            
            List<Map<String, Object>> practiceProblems = new ArrayList<>();
            for (Problem problem : problems) {
                Map<String, Object> problemData = new HashMap<>();
                problemData.put("id", problem.getId());
                problemData.put("title", problem.getTitle());
                problemData.put("description", problem.getDescription());
                problemData.put("difficulty", problem.getDifficulty());
                problemData.put("points", problem.getPoints());
                problemData.put("type", problem.getType());
                problemData.put("timeLimit", problem.getTimeLimit());
                problemData.put("category", "연습 문제");
                
                Optional<Submission> submission = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
                problemData.put("solved", submission.isPresent() && "GRADED".equals(submission.get().getStatus()));
                problemData.put("score", submission.map(Submission::getScore).orElse(null));
                
                practiceProblems.add(problemData);
            }
            
            return Map.of("problems", practiceProblems);
        } catch (Exception e) {
            return Map.of("error", "문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/today")
    public Map<String, Object> getTodayData(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Material> allMaterials = materialRepository.findAll();
            
            List<Map<String, Object>> materialData = allMaterials.stream().map(material -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", material.getId());
                data.put("title", material.getTitle());
                data.put("description", material.getDescription());
                data.put("fileType", material.getFileType());
                data.put("fileSize", material.getFileSize());
                data.put("originalFilename", material.getOriginalFilename());
                data.put("createdAt", material.getCreatedAt());
                data.put("downloadCount", material.getDownloadCount());
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("materials", materialData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load today data: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(material.getLocalFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                material.setDownloadCount(material.getDownloadCount() + 1);
                materialRepository.save(material);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getOriginalFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/materials/{id}/preview")
    public ResponseEntity<Resource> previewMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(material.getLocalFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String fileType = material.getFileType() != null ? material.getFileType().toLowerCase() : "";
                MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                
                if (fileType.equals("pdf")) {
                    mediaType = MediaType.APPLICATION_PDF;
                } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(fileType)) {
                    mediaType = MediaType.IMAGE_JPEG;
                }

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + material.getOriginalFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher-problems")
    public Map<String, Object> getTeacherProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
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
                problemData.put("createdAt", problem.getCreatedAt());
                
                if ("QUIZ".equals(problem.getType())) {
                    problemData.put("optionA", problem.getOptionA());
                    problemData.put("optionB", problem.getOptionB());
                    problemData.put("optionC", problem.getOptionC());
                    problemData.put("optionD", problem.getOptionD());
                }
                
                Optional<Submission> submissionOpt = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
                boolean submitted = submissionOpt.isPresent();
                boolean graded = submissionOpt.isPresent() && "GRADED".equals(submissionOpt.get().getStatus());
                
                problemData.put("submitted", submitted);
                problemData.put("graded", graded);
                
                if (submissionOpt.isPresent()) {
                    Submission submission = submissionOpt.get();
                    problemData.put("score", submission.getScore());
                    problemData.put("feedback", submission.getFeedback());
                    problemData.put("submissionStatus", submission.getStatus());
                }
                
                problemList.add(problemData);
            }
            
            return Map.of("problems", problemList);
        } catch (Exception e) {
            return Map.of("error", "Failed to load teacher problems: " + e.getMessage());
        }
    }

    @GetMapping("/problem/{id}")
    public Map<String, Object> getProblemDetail(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null) {
                return Map.of("error", "문제를 찾을 수 없습니다.");
            }
            
            Map<String, Object> problemData = new HashMap<>();
            problemData.put("id", problem.getId());
            problemData.put("title", problem.getTitle());
            problemData.put("description", problem.getDescription());
            problemData.put("difficulty", problem.getDifficulty());
            problemData.put("timeLimit", problem.getTimeLimit());
            problemData.put("points", problem.getPoints());
            problemData.put("type", problem.getType());
            problemData.put("createdAt", problem.getCreatedAt());
            
            if ("QUIZ".equals(problem.getType())) {
                problemData.put("optionA", problem.getOptionA());
                problemData.put("optionB", problem.getOptionB());
                problemData.put("optionC", problem.getOptionC());
                problemData.put("optionD", problem.getOptionD());
            }
            
            Optional<Submission> submissionOpt = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
            if (submissionOpt.isPresent()) {
                Submission submission = submissionOpt.get();
                problemData.put("submitted", true);
                problemData.put("submissionAnswer", submission.getAnswer());
                problemData.put("submissionScore", submission.getScore());
                problemData.put("submissionFeedback", submission.getFeedback());
                problemData.put("submissionStatus", submission.getStatus());
                problemData.put("submittedAt", submission.getSubmittedAt());
            } else {
                problemData.put("submitted", false);
            }
            
            return Map.of("success", true, "problem", problemData);
        } catch (Exception e) {
            return Map.of("success", false, "message", "문제 상세 정보를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/submit-answer")
    public Map<String, Object> submitAnswer(
        @RequestParam("problemId") Long problemId,
        @RequestParam("answer") String answer,
        @RequestParam(value = "timeSpent", required = false) Integer timeSpent,
        @RequestParam(value = "autoSubmit", required = false) Boolean autoSubmit,
        @RequestParam(value = "timeLimitExceeded", required = false) Boolean timeLimitExceeded,
        HttpSession session) {

    try {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            return Map.of("success", false, "message", "인증이 필요합니다.");
        }
        
        if (submissionRepository.existsByUserIdAndProblemId(userId, problemId)) {
            return Map.of("success", false, "message", "이미 제출한 문제입니다.");
        }
        
        Problem problem = problemRepository.findById(problemId).orElse(null);
        if (problem == null) {
            return Map.of("success", false, "message", "문제를 찾을 수 없습니다.");
        }
        
        if (Boolean.TRUE.equals(timeLimitExceeded)) {
            return Map.of("success", false, "message", "시간이 초과되었습니다. 더 이상 제출할 수 없습니다.");
        }
        
        if (answer == null || answer.trim().isEmpty()) {
            if (Boolean.TRUE.equals(autoSubmit)) {
                answer = "시간 초과로 빈 답안 자동 제출";
            } else {
                return Map.of("success", false, "message", "답안을 작성해주세요.");
            }
        }
        
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setAnswer(answer);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("PENDING");
        
        if (timeSpent != null) {
            submission.setTimeSpent(timeSpent);
            
            if (problem.getTimeLimit() != null && timeSpent > (problem.getTimeLimit() * 60)) {
                submission.setAutoSubmitted(true);
                submission.setFeedback("시간 초과 후 제출됨");
            }
        }
        
        if (Boolean.TRUE.equals(autoSubmit)) {
            submission.setAutoSubmitted(true);
            if (submission.getFeedback() == null) {
                submission.setFeedback("시간 초과로 자동 제출됨");
            }
        }
        
        if ("QUIZ".equals(problem.getType()) && problem.getCorrectAnswer() != null) {
            boolean isCorrect = problem.getCorrectAnswer().equalsIgnoreCase(answer.trim());
            int baseScore = isCorrect ? problem.getPoints() : 0;
            
            if (Boolean.TRUE.equals(autoSubmit) && isCorrect) {
                baseScore = (int) (baseScore * 0.7);
                submission.setFeedback("시간 초과 후 제출 - 정답이지만 70% 점수");
            } else if (Boolean.TRUE.equals(autoSubmit)) {
                submission.setFeedback("시간 초과 후 제출 - 오답");
            } else {
                submission.setFeedback(isCorrect ? "정답입니다!" : "오답입니다. 정답: " + problem.getCorrectAnswer());
            }
            
            submission.setScore(baseScore);
            submission.setStatus("GRADED");
            submission.setGradedAt(LocalDateTime.now());
        }
        
        submissionRepository.save(submission);
        
        String message = Boolean.TRUE.equals(autoSubmit) ? 
            "시간이 만료되어 자동으로 제출되었습니다." : 
            "답안이 제출되었습니다.";
            
        return Map.of("success", true, "message", message);
    } catch (Exception e) {
        return Map.of("success", false, "message", "제출 실패: " + e.getMessage());
    }
}

    @PostMapping("/check-time-limit")
    public Map<String, Object> checkTimeLimit(
        @RequestParam("problemId") Long problemId,
        @RequestParam("startTime") Long startTime,
        HttpSession session) {
        
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }
            
            Problem problem = problemRepository.findById(problemId).orElse(null);
            if (problem == null) {
                return Map.of("success", false, "message", "문제를 찾을 수 없습니다.");
            }
            
            if (problem.getTimeLimit() == null) {
                return Map.of("success", true, "timeUp", false);
            }
            
            long currentTime = System.currentTimeMillis();
            long elapsedSeconds = (currentTime - startTime) / 1000;
            long timeLimitSeconds = problem.getTimeLimit() * 60;
            
            boolean timeUp = elapsedSeconds >= timeLimitSeconds;
            long remainingSeconds = Math.max(0, timeLimitSeconds - elapsedSeconds);
            
            return Map.of(
                "success", true,
                "timeUp", timeUp,
                "remainingSeconds", remainingSeconds,
                "elapsedSeconds", elapsedSeconds
            );
            
        } catch (Exception e) {
            return Map.of("success", false, "message", "시간 확인 실패: " + e.getMessage());
        }
    }

    @GetMapping("/notes")
    public List<StudyNote> getNotes(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return new ArrayList<>();
            }
            
            return studyNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/notes")
    public Map<String, Object> createNote(@RequestBody Map<String, String> noteData, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = new StudyNote();
            note.setUserId(userId);
            note.setTitle(noteData.get("title"));
            note.setContent(noteData.get("content"));
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            
            studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 저장되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PutMapping("/notes/{id}")
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> noteData, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = studyNoteRepository.findById(id).orElse(null);
            if (note == null || !note.getUserId().equals(userId)) {
                return Map.of("success", false, "message", "노트를 찾을 수 없습니다.");
            }
            
            note.setTitle(noteData.get("title"));
            note.setContent(noteData.get("content"));
            note.setUpdatedAt(LocalDateTime.now());
            
            studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 수정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = studyNoteRepository.findById(id).orElse(null);
            if (note == null || !note.getUserId().equals(userId)) {
                return Map.of("success", false, "message", "노트를 찾을 수 없습니다.");
            }
            
            studyNoteRepository.delete(note);
            
            return Map.of("success", true, "message", "노트가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/student/materials/{id}/preview")
    public String previewMaterial(@PathVariable Long id, Model model, HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        Material material = materialRepository.findById(id).orElse(null);
        
        if (material == null) {
            model.addAttribute("error", "자료를 찾을 수 없습니다.");
            return "student/preview-error";
        }
        
        model.addAttribute("material", material);
        
        String fileType = material.getFileType() != null ? material.getFileType().toLowerCase() : "";
        if (fileType.equals("pdf")) {
            return "student/preview-pdf";
        } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg").contains(fileType)) {
            return "student/preview-image";
        } else {
            return "student/preview-general";
        }
    }
}