@Controller
public class PageController {
    @GetMapping("/")
    public String index() { return "index"; }
    
    @GetMapping("/student/today")
    public String studentToday() { return "student/today"; }
    
    @GetMapping("/teacher/dashboard") 
    public String teacherDashboard() { return "teacher/dashboard"; }
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard() { return "admin/dashboard"; }
}