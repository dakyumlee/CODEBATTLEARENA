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
                            📊 ${formatFileSize(material.fileSize || 0)}
                        </div>
                        <div style="display: flex; gap: 10px; margin-top: 15px;">
                            <button class="btn btn-primary" onclick="previewMaterial(${material.id})" style="flex: 1;">
                                미리보기
                            </button>
                            <button class="btn btn-secondary" onclick="downloadMaterial(${material.id})" style="flex: 1;">
                                다운로드
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } else {
        container.innerHTML = '<div class="empty-state">공유된 자료가 없습니다</div>';
    }
}

function downloadMaterial(materialId) {
    window.open(`/api/materials/${materialId}/download`, '_blank');
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}
