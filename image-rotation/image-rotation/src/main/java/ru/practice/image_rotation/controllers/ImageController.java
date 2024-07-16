package ru.practice.image_rotation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.practice.image_rotation.dto.ImageDTO;
import ru.practice.image_rotation.services.ImageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotateImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("angle") double angle) {
        try {
            byte[] rotatedImageBytes = imageService.rotateImage(file, angle);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "rotated_output.jpg"); // вложение
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(rotatedImageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
