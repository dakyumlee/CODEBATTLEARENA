    @Autowired
    private ProblemRepository problemRepository;

    @GetMapping("/teacher-problems")
    public List<Map<String, Object>> getTeacherProblems(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<Problem> problems = problemRepository.findAllTeacherProblems();
            List<Map<String, Object>> problemList = new ArrayList<>();
            
            for (Problem problem : problems) {
                Map<String, Object> problemMap = new HashMap<>();
                problemMap.put("id", problem.getId());
                problemMap.put("title", problem.getTitle());
                problemMap.put("description", problem.getDescription());
                problemMap.put("difficulty", problem.getDifficulty());
                problemMap.put("timeLimit", problem.getTimeLimit());
                problemMap.put("type", problem.getType());
                problemMap.put("createdAt", problem.getCreatedAt().toString());
                problemList.add(problemMap);
            }
            
            return problemList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
