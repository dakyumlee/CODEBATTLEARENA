@PostMapping("/materials")
public Map<String, Object> uploadMaterial(
        @RequestParam("title") String title,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {

    try {
        System.out.println("=== 업로드 시작 ===");
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

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!isAllowedFileType(fileExtension)) {
            System.out.println("허용되지 않는 파일 타입: " + fileExtension);
            return Map.of("success", false, "message", "지원하지 않는 파일 형식입니다.");
        }

        System.out.println("Cloudinary 업로드 시도...");
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                Map.of("folder", "codebattlearena/materials",
                        "public_id", System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_"),
                        "resource_type", "auto"));

        String cloudinaryUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        System.out.println("Cloudinary URL: " + cloudinaryUrl);

        Material material = new Material();
        material.setTeacherId(teacherId);
        material.setTitle(title);
        material.setDescription(description);
        material.setFilePath(cloudinaryUrl);
        material.setFileType(fileExtension);
        material.setFileSize(file.getSize());
        material.setOriginalFilename(originalFilename);
        material.setCloudinaryPublicId(publicId);
        material.setCreatedAt(LocalDateTime.now());

        materialRepository.save(material);

        System.out.println("업로드 성공");
        return Map.of("success", true, "message", "자료가 업로드되었습니다.");

    } catch (Exception e) {
        System.err.println("Upload error: " + e.getMessage());
        e.printStackTrace();
        return Map.of("success", false, "message", "업로드 실패: " + e.getMessage());
    }
}
