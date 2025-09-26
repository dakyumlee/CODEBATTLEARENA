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
                    { href: '/student/battle', text: '코드배틀' }
                ];
                break;
                
            case 'TEACHER':
                links = [
                    { href: '/teacher', text: '대시보드' },
                    { href: '/teacher/class', text: '수업관리' },
                    { href: '/teacher/grades', text: '성적관리' },
                    { href: '/student', text: '학생 체험' }
                ];
                break;
                
            case 'ADMIN':
                links = [
                    { href: '/admin', text: '관리자' },
                    { href: '/teacher', text: '강사 체험' },
                    { href: '/student', text: '학생 체험' }
                ];
                break;
        }

        // 로그아웃 링크 추가
        links.push({ href: '#', text: '로그아웃', onclick: 'AuthManager.logout()' });

        navLinks.innerHTML = links.map(link => 
            `<li><a href="${link.href}" ${link.onclick ? `onclick="${link.onclick}"` : ''}>${link.text}</a></li>`
        ).join('');
    }
}

// AuthManager에 역할 확인 메소드 추가
AuthManager.getCurrentUserRole = function() {
    const token = this.getToken();
    if (!token) return null;
    
    try {
        // JWT 토큰을 파싱해서 역할 정보를 가져와야 하지만,
        // 현재는 localStorage에서 역할 정보를 별도로 저장
        return localStorage.getItem('userRole');
    } catch {
        return null;
    }
};
