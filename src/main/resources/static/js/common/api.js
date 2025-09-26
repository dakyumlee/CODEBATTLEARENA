class API {
    static getHeaders() {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        };
    }

    static async get(url) {
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: this.getHeaders()
            });
            return await response.json();
        } catch (error) {
            console.error('API GET Error:', error);
            throw error;
        }
    }

    static async post(url, data = {}) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('API POST Error:', error);
            throw error;
        }
    }

    static async put(url, data = {}) {
        try {
            const response = await fetch(url, {
                method: 'PUT',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('API PUT Error:', error);
            throw error;
        }
    }

    static async delete(url) {
        try {
            const response = await fetch(url, {
                method: 'DELETE',
                headers: this.getHeaders()
            });
            return await response.json();
        } catch (error) {
            console.error('API DELETE Error:', error);
            throw error;
        }
    }

    static async student() {
        return {
            getToday: () => this.get('/api/student/today'),
            getStats: () => this.get('/api/student/stats'),
            getBattleStats: () => this.get('/api/student/battle-stats'),
            getPracticeStats: () => this.get('/api/student/practice-stats'),
            getNotes: () => this.get('/api/student/notes'),
            createNote: (data) => this.post('/api/student/notes', data),
            updateNote: (id, data) => this.put(`/api/student/notes/${id}`, data),
            deleteNote: (id) => this.delete(`/api/student/notes/${id}`)
        };
    }

    static async teacher() {
        return {
            getStudents: () => this.get('/api/teacher/students'),
            getStats: () => this.get('/api/teacher/stats')
        };
    }

    static async admin() {
        return {
            getUsers: () => this.get('/api/admin/users'),
            deleteUser: (id) => this.delete(`/api/admin/users/${id}`),
            updateRole: (id, role) => this.put(`/api/admin/users/${id}/role`, { role })
        };
    }
}

window.API = API;
