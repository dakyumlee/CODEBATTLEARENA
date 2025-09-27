let currentSubmissionType = 'ALL';

async function loadSubmissionsByType(type) {
    currentSubmissionType = type;
    document.getElementById('submissionTypeFilter').textContent = 
        type === 'PROBLEM' ? '문제 제출물' : 
        type === 'QUIZ' ? '퀴즈 제출물' : 
        type === 'EXAM' ? '시험 제출물' : '전체 제출물';
    
    try {
        const response = await fetch('/api/teacher/submissions', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
        });
        const data = await response.json();
        
        const container = document.getElementById('submissionsContainer');
        
        if (data.submissions && data.submissions.length > 0) {
            let filteredSubmissions = data.submissions;
            if (type !== 'ALL') {
                filteredSubmissions = data.submissions.filter(submission => {
                    return submission.problemType === type;
                });
            }
            
            if (filteredSubmissions.length === 0) {
                container.innerHTML = `<div class="empty-state">${document.getElementById('submissionTypeFilter').textContent}이 없습니다.</div>`;
                return;
            }
            
            container.innerHTML = filteredSubmissions.map(submission => `
                <div class="submission-card">
                    <div class="submission-header">
                        <div>
                            <strong>${submission.studentName}</strong> - ${submission.problemTitle}
                            <span class="difficulty-badge difficulty-${getProblemTypeClass(submission.problemType || 'PROBLEM')}">${submission.problemType || 'PROBLEM'}</span>
                            <div class="submission-info">제출일: ${new Date(submission.submittedAt).toLocaleString()}</div>
                        </div>
                        <div style="display: flex; gap: 10px; align-items: center;">
                            <span class="status-badge status-${submission.status.toLowerCase()}">${submission.status === 'PENDING' ? '채점 대기' : '채점 완료'}</span>
                            ${submission.status === 'PENDING' ? 
                                `<button class="btn btn-primary btn-small" onclick="openImprovedGradeModal(${submission.id})">채점하기</button>` : 
                                `<span style="color: #4caf50; font-weight: 600;">${submission.score}점</span>`
                            }
                        </div>
                    </div>
                    <div class="submission-answer" style="max-height: 100px; overflow-y: auto;">${submission.answer}</div>
                    ${submission.feedback ? `<div style="background: #e8f5e8; padding: 10px; border-radius: 6px; margin-top: 10px;"><strong>피드백:</strong> ${submission.feedback}</div>` : ''}
                </div>
            `).join('');
        } else {
            container.innerHTML = '<div class="empty-state">제출된 답안이 없습니다.</div>';
        }
    } catch (error) {
        console.error('제출물 로드 실패:', error);
        document.getElementById('submissionsContainer').innerHTML = '<div class="empty-state">제출물을 불러올 수 없습니다.</div>';
    }
}

function loadAllSubmissions() {
    loadSubmissionsByType('ALL');
}

function getProblemTypeClass(type) {
    switch(type) {
        case 'PROBLEM': return 'easy';
        case 'QUIZ': return 'medium';
        case 'EXAM': return 'hard';
        default: return 'easy';
    }
}

async function openImprovedGradeModal(submissionId) {
    try {
        const [submissionsResponse, problemsResponse] = await Promise.all([
            fetch('/api/teacher/submissions', {
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
            }),
            fetch('/api/teacher/problems', {
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
            })
        ]);
        
        const submissionsData = await submissionsResponse.json();
        const problemsData = await problemsResponse.json();
        
        const submission = submissionsData.submissions.find(s => s.id === submissionId);
        const problem = problemsData.problems.find(p => p.id === submission.problemId);
        
        if (!submission || !problem) {
            alert('제출물 또는 문제 정보를 찾을 수 없습니다.');
            return;
        }
        
        document.getElementById('improvedGradeSubmissionId').value = submissionId;
        document.getElementById('improvedGradeStudentName').textContent = submission.studentName;
        document.getElementById('improvedGradeProblemType').textContent = problem.type || 'PROBLEM';
        document.getElementById('improvedGradeProblemType').className = `status-badge status-${getProblemTypeClass(problem.type)}`;
        document.getElementById('improvedGradeProblemTitle').textContent = problem.title;
        document.getElementById('improvedGradeProblemDescription').textContent = problem.description;
        document.getElementById('improvedGradeStudentAnswer').textContent = submission.answer;
        
        const quizOptions = document.getElementById('improvedGradeQuizOptions');
        if (problem.type === 'QUIZ' && problem.optionA) {
            document.getElementById('optionA').textContent = problem.optionA;
            document.getElementById('optionB').textContent = problem.optionB;
            document.getElementById('optionC').textContent = problem.optionC;
            document.getElementById('optionD').textContent = problem.optionD;
            document.getElementById('correctAnswer').textContent = problem.correctAnswer;
            quizOptions.style.display = 'block';
        } else {
            quizOptions.style.display = 'none';
        }
        
        document.getElementById('improvedGradeModal').style.display = 'flex';
    } catch (error) {
        alert('정보를 불러올 수 없습니다: ' + error.message);
    }
}

function closeImprovedGradeModal() {
    document.getElementById('improvedGradeModal').style.display = 'none';
    document.getElementById('improvedGradeForm').reset();
}

document.getElementById('improvedGradeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const submissionId = document.getElementById('improvedGradeSubmissionId').value;
    const gradeData = {
        submissionId: submissionId,
        score: formData.get('score'),
        feedback: formData.get('feedback')
    };

    try {
        const response = await fetch('/api/teacher/grade-submission', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(gradeData)
        });

        const result = await response.json();
        
        if (result.success) {
            alert('채점이 완료되었습니다.');
            closeImprovedGradeModal();
            loadSubmissionsByType(currentSubmissionType);
        } else {
            alert('채점 실패: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
});

document.getElementById('improvedGradeModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeImprovedGradeModal();
    }
});
