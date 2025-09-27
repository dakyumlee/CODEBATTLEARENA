async function loadStudentProblems() {
    try {
        const [problemsResponse, submissionsResponse] = await Promise.all([
            fetch('/api/teacher/problems', {
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
            }),
            fetch('/api/student/my-submissions', {
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
            })
        ]);
        
        const problemsData = await problemsResponse.json();
        const submissionsData = await submissionsResponse.json();
        
        const problems = problemsData.problems || [];
        const mySubmissions = submissionsData.submissions || [];
        
        const container = document.getElementById('teacherProblemsContainer');
        
        if (problems.length === 0) {
            container.innerHTML = '<div class="empty-state">출제된 문제가 없습니다.</div>';
            return;
        }
        
        container.innerHTML = problems.map(problem => {
            const submission = mySubmissions.find(s => s.problemId === problem.id);
            const isSubmitted = !!submission;
            const statusClass = isSubmitted ? 'submitted' : 'pending';
            const statusText = isSubmitted ? '제출완료' : '미제출';
            const statusIcon = isSubmitted ? '✅' : '❌';
            
            return `
                <div class="problem-card ${statusClass}">
                    <div class="problem-header">
                        <div>
                            <h4>${problem.title}</h4>
                            <span class="difficulty-badge difficulty-${getDifficultyClass(problem.difficulty)}">${problem.difficulty}</span>
                            <span class="type-badge type-${problem.type.toLowerCase()}">${problem.type}</span>
                        </div>
                        <div class="problem-status">
                            <span class="status-indicator status-${statusClass}">
                                ${statusIcon} ${statusText}
                            </span>
                            ${isSubmitted && submission.score !== null ? 
                                `<span class="score-badge">${submission.score}점</span>` : ''}
                        </div>
                    </div>
                    <div class="problem-description">${problem.description}</div>
                    <div class="problem-actions">
                        ${isSubmitted ? 
                            `<button class="btn btn-success btn-small" onclick="viewSubmission(${submission.id})">제출내역 보기</button>` :
                            `<button class="btn btn-primary btn-small" onclick="solveProblem(${problem.id})">문제 풀기</button>`
                        }
                        <small class="problem-info">제한시간: ${problem.timeLimit}분 | 배점: ${problem.points}점</small>
                    </div>
                    ${isSubmitted && submission.feedback ? 
                        `<div class="feedback-section">
                            <strong>강사 피드백:</strong>
                            <div class="feedback-content">${submission.feedback}</div>
                        </div>` : ''
                    }
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('문제 로드 실패:', error);
        document.getElementById('teacherProblemsContainer').innerHTML = '<div class="empty-state">문제를 불러올 수 없습니다.</div>';
    }
}

function viewSubmission(submissionId) {
    // 제출내역 모달 또는 페이지로 이동
    window.location.href = `/student/submission/${submissionId}`;
}

function solveProblem(problemId) {
    // 문제 풀이 페이지로 이동
    window.location.href = `/student/solve/${problemId}`;
}
