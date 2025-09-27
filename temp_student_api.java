@GetMapping("/my-submissions")
public Map<String, Object> getMySubmissions(HttpServletRequest request) {
    try {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Map.of("error", "Unauthorized");
        }
        
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        return Map.of("submissions", submissions);
    } catch (Exception e) {
        return Map.of("error", "Failed to load submissions: " + e.getMessage());
    }
}
