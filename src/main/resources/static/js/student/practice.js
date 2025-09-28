let currentProblemId = null;

const codeTemplates = {
    java: `public class Solution {
    public static void main(String[] args) {
        // 여기에 코드를 작성하세요
        
    }
}`,
    python: `def solve():
    # 여기에 코드를 작성하세요
    pass`,
    javascript: `function solve() {
    // 여기에 코드를 작성하세요
    
}`,
    cpp: `#include <iostream>
using namespace std;

int main() {
    // 여기에 코드를 작성하세요
    
    return 0;
}`
};

async function loadProblems() {
    try {
        const response = await fetch('/api/practice/problems');
        const data = await response.json();
        
        if (data.problems && data.problems.length > 0) {
            displayProblems(data.problems);
        } else {
            document.getElementById('problemsContainer').innerHTML = '<div class="empty-state">문제가 없습니다.</div>';
        }
    } catch (error) {
        console.error('문제 로드 실패:', error);
        document.getElementById('problemsContainer').innerHTML = '<div class="empty-state">문제를 불러올 수 없습니다.</div>';
    }
}

function displayProblems(problems) {
    const container = document.getElementById('problemsContainer');
    
    container.innerHTML = problems.map(problem => `
        <div class="problem-card" data-difficulty="${problem.difficulty}">
            <div class="problem-title">${problem.title}</div>
            <div class="problem-description">${problem.description.substring(0, 100)}...</div>
            <div class="problem-info">
                <span class="problem-badge badge-difficulty-${problem.difficulty}">${problem.difficulty}</span>
                <span class="problem-badge">난이도</span>
            </div>
            <div class="problem-status">
                <button class="btn btn-primary" onclick="startProblem('${problem.id}', '${problem.title}', \`${problem.description}\`)">도전하기</button>
                ${problem.solved ? '<span class="btn btn-success">해결완료</span>' : ''}
            </div>
        </div>
    `).join('');
}

function startProblem(problemId, title, description) {
    currentProblemId = problemId;
    
    document.getElementById('selectedProblemTitle').textContent = title;
    document.getElementById('selectedProblemDescription').textContent = description;
    
    const container = document.getElementById('codeEditorContainer');
    container.classList.add('active');
    
    loadTemplate();
    
    container.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeProblem() {
    currentProblemId = null;
    const container = document.getElementById('codeEditorContainer');
    container.classList.remove('active');
    
    document.getElementById('selectedProblemTitle').textContent = '문제를 선택하세요';
    document.getElementById('selectedProblemDescription').textContent = '위에서 문제를 선택하면 여기에 문제 설명이 나타납니다.';
    document.getElementById('codeEditor').value = '';
    document.getElementById('outputContainer').innerHTML = '<span class="output-info">코드를 실행하려면 위의 \'실행\' 버튼을 클릭하세요.</span>';
}

function filterByDifficulty(difficulty) {
    document.querySelectorAll('.difficulty-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    const cards = document.querySelectorAll('.problem-card');
    cards.forEach(card => {
        const cardDifficulty = card.getAttribute('data-difficulty');
        if (difficulty === 'all' || cardDifficulty === difficulty) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

function loadTemplate() {
    const language = document.getElementById('languageSelect').value;
    document.getElementById('codeEditor').value = codeTemplates[language];
}

function clearCode() {
    document.getElementById('codeEditor').value = '';
    document.getElementById('outputContainer').innerHTML = '<span class="output-info">코드를 실행하려면 위의 \'실행\' 버튼을 클릭하세요.</span>';
}

async function runCode() {
    const code = document.getElementById('codeEditor').value;
    const output = document.getElementById('outputContainer');
    
    if (!code.trim()) {
        output.innerHTML = '<span class="output-error">코드를 입력해주세요.</span>';
        return;
    }
    
    if (!currentProblemId) {
        output.innerHTML = '<span class="output-error">문제를 먼저 선택해주세요.</span>';
        return;
    }
    
    output.innerHTML = '<span class="output-info">코드를 실행 중...</span>';
    
    try {
        const response = await fetch('/api/practice/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                problemId: currentProblemId,
                code: code
            })
        });
        
        const result = await response.json();
        
        if (result.correct) {
            output.innerHTML = `<span class="output-success">정답입니다! 🎉\n\n점수: ${result.score}점\n실행시간: ${result.executionTime}\n\n피드백: ${result.feedback}</span>`;
        } else {
            output.innerHTML = `<span class="output-error">틀렸습니다.\n\n점수: ${result.score}점\n\n피드백: ${result.feedback}</span>`;
        }
    } catch (error) {
        output.innerHTML = '<span class="output-error">코드 실행 중 오류가 발생했습니다.</span>';
    }
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}

document.getElementById('languageSelect').addEventListener('change', function() {
    if (currentProblemId && confirm('언어를 변경하면 현재 코드가 초기화됩니다. 계속하시겠습니까?')) {
        loadTemplate();
    }
});

document.addEventListener('DOMContentLoaded', function() {
    loadProblems();
});