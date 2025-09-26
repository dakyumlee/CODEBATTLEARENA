class WebSocketManager {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
    }
    
    connect() {
        try {
            this.socket = new WebSocket(`ws://${window.location.host}/ws`);
            
            this.socket.onopen = () => {
                console.log('WebSocket 연결됨');
                this.connected = true;
                this.reconnectAttempts = 0;
                this.onConnect();
            };
            
            this.socket.onmessage = (event) => {
                const data = JSON.parse(event.data);
                this.onMessage(data);
            };
            
            this.socket.onclose = () => {
                console.log('WebSocket 연결 끊어짐');
                this.connected = false;
                this.onDisconnect();
                this.attemptReconnect();
            };
            
            this.socket.onerror = (error) => {
                console.error('WebSocket 에러:', error);
                this.onError(error);
            };
            
        } catch (error) {
            console.error('WebSocket 연결 실패:', error);
        }
    }
    
    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
            this.connected = false;
        }
    }
    
    send(message) {
        if (this.connected && this.socket) {
            this.socket.send(JSON.stringify(message));
        }
    }
    
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => this.connect(), this.reconnectDelay);
        }
    }
    
    onConnect() {}
    onMessage(data) {}
    onDisconnect() {}
    onError(error) {}
}
