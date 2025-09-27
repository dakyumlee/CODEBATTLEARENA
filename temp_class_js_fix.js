// 기존 openGradeModal 함수 수정
function openGradeModal(submissionId, studentName, problemTitle, problemType, answer) {
    document.getElementById('gradeSubmissionId').value = submissionId;
    document.getElementById('gradeStudentName').value = studentName;
    document.getElementById('gradeProblemTitle').value = problemTitle;
    document.getElementById('gradeProblemType').value = getTypeKorean(problemType);
    document.getElementById('gradeStudentAnswer').value = answer;
    document.getElementById('gradeModal').style.display = 'flex';
}
