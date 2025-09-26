class AuthManager {
    static setToken(token) {
        localStorage.setItem('authToken', token);
    }
    
    static getToken() {
        return localStorage.getItem('authToken');
    }
    
    static setUserRole(role) {
        localStorage.setItem('userRole', role);
    }
    
    static setUserInfo(userInfo) {
        localStorage.setItem('userId', userInfo.id);
        localStorage.setItem('userName', userInfo.name);
        localStorage.setItem('userEmail', userInfo.email);
    }
    
    static getCurrentUserRole() {
        return localStorage.getItem('userRole');
    }
    
    static getCurrentUserId() {
        return localStorage.getItem('userId');
    }
    
    static getCurrentUserName() {
        return localStorage.getItem('userName');
    }
    
    static getAuthHeaders() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }
    
    static logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userId');
        localStorage.removeItem('userName');
        localStorage.removeItem('userEmail');
        window.location.href = '/';
    }
    
    static isLoggedIn() {
        return !!this.getToken();
    }
}
