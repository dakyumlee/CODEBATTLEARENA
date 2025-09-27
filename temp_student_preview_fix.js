// today.html의 displayMaterials 함수 수정
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
                            📅 ${new Date(material.createdAt).toLocaleDateString()}
                        </div>
                        <div style="display: flex; gap: 10px; margin-top: 15px;">
                            ${canPreview(material.fileType) ? 
                                `<button class="btn btn-warning" style="padding: 8px 16px; font-size: 0.8rem;" onclick="previewMaterial(${material.id}, '${material.title}', '${material.filePath}', '${material.fileType}')">미리보기</button>` : 
                                ''
                            }
                            <button class="btn btn-primary" style="padding: 8px 16px; font-size: 0.8rem;" onclick="downloadMaterial(${material.id})">
                                다운로드
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } else {
        container.innerHTML = '<div class="empty-state">공유된 자료가 없습니다.</div>';
    }
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
        background: linear-gradient(135deg, #1a2332 0%, #2d3748 100%);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 20px; padding: 30px;
        max-width: 90%; max-height: 90%; overflow: auto;
        position: relative;
    `;
    
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '✕';
    closeBtn.style.cssText = `
        position: absolute; top: 15px; right: 20px; background: none;
        border: none; font-size: 24px; cursor: pointer; z-index: 1;
        color: #4fc3f7; font-weight: bold;
    `;
    closeBtn.onclick = () => document.body.removeChild(modal);
    
    const titleDiv = document.createElement('h3');
    titleDiv.textContent = title;
    titleDiv.style.cssText = 'margin-bottom: 20px; color: #4fc3f7; font-size: 1.3rem;';
    
    let previewElement;
    if (fileType.toLowerCase() === 'pdf') {
        previewElement = document.createElement('iframe');
        previewElement.src = filePath;
        previewElement.style.cssText = 'width: 100%; height: 600px; border: 1px solid rgba(255,255,255,0.2); border-radius: 8px;';
    } else {
        previewElement = document.createElement('img');
        previewElement.src = filePath;
        previewElement.style.cssText = 'max-width: 100%; height: auto; border-radius: 8px;';
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
