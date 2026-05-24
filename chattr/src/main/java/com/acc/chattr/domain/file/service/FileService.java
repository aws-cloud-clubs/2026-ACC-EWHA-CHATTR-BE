package com.acc.chattr.domain.file.service;

import com.acc.chattr.domain.file.dto.PresignRequest;
import com.acc.chattr.domain.file.dto.PresignResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class FileService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presign-expiry-minutes}")
    private int presignExpiryMinutes;

    public FileService(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public PresignResponse presign(String userId, PresignRequest request) {
        String fileKey = "uploads/%s/%s/%s".formatted(userId, UUID.randomUUID(), request.fileName());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .contentType(request.contentType())
            .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpiryMinutes))
                .putObjectRequest(putObjectRequest)
                .build());

        return new PresignResponse(fileKey, presigned.url().toString());
    }
}
