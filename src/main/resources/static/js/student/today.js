document.addEventListener('DOMContentLoaded', function() {
    loadTodayContent();
    window.loadTodayContent = loadTodayContent;
});

function loadTodayContent() {
    Promise.all([
        loadAIProblem(),
        loadMaterials(),
        loadSubmissions()
    ]).then(() => {
        console.log('오늘의 학습 데이터 로드 완료');
    }).catch(error => {
        console.error('데이터 로드 실패:', error);
    });
}

async function loadAIProblem() {
    try {
        const response = await fetch('/api/student/today');
        const data = await response.json();
        
        if (data.aiProblem) {
            updateAIProblemSection(data.aiProblem);
        }
    } catch (error) {
        console.error('AI 문제 로드 실패:', error);
    }
}

function updateAIProblemSection(problem) {
    const aiSection = document.querySelector('.ai-problem-section');
    if (aiSection) {
        aiSection.innerHTML = `
            <div class="problem-card">
                <div class="problem-header">
                    <h3>🤖 ${problem.title}</h3>
                    <span class="difficulty-badge ${problem.difficulty}">${problem.difficulty}</span>
                </div>
                <p class="problem-description">${problem.description}</p>
                <div class="problem-actions">
                    <button class="btn-primary" onclick="startAIProblem('${problem.id}')">문제 풀기</button>
                    <span class="points">+${problem.points}점</span>
                </div>
            </div>
        `;
    }
}

async function loadMaterials() {
    try {
        const response = await fetch('/api/teacher/materials');
        const materials = await response.json();
        
        updateMaterialsSection(materials);
    } catch (error) {
        console.error('자료 로드 실패:', error);
    }
}

function updateMaterialsSection(materials) {
    const materialsList = document.querySelector('.materials-list');
    if (!materialsList) return;
    
    if (materials.length === 0) {
        materialsList.innerHTML = '<p class="empty-state">공유된 자료가 없습니다.</p>';
        return;
    }
    
    materialsList.innerHTML = materials.map(material => `
        <div class="material-item">
            <div class="material-info">
                <h4>${material.title}</h4>
                <p>${material.description || ''}</p>
                <small>업로드: ${new Date(material.createdAt).toLocaleDateString()}</small>
            </div>
            <div class="material-actions">
                ${material.fileType === 'file' ? 
                    `<button class="btn-outline" onclick="previewFile('${material.id}')">미리보기</button>
                     <button class="btn-primary" onclick="downloadFile('${material.id}')">다운로드</button>` :
                    `<button class="btn-primary" onclick="openLink('${material.url}')">링크 열기</button>`
                }
            </div>
        </div>
    `).join('');
}

function startAIProblem(problemId) {
    window.location.href = `/student/ai-problem?id=${problemId}`;
}

function previewFile(fileId) {
    window.open(`/api/materials/${fileId}/preview`, '_blank');
}

function downloadFile(fileId) {
    window.open(`/api/materials/${fileId}/download`);
}

function openLink(url) {
    window.open(url, '_blank');
}

async function loadSubmissions() {
    try {
        const response = await fetch('/api/student/submissions');
        const submissions = await response.json();
        
        updateSubmissionsSection(submissions);
    } catch (error) {
        console.error('제출 내역 로드 실패:', error);
    }
}

function updateSubmissionsSection(submissions) {
    const submissionsList = document.querySelector('.submissions-list');
    if (!submissionsList) return;
    
    if (submissions.length === 0) {
        submissionsList.innerHTML = '<p class="empty-state">아직 제출한 답안이 없습니다.</p>';
        return;
    }
    
    submissionsList.innerHTML = submissions.map(submission => `
        <div class="submission-item">
            <div class="submission-info">
                <h4>${submission.problemTitle}</h4>
                <p class="score ${getScoreClass(submission.score)}">
                    ${submission.score !== null ? `${submission.score}점` : '채점 대기중'}
                </p>
                ${submission.feedback ? `<p class="feedback">"${submission.feedback}"</p>` : ''}
            </div>
            <div class="submission-date">
                ${new Date(submission.submittedAt).toLocaleString()}
            </div>
        </div>
    `).join('');
}

function getScoreClass(score) {
    if (score === null) return 'pending';
    if (score >= 90) return 'excellent';
    if (score >= 70) return 'good';
    if (score >= 50) return 'fair';
    return 'poor';
}
