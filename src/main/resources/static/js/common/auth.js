class AuthManager {
    static login(email, password) {
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        })
        .then(response => response.json())
        .then(data => {
            console.log('Full login response:', data);
            
            if (data.success) {
                const role = data.user.role;
                if (role === 'STUDENT') {
                    window.location.href = '/student/today';
                } else if (role === 'TEACHER') {
                    window.location.href = '/teacher/dashboard';
                } else if (role === 'ADMIN') {
                    window.location.href = '/admin/dashboard';
                } else {
                    window.location.href = '/';
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