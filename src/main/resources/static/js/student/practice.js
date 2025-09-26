class PracticeManager {
    constructor() {
        this.problems = [];
        this.currentCategory = 'all';
        this.userStats = {
            totalAttempts: 0,
            solvedCount: 0,
            successRate: 0,
            avgScore: 0
        };
        this.init();
    }

    async init() {
        await this.loadProblems();
        await this.loadStats();
        this.setupEventListeners();
        this.renderProblems();
        this.renderStats();
    }

    async loadProblems() {
        try {
            const response = await ApiClient.get('/api/problems?category=' + this.currentCategory);
            this.problems = response.length > 0 ? response : this.getDefaultProblems();
        } catch (error) {
            console.error('문제 로딩 오류:', error);
            this.problems = this.getDefaultProblems();
        }
    }

    getDefaultProblems() {
        return [
            { id: 1, title: '두 수의 합', difficulty: '하', category: 'basic', solved: false, attempts: 0, score: 0 },
            { id: 2, title: '배열에서 최댓값 찾기', difficulty: '하', category: 'array', solved: true, attempts: 3, score: 85 },
            { id: 3, title: '문자열 뒤집기', difficulty: '중', category: 'string', solved: false, attempts: 1, score: 0 },
            { id: 4, title: '팩토리얼 계산', difficulty: '중', category: 'math', solved: true, attempts: 2, score: 92 },
            { id: 5, title: '이진 검색', difficulty: '상', category: 'algorithm', solved: false, attempts: 0, score: 0 },
            { id: 6, title: '정렬 알고리즘 구현', difficulty: '상', category: 'algorithm', solved: false, attempts: 2, score: 45 },
            { id: 7, title: '해시맵 활용', difficulty: '중', category: 'data-structure', solved: true, attempts: 1, score: 88 },
            { id: 8, title: '재귀함수 활용', difficulty: '상', category: 'recursion', solved: false, attempts: 1, score: 0 }
        ];
    }

    async loadStats() {
        try {
            const response = await ApiClient.get('/api/student/practice-stats');
            this.userStats = response;
        } catch (error) {
            console.error('통계 로딩 오류:', error);
            this.calculateStatsFromProblems();
        }
    }

    calculateStatsFromProblems() {
        const totalAttempts = this.problems.reduce((sum, p) => sum + p.attempts, 0);
        const solvedCount = this.problems.filter(p => p.solved).length;
        const totalScore = this.problems.filter(p => p.solved).reduce((sum, p) => sum + p.score, 0);

        this.userStats = {
            totalAttempts,
            solvedCount,
            successRate: totalAttempts > 0 ? Math.round((solvedCount / this.problems.length) * 100) : 0,
            avgScore: solvedCount > 0 ? Math.round(totalScore / solvedCount) : 0
        };
    }

    renderStats() {
        const statsContainer = document.getElementById('practiceStats');
        if (!statsContainer) return;

        statsContainer.innerHTML = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value">${this.userStats.totalAttempts}</div>
                    <div class="stat-label">총 시도</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${this.userStats.solvedCount}</div>
                    <div class="stat-label">해결 문제</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${this.userStats.successRate}%</div>
                    <div class="stat-label">정답률</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${this.userStats.avgScore}점</div>
                    <div class="stat-label">평균 점수</div>
                </div>
            </div>
        `;
    }

    renderProblems() {
        const container = document.getElementById('problemsContainer');
        if (!container) return;

        const filteredProblems = this.currentCategory === 'all' 
            ? this.problems 
            : this.problems.filter(p => p.category === this.currentCategory);

        container.innerHTML = `
            <div class="category-tabs">
                <button class="tab ${this.currentCategory === 'all' ? 'active' : ''}" onclick="practiceManager.setCategory('all')">전체</button>
                <button class="tab ${this.currentCategory === 'basic' ? 'active' : ''}" onclick="practiceManager.setCategory('basic')">기초</button>
                <button class="tab ${this.currentCategory === 'array' ? 'active' : ''}" onclick="practiceManager.setCategory('array')">배열</button>
                <button class="tab ${this.currentCategory === 'string' ? 'active' : ''}" onclick="practiceManager.setCategory('string')">문자열</button>
                <button class="tab ${this.currentCategory === 'algorithm' ? 'active' : ''}" onclick="practiceManager.setCategory('algorithm')">알고리즘</button>
                <button class="tab ${this.currentCategory === 'data-structure' ? 'active' : ''}" onclick="practiceManager.setCategory('data-structure')">자료구조</button>
            </div>
            <div class="problems-list">
                ${filteredProblems.map(problem => `
                    <div class="problem-card ${problem.solved ? 'solved' : ''}">
                        <div class="problem-info">
                            <h3>${problem.title}</h3>
                            <span class="difficulty ${problem.difficulty}">${problem.difficulty}</span>
                        </div>
                        <div class="problem-stats">
                            <span>시도: ${problem.attempts}회</span>
                            ${problem.solved ? `<span class="score">점수: ${problem.score}점</span>` : ''}
                        </div>
                        <button onclick="practiceManager.solveProblem(${problem.id})" class="btn btn-primary">
                            ${problem.solved ? '다시 풀기' : '문제 풀기'}
                        </button>
                    </div>
                `).join('')}
            </div>
        `;
    }

    setCategory(category) {
        this.currentCategory = category;
        this.renderProblems();
    }

    setupEventListeners() {
        // 카테고리 변경 이벤트는 renderProblems에서 직접 처리
    }

    async solveProblem(problemId) {
        const problem = this.problems.find(p => p.id === problemId);
        if (!problem) return;

        // 문제 풀이 모달이나 별도 페이지로 이동하는 로직
        // 현재는 시뮬레이션
        const score = Math.floor(Math.random() * 100);
        const solved = score >= 60;

        problem.attempts++;
        if (solved && !problem.solved) {
            problem.solved = true;
            problem.score = score;
        } else if (solved && problem.solved) {
            problem.score = Math.max(problem.score, score);
        }

        this.calculateStatsFromProblems();
        this.renderStats();
        this.renderProblems();

        if (window.studentActivityTracker) {
            window.studentActivityTracker.updateActivity('문제 풀이');
        }
    }
}

window.practiceManager = new PracticeManager();
