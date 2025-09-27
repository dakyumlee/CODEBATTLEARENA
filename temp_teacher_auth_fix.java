@GetMapping("/materials")
public Map<String, Object> getMaterials(HttpServletRequest request) {
    try {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            return Map.of("success", false, "message", "인증이 필요합니다.", "redirect", "/");
        }

        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
            return Map.of("success", false, "message", "강사 권한이 필요합니다.", "redirect", "/");
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
            return data;
        }).collect(Collectors.toList());

        return Map.of("success", true, "materials", materialData);
    } catch (Exception e) {
        return Map.of("success", false, "message", "자료를 불러올 수 없습니다: " + e.getMessage());
    }
}
