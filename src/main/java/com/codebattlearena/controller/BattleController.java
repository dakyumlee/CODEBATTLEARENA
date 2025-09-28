package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/student/battle")
public class BattleController {

    @GetMapping("")
    public String battleDashboard(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle";
    }

    @GetMapping("/create")
    public String createBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-create";
    }

    @GetMapping("/join")
    public String joinBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-join";
    }

    @GetMapping("/random")
    public String randomBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-random";
    }

    @GetMapping("/ai")
    public String aiBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-ai";
    }
}

@RestController
@RequestMapping("/api/battle")
class BattleApiController {

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

    @GetMapping("/stats")
    public ResponseEntity<?> getBattleStats(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBattles", 0);
            stats.put("wins", 0);
            stats.put("losses", 0);
            stats.put("draws", 0);
            stats.put("winRate", 0);
            stats.put("recentBattles", new String[]{});
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "통계를 불러올 수 없습니다."));
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
}