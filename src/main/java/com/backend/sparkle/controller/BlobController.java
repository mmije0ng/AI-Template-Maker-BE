package com.backend.sparkle.controller;

import com.backend.sparkle.dto.BlobDto;
import com.backend.sparkle.service.BlobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/blob")
@Tag(name = "Blob Storage", description = "Azure Blob Storage API")
public class BlobController {

    private final BlobService blobService;

    @Autowired
    public BlobController(BlobService blobService) {
        this.blobService = blobService;
    }

    @Operation(summary = "Blob Storage에 URL 기반 이미지 저장하기")
    @PostMapping("/uploadImageByUrl")
    public String uploadImageByUrl(@RequestParam("imageUrl") String imageUrl) {
        return blobService.uploadImageByUrl(imageUrl);
    }

    @Operation(summary = "base64로 인코딩된 이미지를 Request Body에서 받아 Blob Storage에 업로드")
    @PostMapping("/uploadImageByUrl/body")
    public ResponseEntity<BlobDto.ResponseDto> uploadImageByUrlByBody(@RequestBody BlobDto.RequetDto requestDto) {
        log.info("/uploadImageByUrl/body");

        BlobDto.ResponseDto responseDto = new BlobDto.ResponseDto(blobService.uploadBase64Image(requestDto.getBase64Image()));
        log.info("base64 => URL 변환: {}", responseDto.getImageUrl());

        return ResponseEntity.ok(responseDto);
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
