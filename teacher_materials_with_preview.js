function displayMaterials(materials) {
    const container = document.getElementById('materialsContainer');
    
    if (materials && materials.length > 0) {
        container.innerHTML = `
            <div class="materials-grid">
                ${materials.map(material => `
                    <div class="material-card">
                        <div class="material-title">${material.title}</div>
                        <div class="material-info">
                            📄 ${material.fileType ? material.fileType.toUpperCase() : 'FILE'}<br>
                            📅 ${new Date(material.createdAt).toLocaleDateString()}<br>
                            📊 ${formatFileSize(material.fileSize || 0)}<br>
                            📥 다운로드: ${material.downloadCount || 0}회
                        </div>
                        <div style="display: flex; gap: 8px; margin-top: 15px; flex-wrap: wrap;">
                            <button class="btn btn-primary" onclick="previewMaterial(${material.id})" style="flex: 1; min-width: 80px;">
                                미리보기
                            </button>
                            <button class="btn btn-secondary" onclick="downloadMaterial(${material.id})" style="flex: 1; min-width: 80px;">
                                다운로드
                            </button>
                            <button class="btn btn-danger" onclick="deleteMaterial(${material.id})" style="flex: 1; min-width: 60px;">
                                삭제
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } else {
        container.innerHTML = '<div class="empty-state">업로드된 자료가 없습니다</div>';
    }
}

async function deleteMaterial(materialId) {
    if (!confirm('정말 이 자료를 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/api/teacher/materials/${materialId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = '/';
            return;
        }

        const result = await response.json();

        if (result.success) {
            alert('자료가 삭제되었습니다.');
            loadMaterials();
        } else {
            alert('삭제 실패: ' + result.message);
        }
    } catch (error) {
        alert('오류가 발생했습니다: ' + error.message);
    }
}

function downloadMaterial(materialId) {
    window.open(`/api/teacher/materials/${materialId}/download`, '_blank');
}
