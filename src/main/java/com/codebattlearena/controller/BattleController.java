package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

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
