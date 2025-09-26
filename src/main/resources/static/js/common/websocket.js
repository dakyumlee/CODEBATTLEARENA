class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 5000;
        this.connect();
    }

    connect() {
        console.log('WebSocket 연결 시도 중...');
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.debug = null;
        
        this.stompClient.connect({}, 
            (frame) => {
                console.log('WebSocket 연결 성공:', frame);
                this.connected = true;
                this.reconnectAttempts = 0;
                this.subscribeToNotifications();
            },
            (error) => {
                console.error('WebSocket 연결 실패:', error);
                this.connected = false;
                this.attemptReconnect();
            }
        );
    }

    subscribeToNotifications() {
        if (this.stompClient && this.connected) {
            this.stompClient.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body);
                console.log('알림 수신:', notification);
                this.handleNotification(notification);
            });
        }
    }

    handleNotification(notification) {
        const event = new CustomEvent('websocket-notification', { 
            detail: notification 
        });
        window.dispatchEvent(event);
        
        this.showToast(notification.message);
    }

    showToast(message) {
        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #10b981;
            color: white;
            padding: 1rem;
            border-radius: 0.5rem;
            z-index: 10000;
            max-width: 300px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            animation: slideInRight 0.3s ease-out;
        `;
        toast.textContent = message;
        
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(style);
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }

    sendNotification(message) {
        if (this.stompClient && this.connected) {
            this.stompClient.send('/app/notification', {}, JSON.stringify({
                message: message,
                timestamp: new Date().toISOString()
            }));
        }
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        } else {
            console.error('WebSocket 재연결 포기');
        }
    }

    isConnected() {
        return this.connected;
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

window.webSocketManager = new WebSocketManager();
