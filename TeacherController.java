
    @GetMapping("/materials/{id}/preview")
    public String previewMaterial(@PathVariable Long id, Model model, HttpSession session) {
        Long teacherId = getUserIdFromSession(session);
        if (teacherId == null) {
            return "redirect:/";
        }
        
        Material material = materialRepository.findById(id).orElse(null);
        if (material == null || !material.getTeacherId().equals(teacherId)) {
            model.addAttribute("error", "자료를 찾을 수 없습니다.");
            return "teacher/preview-error";
        }
        
        model.addAttribute("material", material);
        
        String fileType = material.getFileType() != null ? material.getFileType().toLowerCase() : "";
        if (fileType.equals("pdf")) {
            return "teacher/preview-pdf";
        } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg").contains(fileType)) {
            return "teacher/preview-image";
        } else {
            return "teacher/preview-general";
        }
    }

    @GetMapping("/materials/{id}/download-fixed")
    public ResponseEntity<String> downloadMaterialFixed(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증이 필요합니다.");
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
                return ResponseEntity.notFound().build();
            }

            material.setDownloadCount(material.getDownloadCount() + 1);
            materialRepository.save(material);

            String downloadUrl = material.getFilePath();
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 경로가 설정되지 않았습니다.");
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", downloadUrl)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("다운로드 실패: " + e.getMessage());
        }
    }
