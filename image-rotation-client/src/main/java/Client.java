import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Client {
    private static final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080/api/image";

    static {
        restTemplate = new RestTemplate();
    }

    public static void main(String[] args) {
        String imagePath = "D:/Pictures/orig.jpg";
        double angle = 45.0;
        String message = "Image rotated successfully!";
        String outputImagePath = "D:/Pictures/rotatedImage.png";

        try {
            File imageFile = new File(imagePath);
            MultipartFile multipartFile = convertFileToMultipartFile(imageFile);
            byte[] rotatedImageBytes = rotateImage(multipartFile, angle, message);
            if (rotatedImageBytes != null) {
                saveImage(rotatedImageBytes, outputImagePath);
            } else {
                System.out.println("Rotated image bytes are null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MultipartFile convertFileToMultipartFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file", file.getName(), "image/png", input);
    }

    public static byte[] rotateImage(MultipartFile file, double angle, String message) {
        String url = BASE_URL + "/rotate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url + "?angle=" + angle, HttpMethod.POST, requestEntity, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(message);
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveImage(byte[] imageBytes, String outputPath) throws IOException {
        if (imageBytes == null) {
            return;
        }
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            System.out.println("Image with that name already exists");
            outputFile.delete();
        }
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs(); // creating parent directories
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
