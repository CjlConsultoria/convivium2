package com.convivium.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicBaseUrl;

    public S3FileStorageService(
            @Value("${app.storage.endpoint:}") String endpoint,
            @Value("${app.storage.region:us-east-1}") String region,
            @Value("${app.storage.bucket-name:convivium}") String bucketName,
            @Value("${app.storage.access-key:}") String accessKey,
            @Value("${app.storage.secret-key:}") String secretKey) {

        this.bucketName = bucketName;

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
            // Path-style obrigatório para Backblaze B2 e outros S3-compatible (MinIO, R2)
            builder.serviceConfiguration(
                    S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }
        this.s3Client = builder.build();

        if (endpoint != null && !endpoint.isBlank()) {
            this.publicBaseUrl = endpoint.endsWith("/") ? endpoint + bucketName : endpoint + "/" + bucketName;
        } else {
            this.publicBaseUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com";
        }
    }

    @Override
    public String store(String relativePath, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(relativePath)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return publicBaseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("Erro ao enviar arquivo para S3: {}", relativePath, e);
            throw new RuntimeException("Erro ao salvar arquivo", e);
        }
    }
}
