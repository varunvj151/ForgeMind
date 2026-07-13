package com.forgemind.infrastructure.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/** AWS S3 / MinIO compatible storage provider. */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3StorageProvider implements StorageProvider {

  @Value("${app.storage.s3.bucket}")
  private String bucket;

  private final S3Client s3Client;
  private final S3Presigner presigner;

  public S3StorageProvider(S3Client s3Client, S3Presigner presigner) {
    this.s3Client = s3Client;
    this.presigner = presigner;
  }

  @Override
  public void upload(String path, InputStream content, long contentLength, Map<String, String> metadata) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(path)
            .metadata(metadata)
            .build(),
        RequestBody.fromInputStream(content, contentLength));
  }

  @Override
  public InputStream download(String path) {
    return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(path).build());
  }

  @Override
  public void delete(String path) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(path).build());
  }

  @Override
  public String signedUrl(String path, Duration ttl) {
    return presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(path).build())
            .build()
    ).url().toString();
  }

  @Override
  public boolean exists(String path) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(path).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }
}
