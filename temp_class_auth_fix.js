async function loadMaterials() {
    try {
        const response = await fetch('/api/teacher/materials', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        if (response.status === 401) {
            alert('로그인이 만료되었습니다.');
            localStorage.clear();
            window.location.href = '/';
            return;
        }
        
        const data = await response.json();
        
        if (!data.success) {
            if (data.redirect) {
                localStorage.clear();
                window.location.href = data.redirect;
                return;
            }
            throw new Error(data.message);
        }
        
        const container = document.getElementById('materialsContainer');
        
        if (data.materials && data.materials.length > 0) {
            container.innerHTML = `
                <div class="materials-grid">
                    ${data.materials.map(material => `
                        <div class="material-card">
                            <div class="material-title">${material.title}</div>
                            <div class="material-info">
                                크기: ${formatFileSize(material.fileSize)}<br>
                                다운로드: ${material.downloadCount || 0}회<br>
                                업로드: ${new Date(material.createdAt).toLocaleDateString()}
                            </div>
                            <div class="material-actions">
                                <button class="btn btn-primary btn-small" onclick="downloadMaterial(${material.id})">다운로드</button>
                                <button class="btn btn-danger btn-small" onclick="deleteMaterial(${material.id})">삭제</button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } else {
            container.innerHTML = '<div class="empty-state">업로드된 자료가 없습니다.</div>';
        }
    } catch (error) {
        console.error('자료 로드 실패:', error);
        document.getElementById('materialsContainer').innerHTML = '<div class="empty-state">자료를 불러올 수 없습니다.</div>';
    }
}
