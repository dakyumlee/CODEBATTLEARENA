class WebSocketValidator {
    static checkConnection() {
        return new Promise((resolve) => {
            if (!window.webSocketManager) {
                console.error('WebSocket Manager가 초기화되지 않았습니다');
                resolve(false);
                return;
            }

            if (!window.webSocketManager.isConnected()) {
                console.warn('WebSocket 연결이 끊어져 있습니다. 재연결 시도 중...');
                window.webSocketManager.connect();
                
                setTimeout(() => {
                    resolve(window.webSocketManager.isConnected());
                }, 3000);
            } else {
                resolve(true);
            }
        });
    }

    static addConnectionStatusIndicator() {
        if (document.getElementById('ws-status-indicator')) {
            return; // 이미 존재함
        }

        const indicator = document.createElement('div');
        indicator.id = 'ws-status-indicator';
        indicator.style.cssText = `
            position: fixed;
            top: 10px;
            left: 50%;
            transform: translateX(-50%);
            padding: 0.5rem 1rem;
            border-radius: 0.25rem;
            font-size: 0.8rem;
            z-index: 1000;
            transition: all 0.3s ease;
        `;
        
        document.body.appendChild(indicator);
        this.updateConnectionStatus();
        
        setInterval(() => {
            this.updateConnectionStatus();
        }, 5000);
    }

    static updateConnectionStatus() {
        const indicator = document.getElementById('ws-status-indicator');
        if (!indicator) return;

        const isConnected = window.webSocketManager && window.webSocketManager.isConnected();
        
        if (isConnected) {
            indicator.textContent = '실시간 연결됨';
            indicator.style.background = '#10b981';
            indicator.style.color = 'white';
            indicator.style.display = 'none'; // 연결되면 숨김
        } else {
            indicator.textContent = '실시간 연결 끊어짐 - 재연결 중...';
            indicator.style.background = '#ef4444';
            indicator.style.color = 'white';
            indicator.style.display = 'block'; // 연결 안되면 표시
        }
    }

    static setupNotificationHandler() {
        if (this.notificationHandlerSetup) {
            return;
        }

        window.addEventListener('websocket-notification', (event) => {
            const notification = event.detail;
            console.log('WebSocket 알림 수신:', notification);
            
            // 페이지별 알림 처리
            this.handlePageSpecificNotification(notification);
        });

        this.notificationHandlerSetup = true;
    }

    static handlePageSpecificNotification(notification) {
        const currentPath = window.location.pathname;
        
        if (currentPath.includes('/student/today')) {
            this.handleTodayPageNotification(notification);
        } else if (currentPath.includes('/student/practice')) {
            this.handlePracticePageNotification(notification);
        } else if (currentPath.includes('/student/notes')) {
            this.handleNotesPageNotification(notification);
        }
    }

    static handleTodayPageNotification(notification) {
        if (notification.type === 'new_material' && typeof loadSharedMaterials === 'function') {
            loadSharedMaterials();
        } else if (notification.type === 'new_problem' && typeof updateTeacherProblems === 'function') {
            updateTeacherProblems(notification.problem);
        }
    }

    static handlePracticePageNotification(notification) {
        if (notification.type === 'new_problem') {
            this.showNotification('새로운 연습 문제가 추가되었습니다');
        }
    }

    static handleNotesPageNotification(notification) {
        // 노트 페이지는 개인적인 공간이므로 특별한 처리 없음
    }

    static showNotification(message) {
        const toast = document.createElement('div');
        toast.className = 'ws-notification-toast';
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
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 5000);
    }

    static initialize() {
        this.addConnectionStatusIndicator();
        this.setupNotificationHandler();
        
        // WebSocket이 아직 로드되지 않았다면 기다림
        const checkWebSocket = () => {
            if (window.webSocketManager) {
                this.checkConnection();
            } else {
                setTimeout(checkWebSocket, 1000);
            }
        };
        checkWebSocket();
    }
}

// CSS 애니메이션 추가
if (!document.getElementById('ws-animations')) {
    const style = document.createElement('style');
    style.id = 'ws-animations';
    style.textContent = `
        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
    `;
    document.head.appendChild(style);
}
