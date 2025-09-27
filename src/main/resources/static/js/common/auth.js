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
    console.log('Login response:', data);

    if (!res.ok || !data.success) throw new Error(data.message || 'login failed');

    const role = data?.role ?? data?.userRole ?? data?.user?.role ?? null;
    const next = (typeof data?.next === 'string' && data.next.startsWith('/')) ? data.next : null;
    const byRole = { STUDENT: '/student/today', TEACHER: '/teacher/dashboard', ADMIN: '/admin/dashboard' };

    let dest = next || byRole[role] || '/';
    if (!dest.startsWith('/')) dest = '/' + dest;
    if (dest === '/undefined' || dest === '/null' || dest === '/NaN') dest = '/';

    console.log('Redirect to:', dest);
    window.location.assign(dest);
  }

  static async logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.assign('/');
  }
}
window.AuthManager = AuthManager;
