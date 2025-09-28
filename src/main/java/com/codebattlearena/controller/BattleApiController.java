@RestController
@RequestMapping("/api/battle")
class BattleApiController {

    @Autowired
    private StudyNoteRepository studyNoteRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createBattleRoom(@RequestBody BattleRoomRequest request) {
        try {
            String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inviteCode", inviteCode);
            response.put("roomName", request.getRoomName());
            response.put("difficulty", request.getDifficulty());
            response.put("timeLimit", request.getTimeLimit());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "배틀방 생성에 실패했습니다."));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinBattleRoom(@RequestBody JoinRoomRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "배틀방에 참여했습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "배틀방 참여에 실패했습니다."));
        }
    }

    @PostMapping("/ai-battle/result")
    public ResponseEntity<?> saveAiBattleResult(@RequestBody AiBattleResult result, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            saveBattleRecord(userId, "AI", result.getResult(), result.getScore(), result.getProblemsolved());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "AI 배틀 결과가 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "배틀 결과 저장에 실패했습니다."));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getBattleStats(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            List<StudyNote> battleRecords = studyNoteRepository.findByUserIdAndTitleContaining(userId, "배틀 기록");
            
            int totalBattles = battleRecords.size();
            int wins = 0;
            int losses = 0;
            List<Map<String, String>> recentBattles = new ArrayList<>();
            
            for (StudyNote record : battleRecords) {
                String content = record.getContent();
                if (content.contains("승리")) wins++;
                if (content.contains("패배")) losses++;
                
                Map<String, String> battleInfo = new HashMap<>();
                battleInfo.put("opponent", content.contains("AI") ? "AI" : "상대방");
                battleInfo.put("result", content.contains("승리") ? "WIN" : "LOSS");
                battleInfo.put("date", record.getCreatedAt().toLocalDate().toString());
                recentBattles.add(battleInfo);
            }
            
            Collections.reverse(recentBattles);
            if (recentBattles.size() > 5) {
                recentBattles = recentBattles.subList(0, 5);
            }
            
            int winRate = totalBattles > 0 ? (wins * 100 / totalBattles) : 0;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBattles", totalBattles);
            stats.put("wins", wins);
            stats.put("losses", losses);
            stats.put("draws", 0);
            stats.put("winRate", winRate);
            stats.put("recentBattles", recentBattles);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("배틀 통계 로드 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "통계를 불러올 수 없습니다."));
        }
    }

    private void saveBattleRecord(Long userId, String opponent, String result, int score, int problemsSolved) {
        try {
            StudyNote battleRecord = new StudyNote();
            battleRecord.setUserId(userId);
            battleRecord.setTitle("배틀 기록 - " + LocalDateTime.now().toLocalDate());
            
            String content = String.format("""
                상대: %s
                결과: %s
                점수: %d점
                해결한 문제: %d개
                날짜: %s
                """, opponent, result, score, problemsSolved, LocalDateTime.now());
                
            battleRecord.setContent(content);
            battleRecord.setCreatedAt(LocalDateTime.now());
            battleRecord.setUpdatedAt(LocalDateTime.now());
            
            studyNoteRepository.save(battleRecord);
        } catch (Exception e) {
            System.err.println("배틀 기록 저장 실패: " + e.getMessage());
        }
    }

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

    public static class BattleRoomRequest {
        private String roomName;
        private String difficulty;
        private int timeLimit;

        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getTimeLimit() { return timeLimit; }
        public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    }

    public static class JoinRoomRequest {
        private String inviteCode;

        public String getInviteCode() { return inviteCode; }
        public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    }

    public static class AiBattleResult {
        private String result;
        private int score;
        private int problemsSolved;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getProblemsolved() { return problemsSolved; }
        public void setProblemsSolved(int problemsSolved) { this.problemsSolved = problemsSolved; }
    }
}