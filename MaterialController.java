package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.Material;
import com.codebattlearena.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                if (request.getCookies() != null) {
                    for (var cookie : request.getCookies()) {
                        if ("authToken".equals(cookie.getName())) {
                            authHeader = "Bearer " + cookie.getValue();
                            break;
                        }
                    }
                }
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.isTokenExpired(token)) {
                    return null;
                }
                return 1L; // 임시로 1L 반환 (실제로는 토큰에서 사용자 ID 추출)
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Map<String, Object>> previewMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "인증이 필요합니다."));
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            String fileType = material.getFileType().toLowerCase();
            boolean previewable = fileType.equals("pdf") || 
                                fileType.equals("txt") || 
                                fileType.equals("md") ||
                                isImageFile(fileType) ||
                                isCodeFile(fileType);

            return ResponseEntity.ok(Map.of(
                "id", material.getId(),
                "title", material.getTitle(),
                "fileUrl", material.getFilePath(),
                "fileType", material.getFileType(),
                "fileSize", material.getFileSize(),
                "previewable", previewable,
                "originalFilename", material.getOriginalFilename()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "미리보기 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> downloadMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            material.setDownloadCount(material.getDownloadCount() + 1);
            materialRepository.save(material);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", material.getFilePath())
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isImageFile(String fileType) {
        return fileType.equals("jpg") || fileType.equals("jpeg") || 
               fileType.equals("png") || fileType.equals("gif") || 
               fileType.equals("bmp") || fileType.equals("svg");
    }

    private boolean isCodeFile(String fileType) {
        return fileType.equals("java") || fileType.equals("js") || 
               fileType.equals("html") || fileType.equals("css") || 
               fileType.equals("py") || fileType.equals("cpp") || 
               fileType.equals("c") || fileType.equals("json");
    }
}
