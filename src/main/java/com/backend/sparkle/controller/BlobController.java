package com.backend.sparkle.controller;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/blob")
@Tag(name = "Blob Storage", description = "Azure Blob Storage API")
public class BlobController {

    private final BlobContainerClient blobContainerClient;

    public BlobController(
            @Value("${spring.cloud.azure.storage.blob.connection-string}") String connectionString) {
        this.blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName("test-blob")
                .buildClient();
    }

    public String uploadBlob(String blobName, InputStream data, long length) {
        blobContainerClient.getBlobClient(blobName).upload(data, length, true);
        return blobContainerClient.getBlobClient(blobName).getBlobUrl();
    }

    @Operation(summary = "Blob Storage에 URL 기반 이미지 저장하기")
    @PostMapping("/uploadImageByUrl")
    public String uploadImageByUrl(@RequestParam("imageUrl") String imageUrl) {
        try {
            // 이미지 URL로부터 InputStream 가져오기
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            long contentLength = connection.getContentLengthLong();

            // BlobClient 설정 및 Blob Storage에 이미지 업로드
            String blobName = "uploaded_image_" + System.currentTimeMillis() + ".png";

            return uploadBlob(blobName, inputStream, contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload image: " + e.getMessage();
        }
    }

    @Operation(summary = "Blob Storage를 통해 이미지 가져오기")
    @GetMapping("/getImage")
    public URL getImage(@RequestParam("fileName") String fileName) throws MalformedURLException {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        return new URL(blobClient.getBlobUrl());
}
}
