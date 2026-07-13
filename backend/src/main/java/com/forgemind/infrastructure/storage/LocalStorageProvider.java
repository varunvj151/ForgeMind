package com.forgemind.infrastructure.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Local filesystem storage — intended for development and single-node deployments. */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageProvider implements StorageProvider {

  @Value("${app.storage.local.base-path:/tmp/forgemind/storage}")
  private String basePath;

  @Override
  public void upload(String path, InputStream content, long contentLength, Map<String, String> metadata) {
    Path target = resolvePath(path);
    try {
      Files.createDirectories(target.getParent());
      try (FileOutputStream out = new FileOutputStream(target.toFile())) {
        content.transferTo(out);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to upload to local storage: " + path, e);
    }
  }

  @Override
  public InputStream download(String path) {
    try {
      return new FileInputStream(resolvePath(path).toFile());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to download from local storage: " + path, e);
    }
  }

  @Override
  public void delete(String path) {
    try {
      Files.deleteIfExists(resolvePath(path));
    } catch (IOException e) {
      log.warn("Could not delete {}: {}", path, e.getMessage());
    }
  }

  @Override
  public String signedUrl(String path, Duration ttl) {
    return "file://" + resolvePath(path).toAbsolutePath();
  }

  @Override
  public boolean exists(String path) {
    return resolvePath(path).toFile().exists();
  }

  private Path resolvePath(String path) {
    return Paths.get(basePath, path);
  }
}
