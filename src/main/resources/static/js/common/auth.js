class AuthManager {
    static getToken() {
        return localStorage.getItem('authToken');
    }
    
    static setToken(token) {
        localStorage.setItem('authToken', token);
    }
    
    static removeToken() {
        localStorage.removeItem('authToken');
    }
    
    static getAuthHeaders() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }
    
    static async isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;
        
        try {
            const response = await fetch('/api/auth/validate', {
                headers: this.getAuthHeaders()
            });
            return response.ok;
        } catch {
            return false;
        }
    }
    
    static logout() {
        this.removeToken();
        window.location.href = '/';
    }
}
