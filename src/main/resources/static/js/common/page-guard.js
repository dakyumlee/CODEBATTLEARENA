class PageGuard {
    static checkAccess(requiredRoles, currentPath) {
        if (!AuthManager.isLoggedIn()) {
            window.location.href = '/';
            return false;
        }

        const userRole = AuthManager.getCurrentUserRole();
        if (!userRole) {
            window.location.href = '/';
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
                window.location.href = '/student';
                break;
            case 'TEACHER':
                window.location.href = '/teacher';
                break;
            case 'ADMIN':
                window.location.href = '/admin';
                break;
            default:
                window.location.href = '/';
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
