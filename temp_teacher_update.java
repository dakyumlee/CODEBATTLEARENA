// TeacherController.java에 추가할 내용

@Autowired
private WebSocketController webSocketController;

// 문제 출제 API에서 WebSocket 알림 전송
@PostMapping("/api/teacher/problems")
public ResponseEntity<?> createProblem(@RequestBody Map<String, Object> problemData) {
    try {
        // 문제 저장 로직 (기존 코드)
        
        // WebSocket으로 실시간 알림 전송
        String problemTitle = (String) problemData.get("title");
        webSocketController.sendProblemNotification(problemTitle, "문제");
        
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.status(500).body("문제 출제 실패");
    }
}
