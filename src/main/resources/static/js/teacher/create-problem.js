function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    document.querySelector(`[onclick="switchTab('${tabName}')"]`).classList.add('active');
    document.getElementById(tabName + 'Tab').classList.add('active');
}

document.getElementById('codingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const problemData = {
        title: formData.get('title') || e.target.querySelector('input[type="text"]').value,
        difficulty: e.target.querySelector('select').value,
        description: e.target.querySelector('textarea').value,
        timeLimit: parseInt(e.target.querySelector('input[type="number"]').value),
        type: 'CODING'
    };

    try {
        const response = await fetch('/api/teacher/problem', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(problemData)
        });

        const result = await response.json();
        
        if (result.success) {
            alert('문제가 성공적으로 출제되었습니다! 학생들에게 알림이 전송되었습니다.');
            e.target.reset();
        } else {
            alert('문제 출제에 실패했습니다: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
});

document.getElementById('quizForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const inputs = e.target.querySelectorAll('input, textarea, select');
    const quizData = {
        title: inputs[0].value,
        question: inputs[1].value,
        options: [
            inputs[2].value,
            inputs[3].value,
            inputs[4].value,
            inputs[5].value
        ],
        correctAnswer: parseInt(inputs[6].value),
        explanation: inputs[8].value,
        timeLimit: parseInt(inputs[7].value),
        type: 'QUIZ'
    };

    try {
        const response = await fetch('/api/teacher/quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(quizData)
        });

        const result = await response.json();
        
        if (result.success) {
            alert('퀴즈가 성공적으로 출제되었습니다! 학생들에게 알림이 전송되었습니다.');
            e.target.reset();
        } else {
            alert('퀴즈 출제에 실패했습니다: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
});

document.getElementById('examForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const inputs = e.target.querySelectorAll('input, textarea');
    const examData = {
        title: inputs[0].value,
        timeLimit: parseInt(inputs[2].value),
        maxScore: parseInt(inputs[3].value),
        instructions: inputs[4].value,
        type: 'EXAM'
    };

    try {
        const response = await fetch('/api/teacher/exam', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(examData)
        });

        const result = await response.json();
        
        if (result.success) {
            alert('시험이 성공적으로 출제되었습니다! 학생들에게 알림이 전송되었습니다.');
            e.target.reset();
        } else {
            alert('시험 출제에 실패했습니다: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
});

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
