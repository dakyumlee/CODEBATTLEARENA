package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @GetMapping("")
    public ResponseEntity<?> getProblems() {
        List<Map<String, Object>> problems = new ArrayList<>();
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProblem(@PathVariable Long id) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", id);
        problem.put("title", "샘플 문제");
        problem.put("description", "이것은 샘플 문제입니다.");
        problem.put("difficulty", "하");
        problem.put("points", 10);
        return ResponseEntity.ok(problem);
    }
}
