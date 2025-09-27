async function loadSubmissions() {
    try {
        const response = await fetch("/api/student/teacher-problems", {
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("authToken")
            }
        });
        const problems = await response.json();
        
        const section = document.getElementById("submissionsSection");
        
        if (problems.length > 0) {
            section.innerHTML = problems.map(problem => `
                <div class="teacher-problem-card">
                    <h3 class="teacher-problem-title">${problem.title}</h3>
                    <p class="teacher-problem-desc">${problem.description}</p>
                    ${problem.feedback ? `
                        <div style="background: #1a365d; border: 1px solid #4299e1; border-radius: 8px; padding: 15px; margin: 15px 0;">
                            <strong style="color: #4299e1;">강사 피드백:</strong>
                            <p style="color: #e2e8f0; margin: 8px 0 0 0;">${problem.feedback}</p>
                            ${problem.score ? `<p style="color: #68d391; margin: 5px 0 0 0;">점수: ${problem.score}점</p>` : ''}
                        </div>
                    ` : ''}
                    <div class="teacher-problem-actions">
                        ${!problem.isSubmitted ? 
                            `<button class="btn" onclick="openSubmitModal(${JSON.stringify(problem).replace(/"/g, '&quot;')})">문제 풀기</button>` :
                            problem.isGraded ? 
                                `<button class="btn btn-success" disabled>채점 완료 (${problem.score || 0}점)</button>` :
                                `<button class="btn btn-success" disabled>채점 대기중</button>`
                        }
                        <span style="color: #a0aec0;">난이도: ${problem.difficulty}</span>
                        <span style="color: #a0aec0;">제한시간: ${problem.timeLimit}분</span>
                        ${problem.isSubmitted ? 
                            `<div class="submission-status status-submitted">제출됨</div>` : 
                            `<div class="submission-status status-pending">미제출</div>`
                        }
                    </div>
                </div>
            `).join("");
        } else {
            section.innerHTML = '<div class="empty-state">출제된 문제가 없습니다.</div>';
        }
    } catch (error) {
        console.error("강사 문제 로드 실패:", error);
        document.getElementById("submissionsSection").innerHTML = '<div class="empty-state">문제를 불러올 수 없습니다.</div>';
    }
}
