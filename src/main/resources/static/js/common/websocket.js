class WebSocketManager {
    constructor() {
        this.socket = null;
        this.stompClient = null;
        this.subscriptions = {};
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
    }

    connect() {
        if (this.stompClient && this.stompClient.connected) {
            return;
        }

        console.log('WebSocket 연결 시도...');
        this.socket = new SockJS('/ws');
        this.stompClient = Stomp.over(this.socket);
        
        this.stompClient.connect({}, 
            (frame) => {
                console.log('WebSocket 연결 성공:', frame);
                this.reconnectAttempts = 0;
                this.setupDefaultSubscriptions();
            },
            (error) => {
                console.error('WebSocket 연결 실패:', error);
                this.handleReconnect();
            }
        );
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
        }
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        console.log('WebSocket 연결 해제');
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`WebSocket 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        } else {
            console.error('WebSocket 재연결 포기');
        }
    }

    setupDefaultSubscriptions() {
        // 알림 구독
        this.subscribe('/topic/notifications', (message) => {
            this.handleNotification(JSON.parse(message.body));
        });

        // 활동 모니터링 구독 (강사용)
        this.subscribe('/topic/activity', (message) => {
            this.handleActivityUpdate(JSON.parse(message.body));
        });

        // 배틀 업데이트 구독
        this.subscribe('/topic/battle', (message) => {
            this.handleBattleUpdate(JSON.parse(message.body));
        });
    }

    subscribe(topic, callback) {
        if (this.stompClient && this.stompClient.connected) {
            this.subscriptions[topic] = this.stompClient.subscribe(topic, callback);
        }
    }

    unsubscribe(topic) {
        if (this.subscriptions[topic]) {
            this.subscriptions[topic].unsubscribe();
            delete this.subscriptions[topic];
        }
    }

    send(destination, data) {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send(destination, {}, JSON.stringify(data));
        } else {
            console.warn('WebSocket이 연결되지 않음');
        }
    }

    // 학생 활동 전송
    sendStudentActivity(activity, page) {
        this.send('/app/student-activity', {
            userId: AuthManager.getCurrentUserId(),
            userName: AuthManager.getCurrentUserName(),
            activity: activity,
            page: page,
            timestamp: new Date().toISOString()
        });
    }

    // 공지사항 전송 (강사용)
    sendNotification(message) {
        this.send('/app/notification', {
            message: message,
            sender: AuthManager.getCurrentUserName(),
            timestamp: new Date().toISOString(),
            type: 'announcement'
        });
    }

    // 알림 처리
    handleNotification(notification) {
        console.log('알림 수신:', notification);
        
        // UI에 알림 표시
        this.showNotificationPopup(notification.message, notification.type);
        
        // 커스텀 이벤트 발생
        window.dispatchEvent(new CustomEvent('websocket-notification', { 
            detail: notification 
        }));
    }

    // 활동 업데이트 처리 (강사용)
    handleActivityUpdate(activity) {
        console.log('활동 업데이트:', activity);
        
        // 커스텀 이벤트 발생
        window.dispatchEvent(new CustomEvent('student-activity-update', { 
            detail: activity 
        }));
    }

    // 배틀 업데이트 처리
    handleBattleUpdate(battleData) {
        console.log('배틀 업데이트:', battleData);
        
        // 커스텀 이벤트 발생
        window.dispatchEvent(new CustomEvent('battle-update', { 
            detail: battleData 
        }));
    }

    // 알림 팝업 표시
    showNotificationPopup(message, type = 'info') {
        // 기존 알림 제거
        const existingNotification = document.getElementById('websocket-notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // 새 알림 생성
        const notification = document.createElement('div');
        notification.id = 'websocket-notification';
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'error' ? '#ef4444' : type === 'success' ? '#10b981' : '#3b82f6'};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 0.5rem;
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            z-index: 10000;
            max-width: 300px;
            font-family: 'Pretendard Variable', sans-serif;
            animation: slideInRight 0.3s ease;
        `;
        
        notification.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>${message}</div>
                <button onclick="this.parentElement.parentElement.remove()" style="background: none; border: none; color: white; font-size: 1.2rem; cursor: pointer; margin-left: 1rem;">&times;</button>
            </div>
        `;

        // CSS 애니메이션 추가
        if (!document.getElementById('websocket-notification-styles')) {
            const style = document.createElement('style');
            style.id = 'websocket-notification-styles';
            style.textContent = `
                @keyframes slideInRight {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `;
            document.head.appendChild(style);
        }

        document.body.appendChild(notification);

        // 5초 후 자동 제거
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }
}

// 전역 WebSocket 매니저 인스턴스
window.webSocketManager = new WebSocketManager();

// 페이지 로드 시 WebSocket 연결
document.addEventListener('DOMContentLoaded', () => {
    if (AuthManager.isLoggedIn()) {
        window.webSocketManager.connect();
    }
});

// 페이지 언로드 시 WebSocket 연결 해제
window.addEventListener('beforeunload', () => {
    window.webSocketManager.disconnect();
});
