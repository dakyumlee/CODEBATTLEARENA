document.addEventListener('DOMContentLoaded', function() {
    loadNotes();
    
    document.getElementById('addNoteBtn')?.addEventListener('click', showAddNoteModal);
    document.getElementById('saveNoteBtn')?.addEventListener('click', saveNote);
    document.getElementById('cancelNoteBtn')?.addEventListener('click', hideNoteModal);
});

async function loadNotes() {
    try {
        const response = await fetch('/api/student/notes');
        const notes = await response.json();
        displayNotes(notes);
    } catch (error) {
        console.error('노트 로드 실패:', error);
    }
}

function displayNotes(notes) {
    const notesList = document.getElementById('notesList');
    if (!notesList) return;
    
    if (notes.length === 0) {
        notesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📝</div>
                <p>아직 작성된 노트가 없습니다.</p>
                <button class="btn-primary" onclick="showAddNoteModal()">첫 노트 작성하기</button>
            </div>
        `;
        return;
    }
    
    notesList.innerHTML = notes.map(note => `
        <div class="note-card" data-id="${note.id}">
            <div class="note-header">
                <h3 class="note-title">${note.title}</h3>
                <div class="note-actions">
                    <button class="btn-edit" onclick="editNote(${note.id})">수정</button>
                    <button class="btn-delete" onclick="deleteNote(${note.id})">삭제</button>
                </div>
            </div>
            <div class="note-content">${note.content.substring(0, 150)}${note.content.length > 150 ? '...' : ''}</div>
            <div class="note-meta">
                <span>작성: ${new Date(note.createdAt).toLocaleString()}</span>
                ${note.updatedAt !== note.createdAt ? `<span>수정: ${new Date(note.updatedAt).toLocaleString()}</span>` : ''}
            </div>
        </div>
    `).join('');
}

function showAddNoteModal() {
    document.getElementById('noteModalTitle').textContent = '새 노트 작성';
    document.getElementById('noteTitle').value = '';
    document.getElementById('noteContent').value = '';
    document.getElementById('noteModal').style.display = 'block';
    document.getElementById('saveNoteBtn').dataset.mode = 'create';
}

function hideNoteModal() {
    document.getElementById('noteModal').style.display = 'none';
}

async function saveNote() {
    const mode = document.getElementById('saveNoteBtn').dataset.mode;
    const title = document.getElementById('noteTitle').value.trim();
    const content = document.getElementById('noteContent').value.trim();
    
    if (!title || !content) {
        alert('제목과 내용을 모두 입력해주세요.');
        return;
    }
    
    try {
        let response;
        
        if (mode === 'create') {
            response = await fetch('/api/student/notes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, content })
            });
        } else {
            const noteId = document.getElementById('saveNoteBtn').dataset.noteId;
            response = await fetch(`/api/student/notes/${noteId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, content })
            });
        }
        
        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            hideNoteModal();
            loadNotes();
        } else {
            alert('저장 실패: ' + result.message);
        }
    } catch (error) {
        console.error('저장 오류:', error);
        alert('저장 중 오류가 발생했습니다.');
    }
}

async function editNote(noteId) {
    try {
        const response = await fetch(`/api/student/notes/${noteId}`);
        if (!response.ok) {
            // 개별 노트 조회 API가 없으므로 전체에서 찾기
            const notesResponse = await fetch('/api/student/notes');
            const notes = await notesResponse.json();
            const note = notes.find(n => n.id == noteId);
            
            if (note) {
                document.getElementById('noteModalTitle').textContent = '노트 수정';
                document.getElementById('noteTitle').value = note.title;
                document.getElementById('noteContent').value = note.content;
                document.getElementById('saveNoteBtn').dataset.mode = 'edit';
                document.getElementById('saveNoteBtn').dataset.noteId = noteId;
                document.getElementById('noteModal').style.display = 'block';
            }
        }
    } catch (error) {
        console.error('노트 로드 실패:', error);
    }
}

async function deleteNote(noteId) {
    if (!confirm('정말로 이 노트를 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/student/notes/${noteId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert(result.message);
            loadNotes();
        } else {
            alert('삭제 실패: ' + result.message);
        }
    } catch (error) {
        console.error('삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}
