class NotesManager {
    constructor() {
        this.notes = [];
        this.currentEditingId = null;
        this.init();
    }

    async init() {
        await this.loadNotes();
        this.setupEventListeners();
    }

    async loadNotes() {
        try {
            const response = await ApiClient.get('/api/student/notes');
            this.notes = response;
            this.renderNotes();
        } catch (error) {
            console.error('노트 로딩 오류:', error);
        }
    }

    renderNotes() {
        const container = document.getElementById('notesContainer');
        if (!container) return;

        if (this.notes.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; color: #64748b;">
                    <p>작성된 노트가 없습니다.</p>
                    <p>첫 번째 복습노트를 작성해보세요!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.notes.map(note => `
            <div class="note-card" data-id="${note.id}">
                <div class="note-header">
                    <h3>${note.title}</h3>
                    <div class="note-actions">
                        <button onclick="notesManager.editNote(${note.id})" class="btn-icon">수정</button>
                        <button onclick="notesManager.deleteNote(${note.id})" class="btn-icon">삭제</button>
                    </div>
                </div>
                <div class="note-content">${note.content}</div>
                <div class="note-date">${new Date(note.createdAt).toLocaleDateString()}</div>
            </div>
        `).join('');
    }

    setupEventListeners() {
        const saveBtn = document.getElementById('saveNoteBtn');
        const cancelBtn = document.getElementById('cancelBtn');
        const newNoteBtn = document.getElementById('newNoteBtn');

        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveNote());
        }

        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => this.cancelEdit());
        }

        if (newNoteBtn) {
            newNoteBtn.addEventListener('click', () => this.newNote());
        }
    }

    newNote() {
        this.currentEditingId = null;
        document.getElementById('noteTitle').value = '';
        document.getElementById('noteContent').value = '';
        document.getElementById('noteForm').style.display = 'block';
        document.getElementById('noteTitle').focus();
    }

    editNote(id) {
        const note = this.notes.find(n => n.id === id);
        if (!note) return;

        this.currentEditingId = id;
        document.getElementById('noteTitle').value = note.title;
        document.getElementById('noteContent').value = note.content;
        document.getElementById('noteForm').style.display = 'block';
        document.getElementById('noteTitle').focus();
    }

    async saveNote() {
        const title = document.getElementById('noteTitle').value.trim();
        const content = document.getElementById('noteContent').value.trim();

        if (!title || !content) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        try {
            if (this.currentEditingId) {
                await ApiClient.put(`/api/student/notes/${this.currentEditingId}`, { title, content });
            } else {
                await ApiClient.post('/api/student/notes', { title, content });
            }

            await this.loadNotes();
            this.cancelEdit();

            if (window.studentActivityTracker) {
                window.studentActivityTracker.updateActivity('노트 저장');
            }
        } catch (error) {
            console.error('노트 저장 오류:', error);
            alert('노트 저장에 실패했습니다.');
        }
    }

    async deleteNote(id) {
        if (!confirm('정말 이 노트를 삭제하시겠습니까?')) return;

        try {
            await ApiClient.delete(`/api/student/notes/${id}`);
            await this.loadNotes();
        } catch (error) {
            console.error('노트 삭제 오류:', error);
            alert('노트 삭제에 실패했습니다.');
        }
    }

    cancelEdit() {
        this.currentEditingId = null;
        document.getElementById('noteForm').style.display = 'none';
        document.getElementById('noteTitle').value = '';
        document.getElementById('noteContent').value = '';
    }
}

window.notesManager = new NotesManager();
