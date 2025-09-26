class StudentActivityTracker {
    constructor() {
        this.currentPage = window.location.pathname;
        this.currentActivity = '페이지 로드';
        this.lastActivityTime = Date.now();
        this.activityInterval = null;
    }

    start() {
        this.sendActivity('페이지 접속', this.currentPage);
        this.startActivityTracking();
        this.setupEventListeners();
    }

    startActivityTracking() {
        // 5초마다 활동 상태 전송
        this.activityInterval = setInterval(() => {
            this.sendCurrentActivity();
        }, 5000);
    }

    setupEventListeners() {
        // 페이지 이동 감지
        window.addEventListener('beforeunload', () => {
            this.sendActivity('페이지 이탈', this.currentPage);
        });

        // 클릭 이벤트 감지
        document.addEventListener('click', (e) => {
            const activity = this.getActivityFromElement(e.target);
            if (activity) {
                this.updateActivity(activity);
            }
        });

        // 입력 이벤트 감지
        document.addEventListener('input', (e) => {
            if (e.target.matches('textarea, input[type="text"]')) {
                this.updateActivity('코드 작성 중');
            }
        });

        // 코드 제출 등 특정 액션 감지
        document.addEventListener('submit', (e) => {
            this.updateActivity('과제 제출');
        });
    }

    getActivityFromElement(element) {
        // 버튼이나 링크에서 활동 추정
        if (element.matches('button')) {
            const text = element.textContent.trim();
            if (text.includes('제출')) return '코드 제출';
            if (text.includes('저장')) return '노트 저장';
            if (text.includes('전송')) return 'AI 채팅';
            return '버튼 클릭';
        }

        if (element.matches('a')) {
            const href = element.getAttribute('href');
            if (href) {
                if (href.includes('battle')) return '배틀 참여';
                if (href.includes('practice')) return '문제 풀이';
                if (href.includes('ai-tutor')) return 'AI 튜터';
                if (href.includes('notes')) return '노트 작성';
            }
        }

        return null;
    }

    updateActivity(activity) {
        if (this.currentActivity !== activity) {
            this.currentActivity = activity;
            this.lastActivityTime = Date.now();
            this.sendActivity(activity, this.currentPage);
        }
    }

    sendCurrentActivity() {
        // 5분 이상 비활성 상태면 '휴식중'으로 변경
        const idleTime = Date.now() - this.lastActivityTime;
        if (idleTime > 300000) { // 5분
            this.currentActivity = '휴식중';
        }

        this.sendActivity(this.currentActivity, this.currentPage);
    }

    sendActivity(activity, page) {
        if (window.webSocketManager && window.webSocketManager.stompClient) {
            window.webSocketManager.sendStudentActivity(activity, page);
        }
    }

    stop() {
        if (this.activityInterval) {
            clearInterval(this.activityInterval);
        }
    }
}

// 전역 활동 추적기
window.studentActivityTracker = new StudentActivityTracker();

// 학생 페이지에서만 활동 추적 시작
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.startsWith('/student') && AuthManager.isLoggedIn()) {
        setTimeout(() => {
            window.studentActivityTracker.start();
        }, 2000); // WebSocket 연결 후 시작
    }
});
