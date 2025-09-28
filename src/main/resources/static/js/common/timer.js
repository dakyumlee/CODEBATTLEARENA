class ProblemTimer {
    constructor(timeLimit, onTimeout, onTick) {
        this.timeLimit = timeLimit; // 분 단위
        this.totalSeconds = timeLimit * 60;
        this.remainingSeconds = this.totalSeconds;
        this.startTime = new Date();
        this.onTimeout = onTimeout;
        this.onTick = onTick;
        this.interval = null;
        this.isRunning = false;
    }

    start() {
        if (this.isRunning) return;
        
        this.isRunning = true;
        this.interval = setInterval(() => {
            this.remainingSeconds--;
            
            if (this.onTick) {
                this.onTick(this.remainingSeconds, this.formatTime(this.remainingSeconds));
            }
            
            if (this.remainingSeconds <= 0) {
                this.stop();
                if (this.onTimeout) {
                    this.onTimeout();
                }
            }
        }, 1000);
    }

    stop() {
        if (this.interval) {
            clearInterval(this.interval);
            this.interval = null;
        }
        this.isRunning = false;
    }

    getTimeSpent() {
        return Math.floor((this.totalSeconds - this.remainingSeconds) / 60); // 분 단위 반환
    }

    formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    }

    isExpired() {
        return this.remainingSeconds <= 0;
    }
}

// 전역에서 사용할 수 있도록 export
window.ProblemTimer = ProblemTimer;
