
@Column(name = "file_path")
private String filePath;

@Column(name = "file_name") 
private String fileName;

@Column(name = "file_size")
private Long fileSize;

@Column(name = "file_type")
private String fileType;

@Column(name = "cloudinary_public_id")
private String cloudinaryPublicId;

// getter, setter 메소드들
public String getFilePath() { return filePath; }
public void setFilePath(String filePath) { this.filePath = filePath; }

public String getFileName() { return fileName; }
public void setFileName(String fileName) { this.fileName = fileName; }

public Long getFileSize() { return fileSize; }
public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

public String getFileType() { return fileType; }
public void setFileType(String fileType) { this.fileType = fileType; }

public String getCloudinaryPublicId() { return cloudinaryPublicId; }
public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
