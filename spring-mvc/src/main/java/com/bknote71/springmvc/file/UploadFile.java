package com.bknote71.springmvc.file;

import lombok.Data;

@Data
public class UploadFile {
    private String uploadFileName;
    private String storeFileName;
    private String fullPath;
    private Long size;

    public UploadFile(String org, String sto, String fullPath) {
        this.uploadFileName = org;
        this.storeFileName = sto;
        this.fullPath = fullPath;
    }

    public UploadFile(String uploadFileName, String storeFileName, String fullPath, Long size) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
        this.fullPath = fullPath;
        this.size = size;
    }
}
