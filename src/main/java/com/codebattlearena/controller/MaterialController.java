package com.codebattlearena.controller;

import com.codebattlearena.model.Material;
import com.codebattlearena.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/materials")
    public ResponseEntity<?> getMaterials() {
        try {
            List<Material> materials = materialRepository.findAllByOrderByCreatedAtDesc();
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "자료를 불러올 수 없습니다."));
        }
    }

    @PostMapping("/materials/upload")
    public ResponseEntity<?> uploadMaterials(@RequestParam("files") MultipartFile[] files,
                                           @RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "description", required = false) String description) {
        try {
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 파일명 생성
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = uploadDir.resolve(fileName);
                    
                    // 파일 저장
                    Files.copy(file.getInputStream(), filePath);
                    
                    // DB에 저장
                    Material material = new Material();
                    material.setTitle(title != null ? title : file.getOriginalFilename());
                    material.setDescription(description);
                    material.setFilePath(filePath.toString());
                    material.setFileType(file.getContentType());
                    material.setFileSize(file.getSize());
                    material.setCreatedAt(LocalDateTime.now());
                    
                    materialRepository.save(material);
                    
                    // 학생들에게 실시간 알림
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "new_material");
                    notification.put("message", "새로운 자료가 공유되었습니다: " + material.getTitle());
                    notification.put("material", material);
                    notification.put("timestamp", LocalDateTime.now().toString());
                    
                    messagingTemplate.convertAndSend("/topic/notifications", notification);
                }
            }
            
            return ResponseEntity.ok(Map.of("message", "파일이 업로드되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "파일 업로드에 실패했습니다."));
        }
    }

    @PostMapping("/materials/link")
    public ResponseEntity<?> shareLink(@RequestBody LinkRequest request) {
        try {
            Material material = new Material();
            material.setTitle(request.getTitle());
            material.setDescription(request.getDescription());
            material.setUrl(request.getUrl());
            material.setFileType("link");
            material.setCreatedAt(LocalDateTime.now());
            
            Material savedMaterial = materialRepository.save(material);
            
            // 학생들에게 실시간 알림
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_material");
            notification.put("message", "새로운 링크가 공유되었습니다: " + material.getTitle());
            notification.put("material", savedMaterial);
            notification.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            return ResponseEntity.ok(Map.of("message", "링크가 공유되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "링크 공유에 실패했습니다."));
        }
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        try {
            materialRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "자료가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "자료 삭제에 실패했습니다."));
        }
    }

    public static class LinkRequest {
        private String url;
        private String title;
        private String description;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
