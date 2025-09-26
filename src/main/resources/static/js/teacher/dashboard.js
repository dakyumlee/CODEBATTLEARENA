class TeacherDashboard {
    constructor() {
        this.students = [];
        this.stats = {};
        this.init();
    }

    async init() {
        await this.loadStudents();
        await this.loadStats();
        this.setupEventListeners();
        this.renderStudents();
        this.renderStats();
    }

    async loadStudents() {
        try {
            const response = await ApiClient.get('/api/teacher/students');
            this.students = response;
        } catch (error) {
            console.error('학생 목록 로딩 오류:', error);
        }
    }

    async loadStats() {
        try {
            const response = await ApiClient.get('/api/teacher/stats');
            this.stats = response;
        } catch (error) {
            console.error('통계 로딩 오류:', error);
        }
    }

    renderStudents() {
        const container = document.getElementById('studentsGrid');
        if (!container) return;

        container.innerHTML = this.students.map(student => `
            <div class="student-card ${student.onlineStatus ? 'online' : 'offline'}">
                <div class="student-status ${student.onlineStatus ? 'online' : 'offline'}"></div>
                <div class="student-info">
                    <h4>${student.name}</h4>
                    <p>${student.currentActivity || '학습 중'}</p>
                </div>
            </div>
        `).join('');
    }

    renderStats() {
        const totalElement = document.getElementById('totalStudents');
        const onlineElement = document.getElementById('onlineStudents');
        
        if (totalElement) totalElement.textContent = this.stats.totalStudents || 0;
        if (onlineElement) onlineElement.textContent = this.stats.onlineStudents || 0;
    }

    setupEventListeners() {
        const announcementBtn = document.getElementById('announcementBtn');
        const shareMaterialBtn = document.getElementById('shareMaterialBtn');
        const createProblemBtn = document.getElementById('createProblemBtn');
        const startQuizBtn = document.getElementById('startQuizBtn');

        if (announcementBtn) {
            announcementBtn.onclick = () => this.showAnnouncementModal();
        }
        if (shareMaterialBtn) {
            shareMaterialBtn.onclick = () => this.showMaterialModal();
        }
        if (createProblemBtn) {
            createProblemBtn.onclick = () => this.showProblemModal();
        }
        if (startQuizBtn) {
            startQuizBtn.onclick = () => this.showQuizModal();
        }
    }

    showAnnouncementModal() {
        const modal = this.createModal('공지사항 전송', `
            <div class="modal-body">
                <textarea id="announcementText" placeholder="공지사항을 입력하세요..." style="width: 100%; height: 100px; margin-bottom: 1rem;"></textarea>
                <div class="modal-actions">
                    <button onclick="teacherDashboard.sendAnnouncement()" class="btn btn-primary">전송</button>
                    <button onclick="teacherDashboard.closeModal()" class="btn btn-secondary">취소</button>
                </div>
            </div>
        `);
    }

    showMaterialModal() {
        const modal = this.createModal('자료 공유', `
            <div class="modal-body">
                <div style="margin-bottom: 1rem;">
                    <label>파일 업로드:</label>
                    <input type="file" id="materialFile" multiple style="width: 100%; margin-top: 0.5rem;">
                </div>
                <div style="margin-bottom: 1rem;">
                    <label>또는 링크 공유:</label>
                    <input type="url" id="materialLink" placeholder="https://..." style="width: 100%; margin-top: 0.5rem;">
                    <input type="text" id="linkTitle" placeholder="링크 제목" style="width: 100%; margin-top: 0.5rem;">
                </div>
                <div class="modal-actions">
                    <button onclick="teacherDashboard.shareMaterial()" class="btn btn-primary">공유</button>
                    <button onclick="teacherDashboard.closeModal()" class="btn btn-secondary">취소</button>
                </div>
            </div>
        `);
    }

    showProblemModal() {
        const modal = this.createModal('즉석 문제 출제', `
            <div class="modal-body">
                <input type="text" id="problemTitle" placeholder="문제 제목" style="width: 100%; margin-bottom: 1rem;">
                <textarea id="problemDescription" placeholder="문제 설명" style="width: 100%; height: 100px; margin-bottom: 1rem;"></textarea>
                <select id="problemDifficulty" style="width: 100%; margin-bottom: 1rem;">
                    <option value="하">하</option>
                    <option value="중">중</option>
                    <option value="상">상</option>
                </select>
                <input type="number" id="problemPoints" placeholder="배점" min="1" max="100" value="10" style="width: 100%; margin-bottom: 1rem;">
                <div class="modal-actions">
                    <button onclick="teacherDashboard.createProblem()" class="btn btn-primary">출제</button>
                    <button onclick="teacherDashboard.closeModal()" class="btn btn-secondary">취소</button>
                </div>
            </div>
        `);
    }

    showQuizModal() {
        const modal = this.createModal('퀴즈 시작', `
            <div class="modal-body">
                <input type="text" id="quizTitle" placeholder="퀴즈 제목" style="width: 100%; margin-bottom: 1rem;">
                <input type="number" id="quizDuration" placeholder="제한시간 (분)" min="1" max="60" value="10" style="width: 100%; margin-bottom: 1rem;">
                <textarea id="quizQuestions" placeholder="문제들을 한 줄씩 입력하세요..." style="width: 100%; height: 100px; margin-bottom: 1rem;"></textarea>
                <div class="modal-actions">
                    <button onclick="teacherDashboard.startQuiz()" class="btn btn-primary">퀴즈 시작</button>
                    <button onclick="teacherDashboard.closeModal()" class="btn btn-secondary">취소</button>
                </div>
            </div>
        `);
    }

    createModal(title, content) {
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3>${title}</h3>
                    <button onclick="teacherDashboard.closeModal()" class="modal-close">&times;</button>
                </div>
                ${content}
            </div>
        `;
        
        document.body.appendChild(modal);
        return modal;
    }

    async sendAnnouncement() {
        const text = document.getElementById('announcementText').value.trim();
        if (!text) {
            alert('공지사항을 입력해주세요.');
            return;
        }

        try {
            if (window.webSocketManager) {
                window.webSocketManager.sendNotification(text);
            }
            alert('공지사항이 전송되었습니다.');
            this.closeModal();
        } catch (error) {
            console.error('공지사항 전송 오류:', error);
            alert('공지사항 전송에 실패했습니다.');
        }
    }

    async shareMaterial() {
        const file = document.getElementById('materialFile').files[0];
        const link = document.getElementById('materialLink').value.trim();
        const linkTitle = document.getElementById('linkTitle').value.trim();

        try {
            if (file) {
                const formData = new FormData();
                formData.append('files', file);
                
                const response = await fetch('/api/teacher/materials/upload', {
                    method: 'POST',
                    headers: AuthManager.getAuthHeaders(),
                    body: formData
                });
                
                if (response.ok) {
                    alert('파일이 공유되었습니다.');
                } else {
                    alert('파일 공유에 실패했습니다.');
                }
            } else if (link && linkTitle) {
                await ApiClient.post('/api/teacher/materials/link', { url: link, title: linkTitle });
                alert('링크가 공유되었습니다.');
            } else {
                alert('파일 또는 링크를 입력해주세요.');
                return;
            }
            
            this.closeModal();
        } catch (error) {
            console.error('자료 공유 오류:', error);
            alert('자료 공유에 실패했습니다.');
        }
    }

    async createProblem() {
        const title = document.getElementById('problemTitle').value.trim();
        const description = document.getElementById('problemDescription').value.trim();
        const difficulty = document.getElementById('problemDifficulty').value;
        const points = parseInt(document.getElementById('problemPoints').value);

        if (!title || !description) {
            alert('제목과 설명을 입력해주세요.');
            return;
        }

        try {
            const response = await ApiClient.post('/api/teacher/create-problem', {
                title, description, difficulty, points, type: 'instant'
            });
            
            alert('문제가 출제되었습니다.');
            this.closeModal();
        } catch (error) {
            console.error('문제 출제 오류:', error);
            alert('문제 출제에 실패했습니다.');
        }
    }

    async startQuiz() {
        const title = document.getElementById('quizTitle').value.trim();
        const duration = parseInt(document.getElementById('quizDuration').value);
        const questionsText = document.getElementById('quizQuestions').value.trim();

        if (!title || !questionsText) {
            alert('퀴즈 제목과 문제를 입력해주세요.');
            return;
        }

        const questions = questionsText.split('\n').filter(q => q.trim());

        try {
            const response = await ApiClient.post('/api/teacher/create-quiz', {
                title, duration, questions
            });
            
            alert('퀴즈가 시작되었습니다.');
            this.closeModal();
        } catch (error) {
            console.error('퀴즈 시작 오류:', error);
            alert('퀴즈 시작에 실패했습니다.');
        }
    }

    closeModal() {
        const modal = document.querySelector('.modal-overlay');
        if (modal) {
            modal.remove();
        }
    }
}

window.teacherDashboard = new TeacherDashboard();

// WebSocket 학생 활동 업데이트 처리
window.addEventListener('student-activity-update', (event) => {
    const activity = event.detail;
    const studentCard = document.querySelector(`[data-student-id="${activity.userId}"]`);
    if (studentCard) {
        const activityElement = studentCard.querySelector('.student-activity');
        if (activityElement) {
            activityElement.textContent = activity.activity;
        }
    }
});
