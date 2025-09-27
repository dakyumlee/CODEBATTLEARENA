class PageGuard {
    static checkAccess(requiredRoles, currentPath) {
        if (!AuthManager.isLoggedIn()) {
            location.replace('/');
            return false;
        }
        const userRole = AuthManager.getCurrentUserRole();
        if (!userRole) {
            location.replace('/');
            return false;
        }
        if (Array.isArray(requiredRoles)) {
            if (!requiredRoles.includes(userRole)) {
                this.redirectToDefaultPage(userRole);
                return false;
            }
        }
        return true;
    }

    static redirectToDefaultPage(userRole) {
        switch (userRole) {
            case 'STUDENT':
                location.replace('/student/today');
                break;
            case 'TEACHER':
                location.replace('/teacher/dashboard');
                break;
            case 'ADMIN':
                location.replace('/admin/dashboard');
                break;
            default:
                location.replace('/');
        }
    }

    static initStudentPage() {
        return this.checkAccess(['STUDENT', 'TEACHER', 'ADMIN'], window.location.pathname);
    }

    static initTeacherPage() {
        return this.checkAccess(['TEACHER', 'ADMIN'], window.location.pathname);
    }

    static initAdminPage() {
        return this.checkAccess(['ADMIN'], window.location.pathname);
    }
}
