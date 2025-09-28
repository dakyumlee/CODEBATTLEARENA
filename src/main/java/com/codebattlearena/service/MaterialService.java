package com.codebattlearena.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codebattlearena.model.Material;
import com.codebattlearena.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    private final Cloudinary cloudinary;

    public MaterialService() {
        this.cloudinary = new Cloudinary(System.getenv("CLOUDINARY_URL"));
    }

    public Material uploadMaterial(MultipartFile file, String title, String description, Long teacherId, Long groupId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String safePublicId = "materials/" + UUID.randomUUID().toString() + fileExtension;
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
            ObjectUtils.asMap(
                "public_id", safePublicId,
                "resource_type", "raw"
            )
        );
        
        Material material = new Material();
        material.setTitle(title);
        material.setDescription(description);
        material.setTeacherId(teacherId);
        material.setGroupId(groupId);
        material.setOriginalFilename(originalFilename);
        material.setFilePath((String) uploadResult.get("secure_url"));
        material.setCloudinaryPublicId((String) uploadResult.get("public_id"));
        material.setFileType(getFileType(originalFilename));
        material.setFileSize(file.getSize());
        material.setCreatedAt(LocalDateTime.now());
        
        return materialRepository.save(material);
    }
    
    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        
        String extension = filename.toLowerCase();
        if (extension.endsWith(".pdf")) return "pdf";
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) return "jpg";
        if (extension.endsWith(".png")) return "png";
        if (extension.endsWith(".gif")) return "gif";
        if (extension.endsWith(".doc") || extension.endsWith(".docx")) return "doc";
        if (extension.endsWith(".ppt") || extension.endsWith(".pptx")) return "ppt";
        if (extension.endsWith(".xls") || extension.endsWith(".xlsx")) return "xls";
        
        return "unknown";
    }
}
