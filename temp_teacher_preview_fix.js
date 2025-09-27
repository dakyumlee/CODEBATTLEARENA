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
                        ${canPreview(material.fileType) ? 
                            `<button class="btn btn-warning btn-small" onclick="previewMaterial(${material.id}, '${material.title}', '${material.filePath}', '${material.fileType}')">미리보기</button>` : 
                            ''
                        }
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

function canPreview(fileType) {
    const previewableTypes = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg'];
    return previewableTypes.includes(fileType?.toLowerCase());
}

function previewMaterial(id, title, filePath, fileType) {
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed; top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0,0,0,0.9); display: flex; align-items: center; justify-content: center;
        z-index: 10000; backdrop-filter: blur(10px);
    `;
    
    const content = document.createElement('div');
    content.style.cssText = `
        background: white; border-radius: 12px; padding: 20px;
        max-width: 90%; max-height: 90%; overflow: auto;
        position: relative;
    `;
    
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '✕';
    closeBtn.style.cssText = `
        position: absolute; top: 10px; right: 15px; background: none;
        border: none; font-size: 24px; cursor: pointer; z-index: 1;
    `;
    closeBtn.onclick = () => document.body.removeChild(modal);
    
    const titleDiv = document.createElement('h3');
    titleDiv.textContent = title;
    titleDiv.style.cssText = 'margin-bottom: 15px; color: #333;';
    
    let previewElement;
    if (fileType.toLowerCase() === 'pdf') {
        previewElement = document.createElement('iframe');
        previewElement.src = filePath;
        previewElement.style.cssText = 'width: 100%; height: 600px; border: none;';
    } else {
        previewElement = document.createElement('img');
        previewElement.src = filePath;
        previewElement.style.cssText = 'max-width: 100%; height: auto;';
    }
    
    content.appendChild(closeBtn);
    content.appendChild(titleDiv);
    content.appendChild(previewElement);
    modal.appendChild(content);
    document.body.appendChild(modal);
    
    modal.onclick = (e) => {
        if (e.target === modal) document.body.removeChild(modal);
    };
}
