package com.bknote71.springmvcfile.file.video;

import com.bknote71.springmvcfile.file.MyFileSystem;
import com.bknote71.springmvcfile.file.UploadFile;
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
public class VideoService {
    public static final String LOCALFILE = "/Users/bknote71/repository/learning-code/spring-mvc/vdo/";
    private final VideoRepository videoRepository;

    public Video storeVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("empty file");
        }

        UploadFile uploadedFile = MyFileSystem.saveAsLocalFile(LOCALFILE, file);
        Video image = new Video(
                uploadedFile.getUploadFileName(),
                uploadedFile.getStoreFileName(),
                uploadedFile.getSize(),
                uploadedFile.getFullPath()
        );
        Video savedVdo = videoRepository.save(image);
        return savedVdo;
    }

    public Resource loadVideoResource(Long id) throws MalformedURLException {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalIdentifierException("id에 대응하는 이미지 파일이 없습니다."));
        return new UrlResource("file:" + video.getFilePath());
    }

    public Video loadVideo(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new IllegalIdentifierException("id에 대응하는 이미지 파일이 없습니다."));
    }
}
