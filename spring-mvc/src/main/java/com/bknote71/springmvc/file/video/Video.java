package com.bknote71.springmvc.file.video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Video {
    @Id @GeneratedValue
    private Long id;
    private String filename;
    private String storedFileName;
    private Long size; // byte 단위
    private String filePath;

    public Video(String org, String storedFileName, Long size, String filePath) {
        this.filename = org;
        this.storedFileName = storedFileName;
        this.size = size;
        this.filePath = filePath;
    }
}
