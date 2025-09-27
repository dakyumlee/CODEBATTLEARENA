class AuthManager {
    static login(email, password) {
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        })
        .then(response => response.json())
        .then(data => {
            console.log('전체 응답:', JSON.stringify(data, null, 2));
            console.log('data.user:', data.user);
            console.log('data.user.role:', data.user ? data.user.role : 'user 객체 없음');
            
            if (data.success) {
                const role = data.user.role;
                console.log('역할:', role);
                
                if (role === 'STUDENT') {
                    window.location.href = '/student/today';
                } else if (role === 'TEACHER') {
                    window.location.href = '/teacher/dashboard';
                } else if (role === 'ADMIN') {
                    window.location.href = '/admin/dashboard';
                } else {
                    console.error('알 수 없는 역할:', role);
                    window.location.href = '/student/today';
                }
                return data;
            }
            throw new Error(data.message);
        })
        .catch(error => {
            console.error('Login error:', error);
            alert('로그인 실패: ' + error.message);
        });
    }

    static logout() {
        fetch('/api/auth/logout', {
            method: 'POST'
        }).then(() => {
            window.location.href = '/';
        });
    }
}

window.AuthManager = AuthManager;