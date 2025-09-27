(function () {
    const origFetch = window.fetch;
    window.fetch = async function(input, init) {
        const res = await origFetch(input, init);
        try {
        const url = typeof input === 'string' ? input : input?.url || '';
        if (url.includes('/api/auth/login')) {
            const clone = res.clone();
            const ct = clone.headers.get('content-type') || '';
            if (ct.includes('application/json')) {
            const data = await clone.json();
            if (data && !data.user && (data.role || data.userRole)) {
                data.user = {
                id: data.id ?? null,
                name: data.name ?? null,
                role: data.role ?? data.userRole ?? null
                };
            }
            const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
            return new Response(blob, { status: res.status, statusText: res.statusText, headers: res.headers });
            }
        }
        } catch (_) {}
        return res;
    };
})();