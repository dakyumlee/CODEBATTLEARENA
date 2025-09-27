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
            
            if (data.success) {
                // 임시로 모든 사용자를 student로 처리
                console.log('로그인 성공 - student 페이지로 이동');
                window.location.href = '/student/today';
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