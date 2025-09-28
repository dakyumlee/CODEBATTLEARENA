package com.codebattlearena.controller;

import com.codebattlearena.model.Problem;
import com.codebattlearena.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemRepository problemRepository;

    private Long getUserIdFromSession(HttpSession session) {
        try {
            Object userId = session.getAttribute("userId");
            return userId != null ? (Long) userId : null;
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Problem> problems = problemRepository.findAll();
            return ResponseEntity.ok(Map.of("problems", problems));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "문제를 불러올 수 없습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProblem(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null) {
                return ResponseEntity.status(404).body(Map.of("error", "문제를 찾을 수 없습니다"));
            }

            return ResponseEntity.ok(Map.of("problem", problem));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "문제를 불러올 수 없습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayProblem(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Problem> problems = problemRepository.findAll();
            if (problems.isEmpty()) {
                return ResponseEntity.ok(Map.of("problem", null, "message", "등록된 문제가 없습니다"));
            }

            Problem todayProblem = problems.get(0);
            return ResponseEntity.ok(Map.of("problem", todayProblem));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "오늘의 문제를 불러올 수 없습니다: " + e.getMessage()));
        }
    }
}