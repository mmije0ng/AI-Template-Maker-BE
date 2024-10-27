package com.backend.sparkle.controller;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/blob")
@Tag(name = "Blob Storage", description = "Azure Blob Storage API")
public class BlobController {

    private final BlobContainerClient blobContainerClient;

    public BlobController(
            @Value("${spring.cloud.azure.storage.blob.account-name}") String accountName,
            @Value("${spring.cloud.azure.storage.blob.account-key}") String accountKey,
            @Value("${spring.cloud.azure.storage.blob.endpoint}") String endpoint) {

        this.blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(endpoint)
                .sasToken(accountKey)
                .containerName("testcontainer") // 적절한 Blob 컨테이너 이름으로 변경
                .buildClient();
    }

    @Operation(summary = "Blob Storage에 이미지 저장하기")
    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        BlobClient blobClient = blobContainerClient.getBlobClient(file.getOriginalFilename());
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        return "Image uploaded: " + blobClient.getBlobUrl();
    }

    @Operation(summary = "Blob Storage를 통해 이미지 가져오기")
    @GetMapping("/getImage")
    public URL getImage(@RequestParam("fileName") String fileName) throws MalformedURLException {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        return new URL(blobClient.getBlobUrl());
    }
}
