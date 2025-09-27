class StudentWebSocket {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.createModal();
    }

    connectWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, 
            (frame) => {
                this.connected = true;
                this.stompClient.subscribe('/topic/notifications', (message) => {
                    const notification = JSON.parse(message.body);
                    this.showNotificationModal(notification);
                });
            }, 
            () => {
                this.connected = false;
                setTimeout(() => this.connectWebSocket(), 5000);
            }
        );
    }

    createModal() {
        if (document.getElementById('globalNotificationModal')) return;
        
        const modalHtml = `
            <div id="globalNotificationModal" class="global-notification-modal">
                <div class="modal-content">
                    <h3 id="globalModalTitle" class="modal-title">새 알림</h3>
                    <p id="globalModalMessage" class="modal-message"></p>
                    <div class="modal-buttons">
                        <button id="globalModalAccept" class="btn-modal primary">확인하기</button>
                        <button id="globalModalClose" class="btn-modal secondary">나중에</button>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        this.addModalStyles();
    }

    addModalStyles() {
        if (document.getElementById('global-modal-styles')) return;
        
        const style = document.createElement('style');
        style.id = 'global-modal-styles';
        style.textContent = `
            .global-notification-modal {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.8);
                display: none;
                z-index: 10000;
                align-items: center;
                justify-content: center;
            }

            .global-notification-modal .modal-content {
                background: #2d3748;
                padding: 2rem;
                border-radius: 1rem;
                max-width: 400px;
                width: 90%;
                text-align: center;
                border: 2px solid #4299e1;
                animation: modalSlideIn 0.3s ease-out;
            }

            @keyframes modalSlideIn {
                from {
                    transform: translateY(-50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .global-notification-modal .modal-title {
                color: #4299e1;
                font-size: 1.5rem;
                font-weight: 700;
                margin-bottom: 1rem;
            }

            .global-notification-modal .modal-message {
                color: #e2e8f0;
                margin-bottom: 2rem;
                line-height: 1.5;
            }

            .global-notification-modal .modal-buttons {
                display: flex;
                gap: 1rem;
                justify-content: center;
            }

            .global-notification-modal .btn-modal {
                padding: 0.75rem 1.5rem;
                border: none;
                border-radius: 0.5rem;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.2s;
            }

            .global-notification-modal .btn-modal.primary {
                background: #4299e1;
                color: white;
            }

            .global-notification-modal .btn-modal.secondary {
                background: #4a5568;
                color: #e2e8f0;
            }

            .global-notification-modal .btn-modal:hover {
                transform: translateY(-1px);
            }
        `;
        
        document.head.appendChild(style);
    }

    showNotificationModal(notification) {
        const modal = document.getElementById('globalNotificationModal');
        const title = document.getElementById('globalModalTitle');
        const message = document.getElementById('globalModalMessage');
        const acceptBtn = document.getElementById('globalModalAccept');
        const closeBtn = document.getElementById('globalModalClose');

        if (!modal || !title || !message || !acceptBtn || !closeBtn) return;

        title.textContent = notification.title || '새 알림';
        message.textContent = notification.message || '';
        modal.style.display = 'flex';

        const newAcceptBtn = acceptBtn.cloneNode(true);
        const newCloseBtn = closeBtn.cloneNode(true);
        
        acceptBtn.parentNode.replaceChild(newAcceptBtn, acceptBtn);
        closeBtn.parentNode.replaceChild(newCloseBtn, closeBtn);

        newAcceptBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            modal.style.display = 'none';
            
            if (notification.type === 'NEW_PROBLEM') {
                location.assign('/student/today');
            }
        });

        newCloseBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            modal.style.display = 'none';
        });

        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                e.preventDefault();
                e.stopPropagation();
                modal.style.display = 'none';
            }
        });
    }
}

window.studentWebSocket = new StudentWebSocket();