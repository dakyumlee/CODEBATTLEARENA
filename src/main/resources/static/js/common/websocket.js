class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, 
            (frame) => {
                console.log('WebSocket 연결됨: ' + frame);
                this.connected = true;
                this.reconnectAttempts = 0;
                this.subscribeToNotifications();
            },
            (error) => {
                console.error('WebSocket 연결 실패:', error);
                this.connected = false;
                this.reconnect();
            }
        );
    }

    subscribeToNotifications() {
        if (this.stompClient && this.connected) {
            this.stompClient.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body);
                this.handleNotification(notification);
            });
            
            this.stompClient.subscribe('/topic/activities', (message) => {
                const activity = JSON.parse(message.body);
                this.handleActivity(activity);
            });
        }
    }

    handleNotification(notification) {
        console.log('알림 수신:', notification);
        
        // 화면에 알림 표시
        this.showNotification(notification.message);
        
        // 타입별 처리
        switch(notification.type) {
            case 'new_material':
                this.handleNewMaterial(notification.material);
                break;
            case 'new_problem':
                this.handleNewProblem(notification.problem);
                break;
            case 'new_quiz':
                this.handleNewQuiz(notification.quiz);
                break;
            case 'announcement':
                this.handleAnnouncement(notification.message);
                break;
        }
    }

    handleActivity(activity) {
        // 강사 대시보드용 활동 업데이트
        if (window.updateStudentActivity) {
            window.updateStudentActivity(activity);
        }
    }

    showNotification(message) {
        // 간단한 토스트 알림 생성
        const notification = document.createElement('div');
        notification.className = 'notification-toast';
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #007bff;
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            z-index: 1000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideIn 0.3s ease;
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }

    handleNewMaterial(material) {
        // 오늘의 학습 페이지 새로고침
        if (window.location.pathname.includes('/student/today')) {
            this.refreshTodayPage();
        }
    }

    handleNewProblem(problem) {
        // 문제 목록 새로고침
        if (window.location.pathname.includes('/student/today') || 
            window.location.pathname.includes('/student/practice')) {
            this.refreshTodayPage();
        }
    }

    refreshTodayPage() {
        if (typeof loadTodayContent === 'function') {
            loadTodayContent();
        }
    }

    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`WebSocket 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

// 전역 WebSocket 매니저
const wsManager = new WebSocketManager();

// 페이지 로드 시 연결
document.addEventListener('DOMContentLoaded', () => {
    wsManager.connect();
});

// 페이지 언로드 시 연결 해제
window.addEventListener('beforeunload', () => {
    wsManager.disconnect();
});
