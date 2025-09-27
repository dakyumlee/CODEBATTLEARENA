        async function loadSubmissions() {
            try {
                const response = await fetch('/api/student/teacher-problems', {
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('authToken')
                    }
                });
                const problems = await response.json();
                
                const section = document.getElementById('submissionsSection');
                
                if (problems.length > 0) {
                    section.innerHTML = problems.map(problem => `
                        <div style="background: #1a365d; border: 1px solid #2b6cb0; border-radius: 12px; padding: 25px; margin-bottom: 20px;">
                            <h3 style="color: #90cdf4; margin-bottom: 10px;">${problem.title}</h3>
                            <p style="color: #cbd5e0; margin-bottom: 15px;">${problem.description}</p>
                            <div style="display: flex; gap: 12px; align-items: center;">
                                <button onclick="solveTeacherProblem(${problem.id}, '${problem.title}', '${problem.description}', '${problem.difficulty}')" style="padding: 12px 24px; background: #4299e1; color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer;">문제 풀기</button>
                                <span style="color: #a0aec0;">난이도: ${problem.difficulty}</span>
                                <span style="color: #a0aec0;">제한시간: ${problem.timeLimit}분</span>
                            </div>
                        </div>
                    `).join('');
                } else {
                    section.innerHTML = '<div class="empty-state">출제된 문제가 없습니다.</div>';
                }
            } catch (error) {
                console.error('강사 문제 로드 실패:', error);
                document.getElementById('submissionsSection').innerHTML = '<div class="empty-state">문제를 불러올 수 없습니다.</div>';
            }
        }
        
        function solveTeacherProblem(id, title, description, difficulty) {
            const problemData = {
                id: id,
                title: title,
                description: description,
                difficulty: difficulty
            };
            
            localStorage.setItem('currentTeacherProblem', JSON.stringify(problemData));
            window.location.href = '/student/teacher-problem';
        }
