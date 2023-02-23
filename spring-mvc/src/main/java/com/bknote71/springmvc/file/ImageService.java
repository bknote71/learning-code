package com.bknote71.springmvc.file;

import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

@RequiredArgsConstructor
@Service
public class ImageService {
    public static final String LOCATION = "/Users/bknote71/repository/learning-code/spring-mvc/img/";
    private final ImageRepository imageRepository;

    public Image storeImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("empty file");
        }

        UploadFile uploadedFile = MyFileSystem.save(LOCATION, file);
        Image image = new Image(
                uploadedFile.getUploadFileName(),
                uploadedFile.getStoreFileName(),
                uploadedFile.getSize(),
                uploadedFile.getFullPath()
                );
        Image savedImg = imageRepository.save(image);
        return savedImg;
    }

    public Resource loadImageResource(Long id) throws MalformedURLException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalIdentifierException("id에 대응하는 이미지 파일이 없습니다."));
        return new UrlResource("file:" + image.getFullPath());
    }

    public Image loadImage(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new IllegalIdentifierException("id에 대응하는 이미지 파일이 없습니다."));
    }

}
