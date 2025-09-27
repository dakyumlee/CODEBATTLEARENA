class AuthManager {
    static async login(email, password) {
        const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
        });
    const ct = res.headers.get('content-type') || '';
    if (!ct.includes('application/json')) throw new Error('invalid response');
    const data = await res.json();
    if (!res.ok || !data.success) throw new Error(data.message || 'login failed');
    const next = typeof data.next === 'string' ? data.next : null;
    const role = data.role || data.userRole || data.user?.role;
    const fallback = { STUDENT: '/student/today', TEACHER: '/teacher/dashboard', ADMIN: '/admin/dashboard' };
    window.location.href = next && next.startsWith('/') ? next : (fallback[role] || '/');
    return data;
}

    static async logout() {
        await fetch('/api/auth/logout', { method: 'POST' });
        window.location.href = '/';
    }
}

window.AuthManager = AuthManager;
