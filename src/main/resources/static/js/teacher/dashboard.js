let stompClient = null;
let onlineStudents = new Set();

document.addEventListener('DOMContentLoaded', function() {
    loadStudents();
    connectWebSocket();
    updateStudentCount();
});

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('강사 WebSocket 연결됨: ' + frame);
        
        stompClient.subscribe('/topic/activities', function(message) {
            const activity = JSON.parse(message.body);
            handleStudentActivity(activity);
        });
        
        stompClient.subscribe('/topic/notifications', function(message) {
            const notification = JSON.parse(message.body);
            console.log('알림 수신:', notification);
        });
    }, function(error) {
        console.error('WebSocket 연결 실패:', error);
    });
}

async function loadStudents() {
    try {
        const response = await fetch('/api/teacher/students', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        if (!response.ok) {
            throw new Error('학생 데이터를 불러올 수 없습니다');
        }
        
        const data = await response.json();
        displayStudents(data.students || []);
        
    } catch (error) {
        console.error('학생 로드 오류:', error);
        document.getElementById('studentsGrid').innerHTML = 
            '<div class="no-students">학생 데이터를 불러올 수 없습니다.</div>';
    }
}

function displayStudents(students) {
    const studentsGrid = document.getElementById('studentsGrid');
    
    if (!students || students.length === 0) {
        studentsGrid.innerHTML = '<div class="no-students">등록된 학생이 없습니다.</div>';
        return;
    }
    
    onlineStudents.clear();
    
    studentsGrid.innerHTML = students.map(student => {
        const isOnline = student.online || false;
        if (isOnline) {
            onlineStudents.add(student.id);
        }
        
        const statusClass = isOnline ? 'online' : 'offline';
        const statusText = isOnline ? '온라인' : '오프라인';
        const activity = isOnline ? '학습 중' : '접속하지 않음';
        
        return `
            <div class="student-card ${statusClass}">
                <div class="student-info">
                    <div class="student-status ${statusClass}"></div>
                    <div class="student-details">
                        <h4>${student.name}</h4>
                        <p>${student.email}</p>
                    </div>
                </div>
                <div class="student-activity">
                    <span class="status-text">${statusText}</span>
                    <span class="activity-text">${activity}</span>
                </div>
                <div class="student-actions">
                    <button onclick="viewStudent(${student.id})" class="btn-view">상세보기</button>
                </div>
            </div>
        `;
    }).join('');
    
    updateStudentCount();
}

function handleStudentActivity(activity) {
    const { userId, activityType, online } = activity;
    
    if (online !== undefined) {
        if (online) {
            onlineStudents.add(userId);
        } else {
            onlineStudents.delete(userId);
        }
        updateStudentCount();
        updateStudentStatus(userId, online, activityType);
    }
}

function updateStudentStatus(userId, isOnline, activityType) {
    const studentCard = document.querySelector(`[onclick="viewStudent(${userId})"]`)?.closest('.student-card');
    if (!studentCard) return;
    
    const statusIndicator = studentCard.querySelector('.student-status');
    const statusText = studentCard.querySelector('.status-text');
    const activityText = studentCard.querySelector('.activity-text');
    
    if (isOnline) {
        studentCard.className = 'student-card online';
        statusIndicator.className = 'student-status online';
        statusText.textContent = '온라인';
        
        const activityMap = {
            'PAGE_VIEW': '페이지 이동',
            'PROBLEM_SOLVE': '문제 풀이',
            'AI_CHAT': 'AI 튜터 이용',
            'NOTE_WRITE': '노트 작성',
            'MATERIAL_VIEW': '자료 열람'
        };
        
        activityText.textContent = activityMap[activityType] || '학습 중';
    } else {
        studentCard.className = 'student-card offline';
        statusIndicator.className = 'student-status offline';
        statusText.textContent = '오프라인';
        activityText.textContent = '접속하지 않음';
    }
}

function updateStudentCount() {
    const onlineCount = onlineStudents.size;
    const totalCount = document.querySelectorAll('.student-card').length;
    
    document.getElementById('onlineCount').textContent = onlineCount;
    document.getElementById('totalCount').textContent = totalCount;
    
    const connectionStatus = document.getElementById('connectionStatus');
    if (connectionStatus) {
        connectionStatus.textContent = `${onlineCount}/${totalCount}명 접속 중`;
        connectionStatus.className = `connection-indicator ${onlineCount > 0 ? 'active' : ''}`;
    }
}

function viewStudent(studentId) {
    alert(`학생 ID ${studentId}의 상세 정보를 표시합니다.`);
}

function sendNotification() {
    const message = prompt('학생들에게 보낼 메시지를 입력하세요:');
    if (message && stompClient && stompClient.connected) {
        stompClient.send('/app/notification', {}, JSON.stringify({
            type: 'ANNOUNCEMENT',
            message: message,
            from: 'TEACHER'
        }));
        alert('알림이 전송되었습니다.');
    }
}
