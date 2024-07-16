package ru.practice.image_rotation.dto;

import org.springframework.web.multipart.MultipartFile;

public class ImageDTO {
    private MultipartFile file;
    private double angle;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
