package com.bknote71.springmvc.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MyFileSystem {
    public static UploadFile save(String fileDir, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String org = file.getOriginalFilename();
        String sto = createStoreFileName(org);
        String fullPath = fileDir + sto;
        Resource resource = new UrlResource("file:" +fullPath);
        file.transferTo(new File(fullPath));
        return new UploadFile(org, sto, fullPath, resource.contentLength());
    }

    private static String createStoreFileName(String org) {
        int idx = org.lastIndexOf(".");
        String ext = org.substring(idx + 1);
        String id = UUID.randomUUID().toString();
        return id + "." + ext;
    }
}
