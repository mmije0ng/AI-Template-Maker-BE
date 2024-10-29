package com.backend.sparkle.controller;

import com.backend.sparkle.service.BlobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RestController
@RequestMapping("/blob")
@Tag(name = "Blob Storage", description = "Azure Blob Storage API")
public class BlobController {

    private final BlobService blobService;

    public BlobController(BlobService blobService) {
        this.blobService = blobService;
    }

    @Operation(summary = "Blob Storage에 URL 기반 이미지 저장하기")
    @PostMapping("/uploadImageByUrl")
    public String uploadImageByUrl(@RequestParam("imageUrl") String imageUrl) {
        return blobService.uploadImageByUrl(imageUrl);
    }

    @Operation(summary = "Blob Storage를 통해 이미지 가져오기")
    @GetMapping("/getImage")
    public URL getImage(@RequestParam("fileName") String fileName) {
        try {
            return blobService.getBlobUrl(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve image: " + e.getMessage());
        }
    }
}
