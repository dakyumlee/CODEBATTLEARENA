package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @GetMapping("")
    public ResponseEntity<?> getProblems() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProblem(@PathVariable Long id) {
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", id);
        problem.put("title", "문제 제목");
        problem.put("description", "문제 설명");
        problem.put("difficulty", "중");
        problem.put("points", 10);
        return ResponseEntity.ok(problem);
    }
}
