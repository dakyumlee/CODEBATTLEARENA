class AuthManager {
    static login(email, password) {
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userRole', data.user.role);
                localStorage.setItem('userName', data.user.name);
                window.location.href = data.redirectUrl;
                return data;
            }
            throw new Error(data.message);
        });
    }

    static logout() {
        localStorage.clear();
        window.location.href = '/';
    }

    static getAuthToken() {
        return localStorage.getItem('authToken');
    }

    static getUserRole() {
        return localStorage.getItem('userRole');
    }

    static getUserName() {
        return localStorage.getItem('userName');
    }

    static isAuthenticated() {
        return !!this.getAuthToken();
    }

    static getAuthHeaders() {
        const token = this.getAuthToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    static redirectByRole() {
        const role = this.getUserRole();
        switch(role) {
            case 'STUDENT': window.location.href = '/student'; break;
            case 'TEACHER': window.location.href = '/teacher'; break;
            case 'ADMIN': window.location.href = '/admin'; break;
            default: window.location.href = '/'; break;
        }
    }
}

window.AuthManager = AuthManager;
