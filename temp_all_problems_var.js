let allProblems = []; // 전역 변수로 추가

// loadProblems 함수 수정
async function loadProblems() {
    try {
        const response = await fetch('/api/teacher/problems', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        const data = await response.json();
        
        allProblems = data.problems || []; // 전역 변수에 저장
        
        const container = document.getElementById('problemsContainer');
        
        if (allProblems.length > 0) {
            // 기존 HTML 생성 코드...
        }
    } catch (error) {
        // 에러 처리...
    }
}
