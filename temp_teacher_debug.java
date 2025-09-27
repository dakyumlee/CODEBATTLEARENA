@PostMapping("/materials")
public Map<String, Object> uploadMaterial(
        @RequestParam("title") String title,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {

    try {
        System.out.println("=== 파일 업로드 시작 ===");
        System.out.println("Title: " + title);
        System.out.println("File: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());

        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            System.out.println("인증 실패");
            return Map.of("success", false, "message", "인증이 필요합니다.");
        }

        if (file.isEmpty()) {
            System.out.println("파일이 비어있음");
            return Map.of("success", false, "message", "파일을 선택해주세요.");
        }

        // 일단 로컬 저장으로 테스트
        String uploadDir = "uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;
        
        file.transferTo(new File(filePath));

        Material material = new Material();
        material.setTeacherId(teacherId);
        material.setTitle(title);
        material.setDescription(description);
        material.setFilePath("/uploads/" + fileName);
        material.setFileType(getFileExtension(file.getOriginalFilename()));
        material.setFileSize(file.getSize());
        material.setOriginalFilename(file.getOriginalFilename());
        material.setCreatedAt(LocalDateTime.now());

        materialRepository.save(material);

        System.out.println("업로드 성공: " + filePath);
        return Map.of("success", true, "message", "자료가 업로드되었습니다.");

    } catch (Exception e) {
        System.err.println("Upload error: " + e.getMessage());
        e.printStackTrace();
        return Map.of("success", false, "message", "업로드 실패: " + e.getMessage());
    }
}
