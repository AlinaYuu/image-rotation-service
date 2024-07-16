package ru.practice.image_rotation.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.practice.image_rotation.dto.ImageDTO;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {
    // method for rotating an image and converting it into an array of bytes
    public byte[] rotateImage(MultipartFile file, double angle) throws IOException {
        ImageDTO imageDTO = saveImageDTO(file, angle);

        BufferedImage originalImage = readImage(imageDTO.getFile());
        BufferedImage rotatedImage = rotateImageByAngle(originalImage, imageDTO.getAngle());
        return convertBufferedImageToByteArray(rotatedImage);
    }
    // creating an ImageDTO and setting the image and rotation angle
    private ImageDTO saveImageDTO(MultipartFile file, double angle) {
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setFile(file);
        imageDTO.setAngle(angle);
        return imageDTO;
    }

    // reading an image from an uploaded file
    private BufferedImage readImage(MultipartFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }

    // converting BufferedImage to byte array
    private byte[] convertBufferedImageToByteArray(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            baos.flush();
            return baos.toByteArray();
        }
    }

    // converting BufferedImage to byte array
    private BufferedImage rotateImageByAngle(BufferedImage img, double angle) {
        double radians = Math.toRadians(angle);
        int width = img.getWidth();
        int height = img.getHeight();
        int newWidth = calculateNewDimension(width, height, radians, true);
        int newHeight = calculateNewDimension(width, height, radians, false);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = prepareGraphics(rotatedImage, newWidth, newHeight); // граф объект для рисования

        // rotation with interpolation
        applyRotation(img, rotatedImage, radians, width, height, newWidth, newHeight);

        graphics.dispose(); // release of resources
        return rotatedImage;
    }

    // calculating new image sizes
    private int calculateNewDimension(int width, int height, double radians, boolean isWidth) {
        if (isWidth) {
            return (int) (Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
        } else {
            return (int) (Math.abs(height * Math.cos(radians)) + Math.abs(width * Math.sin(radians)));
        }
    }

    // setting up a graph object
    private Graphics2D prepareGraphics(BufferedImage rotatedImage, int newWidth, int newHeight) {
        Graphics2D g = rotatedImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    // applying rotation to an image
    private void applyRotation(BufferedImage img, BufferedImage rotatedImage, double radians, int width, int height, int newWidth, int newHeight) {
        // center of the original image
        int cx = width / 2;
        int cy = height / 2;

        // center of the new image
        int newCX = newWidth / 2;
        int newCY = newHeight / 2;

        double cosTheta = Math.cos(radians);
        double sinTheta = Math.sin(radians);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // calculating coordinates in the original image
                double ox = (x - newCX) * cosTheta + (y - newCY) * sinTheta + cx;
                double oy = -(x - newCX) * sinTheta + (y - newCY) * cosTheta + cy;

                // check that the coordinates are within the image
                if (isWithinBounds(ox, oy, width, height)) {
                    int rgb = interpolateColor(img, ox, oy);
                    rotatedImage.setRGB(x, y, rgb);
                } else {
                    // setting a white background for pixels outside the original image
                    rotatedImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
    }

    // checking that the coordinates are within the image
    private boolean isWithinBounds(double ox, double oy, int width, int height) {
        return ox >= 0 && ox < width - 1 && oy >= 0 && oy < height - 1;
    }

    // bilinear interpolation for calculating pixel color
    private int interpolateColor(BufferedImage img, double ox, double oy) {
        int x1 = (int) ox;
        int y1 = (int) oy;
        int x2 = x1 + 1;
        int y2 = y1 + 1;

        // extracting colors
        int rgb1 = img.getRGB(x1, y1);
        int rgb2 = img.getRGB(x2, y1);
        int rgb3 = img.getRGB(x1, y2);
        int rgb4 = img.getRGB(x2, y2);

        double dx = ox - x1;
        double dy = oy - y1;
        double weight1 = (1 - dx) * (1 - dy);
        double weight2 = dx * (1 - dy);
        double weight3 = (1 - dx) * dy;
        double weight4 = dx * dy;

        // color interpolation
        int r = (int) (((rgb1 >> 16) & 0xFF) * weight1 +
                ((rgb2 >> 16) & 0xFF) * weight2 +
                ((rgb3 >> 16) & 0xFF) * weight3 +
                ((rgb4 >> 16) & 0xFF) * weight4);
        int g = (int) (((rgb1 >> 8) & 0xFF) * weight1 +
                ((rgb2 >> 8) & 0xFF) * weight2 +
                ((rgb3 >> 8) & 0xFF) * weight3 +
                ((rgb4 >> 8) & 0xFF) * weight4);
        int b = (int) ((rgb1 & 0xFF) * weight1 +
                (rgb2 & 0xFF) * weight2 +
                (rgb3 & 0xFF) * weight3 +
                (rgb4 & 0xFF) * weight4);

        return (r << 16) | (g << 8) | b;
    }
}
