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
        this.subscribe('/topic/notifications', (message) => {
            this.handleNotification(JSON.parse(message.body));
        });

        this.subscribe('/topic/activity', (message) => {
            this.handleActivityUpdate(JSON.parse(message.body));
        });

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

    sendStudentActivity(activity, page) {
        this.send('/app/student-activity', {
            userId: AuthManager.getCurrentUserId(),
            userName: AuthManager.getCurrentUserName(),
            activity: activity,
            page: page,
            timestamp: new Date().toISOString()
        });
    }

    sendNotification(message) {
        this.send('/app/notification', {
            message: message,
            sender: AuthManager.getCurrentUserName(),
            timestamp: new Date().toISOString(),
            type: 'announcement'
        });
    }

    handleNotification(notification) {
        console.log('알림 수신:', notification);
        this.showNotificationPopup(notification.message, notification.type);
        window.dispatchEvent(new CustomEvent('websocket-notification', { 
            detail: notification 
        }));
    }

    handleActivityUpdate(activity) {
        console.log('활동 업데이트:', activity);
        window.dispatchEvent(new CustomEvent('student-activity-update', { 
            detail: activity 
        }));
    }

    handleBattleUpdate(battleData) {
        console.log('배틀 업데이트:', battleData);
        window.dispatchEvent(new CustomEvent('battle-update', { 
            detail: battleData 
        }));
    }

    showNotificationPopup(message, type = 'info') {
        const existingNotification = document.getElementById('websocket-notification');
        if (existingNotification) {
            existingNotification.remove();
        }

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

        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }
}

window.webSocketManager = new WebSocketManager();

document.addEventListener('DOMContentLoaded', () => {
    if (AuthManager.isLoggedIn()) {
        window.webSocketManager.connect();
    }
});

window.addEventListener('beforeunload', () => {
    window.webSocketManager.disconnect();
});
