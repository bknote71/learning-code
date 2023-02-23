package com.bknote71.springmvc.file.image;

import com.bknote71.springmvc.file.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
