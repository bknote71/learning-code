package com.bknote71.springmvc.file.video;

import com.bknote71.springmvc.file.image.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/files")
@Controller
public class VideoFilesController {

    private final VideoService videoService;

    @PostMapping("/transfer/video")
    public ResponseEntity<?> transferImgFile(@RequestParam("file") MultipartFile file) throws IOException {
        Video video = videoService.storeVideoFile(file);
        return ResponseEntity.of(Optional.ofNullable(video));
    }

    @GetMapping(
            value = "/video/binary/{fileId}"
    )
    public @ResponseBody Resource getBinaryFileView(@PathVariable Long fileId) throws MalformedURLException {
        Resource resource = videoService.loadVideoResource(fileId);
        return resource;
    }

    // spring은 Resource 타입이 반환타입이면 응답 헤더에 ACCEPT_RANGE를 추가해준다.
    // 그렇기 때문에 용량이 큰 파일을 나눠서 받을 수 있게 한다.
    // 응답 헤더:
    // Accept-Ranges: bytes
    // Content-Range: bytes 0-13927645/13927646
    // 동영상이 진행되면 Content-Range 를 달리해서 다른 부분을 가져오게끔 한다!!
    // https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodProcessor.java#L187
    @GetMapping(
            value = "/video/{fileId}",
            produces = "video/mp4" // produces: request에서는 accept, response에서는 content-type
    )
    public @ResponseBody Resource getVideoView(@PathVariable Long fileId) throws MalformedURLException {
        Resource resource = videoService.loadVideoResource(fileId);
        return resource;
    }

    @GetMapping(
            value = "/video/stream/{fileId}"
    )
    public ResponseEntity<StreamingResponseBody> getVideoStreamView(@PathVariable Long fileId) {
        Video video = videoService.loadVideo(fileId);
        StreamingResponseBody body = os -> {
            InputStream is = new FileInputStream(video.getFilePath());
            byte[] bytes = new byte[Math.toIntExact(video.getSize())];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                os.write(bytes, 0, length);
            }
            is.close();
            os.flush();
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        headers.add("Content-Length", Long.toString(video.getSize()));
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    @GetMapping(
            value = "/video/stream2/{fileId}"
    )
    public ResponseEntity<StreamingResponseBody> getVideoStream2View(@PathVariable Long fileId) {
        Video video = videoService.loadVideo(fileId);
        StreamingResponseBody body = os -> FileCopyUtils.copy(new FileInputStream(video.getFilePath()), os);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        headers.add("Content-Length", Long.toString(video.getSize()));
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    // 미디어나 파일 다운로드 같이 용량이 큰 경우 일부만 다운로드할 수 있도록 Accept header에 range를 지정할 수 있다.
    // Accept-Ranges: bytes
    // or
    // Range: bytes=200-1000, 2000-6576, 19000- << 다중 부분 범위 요청이 가능, 따라서 List<ResourceRegion> 반환하는 것이 좋다.
    // 응답:
    // - Content-Range: bytes 2-4/6

    @GetMapping(
            value = "/video/range/{fileId}"
    )
    public ResponseEntity<List<ResourceRegion>> getVideoRangeView(
            @PathVariable Long fileId,
            @RequestHeader HttpHeaders httpHeaders
    ) throws MalformedURLException {
        Resource resource = videoService.loadVideoResource(fileId);
        // spring 이 range에 대한 설정을 계산하는 메서드를 제공한다.
        List<ResourceRegion> resourceRegions = HttpRange.toResourceRegions(httpHeaders.getRange(), resource);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resourceRegions);
    }

    // 다운로드 video 첨부 파잁
    @GetMapping("/video/attach/{fileId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long fileId) throws MalformedURLException {
        Video video = videoService.loadVideo(fileId);
        UrlResource resource = new UrlResource("file:" + video.getFilePath());
        // url encoding 필요
        String filename = video.getFilename();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
