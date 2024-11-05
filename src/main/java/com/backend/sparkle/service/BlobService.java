package com.backend.sparkle.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
@Service
public class BlobService {

    private final BlobContainerClient blobContainerClient;

    public BlobService(@Value("${azure.storage.blob.connection-string}") String connectionString) {
        this.blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName("test-blob")
                .buildClient();
    }

    public void setBlobContentDisposition(String blobName, String dispositionType) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        // Blob의 Content-Disposition 설정
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentDisposition(dispositionType); // 예: "inline"으로 설정

        blobClient.setHttpHeaders(headers);
    }

    public String uploadBlob(String blobName, InputStream data, long length) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.upload(data, length, true);

        // 업로드 후 Content-Disposition을 inline으로 설정하여 브라우저에서 열리도록 함
        setBlobContentDisposition(blobName, "inline");

        log.info("변환된 url: "+blobClient.getBlobUrl());

        return blobClient.getBlobUrl();
    }

    public String uploadImageByUrl(String imageUrl) {
        try {
            // 이미지 URL로부터 InputStream 가져오기
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            long contentLength = connection.getContentLengthLong();

            // Blob 이름 생성 및 업로드
            String blobName = "uploaded_image_" + System.currentTimeMillis() + ".png";
            return uploadBlob(blobName, inputStream, contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload image: " + e.getMessage();
        }
    }


    // base64로 인코딩된 이미지를 URL로 업로드
    public String uploadBase64Image(String base64ImageData) {
        try {
            // "data:image/png;base64," 부분 제거
            String base64Data = base64ImageData.split(",")[1];

            // Base64 데이터를 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);
            long contentLength = decodedBytes.length;

            // Blob 이름 생성 및 업로드
            String blobName = "uploaded_image_" + System.currentTimeMillis() + ".png";
            return uploadBlob(blobName, inputStream, contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload image: " + e.getMessage();
        }
    }

    public URL getBlobUrl(String fileName) throws Exception {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        return new URL(blobClient.getBlobUrl());
    }
}