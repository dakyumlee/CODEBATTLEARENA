@PostMapping("/submit-answer")
public Map<String, Object> submitAnswer(
        @RequestParam("problemId") Long problemId,
        @RequestParam(value = "answer", required = false) String answer,
        @RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {

    try {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Map.of("success", false, "message", "인증이 필요합니다.");
        }

        // 텍스트 답안과 파일 중 하나는 반드시 있어야 함
        if ((answer == null || answer.trim().isEmpty()) && (file == null || file.isEmpty())) {
            return Map.of("success", false, "message", "답안을 작성하거나 파일을 첨부해주세요.");
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setAnswer(answer != null ? answer : "파일 제출");
        submission.setStatus("PENDING");
        submission.setSubmittedAt(LocalDateTime.now());

        // 파일이 있으면 Cloudinary에 업로드
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String fileExtension = getFileExtension(originalFilename);
                
                // 허용된 파일 타입 체크
                if (!isAllowedSubmissionFileType(fileExtension)) {
                    return Map.of("success", false, "message", "지원하지 않는 파일 형식입니다. (PDF, DOC, DOCX, HWP, TXT, ZIP, 이미지, 소스코드 파일만 가능)");
                }

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        Map.of("folder", "codebattlearena/submissions",
                                "public_id", System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_"),
                                "resource_type", "auto"));

                String cloudinaryUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                submission.setFilePath(cloudinaryUrl);
                submission.setFileName(originalFilename);
                submission.setFileSize(file.getSize());
                submission.setFileType(fileExtension);
                submission.setCloudinaryPublicId(publicId);

            } catch (Exception e) {
                return Map.of("success", false, "message", "파일 업로드 실패: " + e.getMessage());
            }
        }

        submissionRepository.save(submission);

        return Map.of("success", true, "message", "답안이 제출되었습니다.");

    } catch (Exception e) {
        return Map.of("success", false, "message", "제출 실패: " + e.getMessage());
    }
}

private boolean isAllowedSubmissionFileType(String extension) {
    Set<String> allowedTypes = Set.of(
            "pdf", "doc", "docx", "hwp", "txt", "rtf",
            "jpg", "jpeg", "png", "gif", "bmp",
            "zip", "rar", "7z",
            "java", "js", "html", "css", "json", "xml",
            "py", "cpp", "c", "h", "cs", "php", "rb", "go");
    return allowedTypes.contains(extension);
}
