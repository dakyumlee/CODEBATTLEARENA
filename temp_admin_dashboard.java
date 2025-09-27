@GetMapping("/statistics")
public Map<String, Object> getSystemStatistics(HttpServletRequest request) {
    try {
        Long adminId = getUserIdFromRequest(request);
        if (adminId == null) {
            return Map.of("error", "Unauthorized");
        }
        
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return Map.of("error", "Admin access required");
        }
        
        long totalUsers = userRepository.count();
        long studentCount = userRepository.countByRole(UserRole.STUDENT);
        long teacherCount = userRepository.countByRole(UserRole.TEACHER);
        long onlineUsers = userRepository.countByOnlineStatusTrue();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("onlineUsers", onlineUsers);
        stats.put("totalGroups", groupRepository.count());
        stats.put("totalProblems", problemRepository.count());
        stats.put("totalMaterials", materialRepository.count());
        
        return Map.of("statistics", stats);
    } catch (Exception e) {
        return Map.of("error", "Failed to load statistics: " + e.getMessage());
    }
}
