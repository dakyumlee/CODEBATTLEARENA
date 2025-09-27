function editProblem(id) {
    // 현재 문제 정보를 가져와서 모달에 채우기
    const problem = allProblems.find(p => p.id === id);
    if (!problem) {
        alert('문제 정보를 찾을 수 없습니다.');
        return;
    }
    
    document.getElementById('editProblemId').value = problem.id;
    document.getElementById('editProblemTitle').value = problem.title;
    document.getElementById('editProblemDescription').value = problem.description;
    document.getElementById('editProblemDifficulty').value = problem.difficulty;
    document.getElementById('editProblemTimeLimit').value = problem.timeLimit;
    document.getElementById('editProblemPoints').value = problem.points;
    
    // 퀴즈인 경우 선택지도 채우기
    if (problem.type === 'QUIZ') {
        document.getElementById('editQuizOptions').style.display = 'block';
        document.getElementById('editOptionA').value = problem.optionA || '';
        document.getElementById('editOptionB').value = problem.optionB || '';
        document.getElementById('editOptionC').value = problem.optionC || '';
        document.getElementById('editOptionD').value = problem.optionD || '';
        document.getElementById('editCorrectAnswer').value = problem.correctAnswer || '';
    } else {
        document.getElementById('editQuizOptions').style.display = 'none';
    }
    
    document.getElementById('editProblemModal').style.display = 'flex';
}

function closeEditProblemModal() {
    document.getElementById('editProblemModal').style.display = 'none';
    document.getElementById('editProblemForm').reset();
}

// 수정 폼 제출 이벤트
document.getElementById('editProblemForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const problemId = document.getElementById('editProblemId').value;
    
    const problemData = {
        title: formData.get('title'),
        description: formData.get('description'),
        difficulty: formData.get('difficulty'),
        timeLimit: formData.get('timeLimit'),
        points: formData.get('points')
    };
    
    // 퀴즈인 경우 선택지도 포함
    if (document.getElementById('editQuizOptions').style.display !== 'none') {
        problemData.optionA = formData.get('optionA');
        problemData.optionB = formData.get('optionB');
        problemData.optionC = formData.get('optionC');
        problemData.optionD = formData.get('optionD');
        problemData.correctAnswer = formData.get('correctAnswer');
        problemData.type = 'QUIZ';
    }

    try {
        const response = await fetch(`/api/teacher/problems/${problemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(problemData)
        });

        const result = await response.json();
        
        if (result.success) {
            alert('문제가 성공적으로 수정되었습니다.');
            closeEditProblemModal();
            loadProblems();
        } else {
            alert('문제 수정 실패: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
});
