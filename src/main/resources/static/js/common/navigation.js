class NavigationManager {
    static init() {
        const userRole = AuthManager.getCurrentUserRole();
        this.updateNavigation(userRole);
    }

    static updateNavigation(role) {
        const navLinks = document.querySelector('.nav-links');
        if (!navLinks) return;

        let links = [];

        switch (role) {
            case 'STUDENT':
                links = [
                    { href: '/student', text: '대시보드' },
                    { href: '/student/today', text: '오늘의 학습' },
                    { href: '/student/ai-tutor', text: 'AI 튜터' },
                    { href: '/student/practice', text: '코딩연습' },
                    { href: '/student/notes', text: '복습노트' },
                    { href: '/student/battles', text: '코드배틀' }
                ];
                break;
                
            case 'TEACHER':
                links = [
                    { href: '/teacher', text: '강사 대시보드' },
                    { href: '/teacher/class', text: '수업관리' },
                    { href: '/teacher/grades', text: '성적관리' },
                    { href: '/student', text: '학생 체험', style: 'background: rgba(59, 130, 246, 0.1); color: #3b82f6;' }
                ];
                break;
                
            case 'ADMIN':
                links = [
                    { href: '/admin', text: '관리자' },
                    { href: '/teacher', text: '강사 체험', style: 'background: rgba(16, 185, 129, 0.1); color: #10b981;' },
                    { href: '/student', text: '학생 체험', style: 'background: rgba(59, 130, 246, 0.1); color: #3b82f6;' }
                ];
                break;
        }

        links.push({ href: '#', text: '로그아웃', onclick: 'AuthManager.logout()' });

        navLinks.innerHTML = links.map(link => 
            `<li><a href="${link.href}" ${link.onclick ? `onclick="${link.onclick}"` : ''} ${link.style ? `style="${link.style}"` : ''}>${link.text}</a></li>`
        ).join('');
    }
}

AuthManager.getCurrentUserRole = function() {
    const token = this.getToken();
    if (!token) return null;
    
    try {
        return localStorage.getItem('userRole');
    } catch {
        return null;
    }
};
