// class.html의 미리보기 함수를 이것으로 교체
function previewMaterial(id, title, fileType) {
    const previewUrl = `/api/teacher/materials/${id}/preview`;
    window.open(previewUrl, '_blank', 'width=1000,height=800,scrollbars=yes,resizable=yes');
}

// 다운로드 함수도 수정
function downloadMaterial(id) {
    window.open(`/api/teacher/materials/${id}/download-fixed`, '_blank');
}
