@GetMapping("/materials")
public ResponseEntity<Map<String, Object>> getMaterials(HttpServletRequest request) {
    try {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Material> materials = materialRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        List<Map<String, Object>> materialData = materials.stream().map(material -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", material.getId());
            data.put("title", material.getTitle());
            data.put("description", material.getDescription());
            data.put("fileSize", material.getFileSize());
            data.put("fileType", material.getFileType());
            data.put("originalFilename", material.getOriginalFilename());
            data.put("downloadCount", material.getDownloadCount());
            data.put("createdAt", material.getCreatedAt());
            data.put("filePath", material.getFilePath()); // 이 부분이 누락되어 있었음
            return data;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("materials", materialData));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", "자료를 불러올 수 없습니다: " + e.getMessage()));
    }
}
