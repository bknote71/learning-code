package com.bknote71.springmvc.file.image;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/files")
@Controller
public class ImageFilesController {

    private final ImageService imageService;
    @GetMapping("/")
    public @ResponseBody String fileHome() {
        return "file-home";
    }

    @PostMapping("/transfer/image")
    public ResponseEntity<?> transferImgFile(@RequestParam("file") MultipartFile file) throws IOException {
        Image image = imageService.storeImageFile(file);
        return ResponseEntity.of(Optional.ofNullable(image));
    }

    // Resource 로 전송 + produce에 아무것도 설정 X: binary 파일을 화면에 보여준다.
    @GetMapping(
            value = "/image/binary/{fileId}"
    )
    public @ResponseBody Resource getBinaryFileView(@PathVariable Long fileId) throws MalformedURLException {
        Resource resource = imageService.loadImageResource(fileId);
        return resource;
    }

    // byte[] 로 전송하기
    // 다운로드 image raw file: produce = APPLICATION_OCTET_STREAM_VALUE
    @GetMapping(
            value = "/image/raw/{fileId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody ResponseEntity<byte[]> downloadRawImageFile(@PathVariable Long fileId) throws IOException {
        Image image = imageService.loadImage(fileId);
        // fileInputStream은 "file:" << 써줄필요 없다.
        InputStream is = new FileInputStream(image.getFullPath());
        Assert.notNull(is, "해당 경로에는 파일이 존재하지 않습니다.");
        return ResponseEntity.ok(is.readAllBytes());
    }
    @GetMapping(
            value = "/image/{fileId}",
            // 이미지 파일을 화면에 전송하기 위해서는 produces 에 명시적인 이미지 컨텐츠 타입을 사용해야 한다.
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public @ResponseBody Resource getImageView(@PathVariable Long fileId) throws MalformedURLException {
        Resource resource = imageService.loadImageResource(fileId);
        return resource;
    }

    // 다운로드 image 첨부 파잁 << not raw file
    @GetMapping("/image/attach/{fileId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long fileId) throws MalformedURLException {
        Image image = imageService.loadImage(fileId);
        UrlResource resource = new UrlResource(image.getUrl());
        // url encoding 필요
        String filename = image.getFilename();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
