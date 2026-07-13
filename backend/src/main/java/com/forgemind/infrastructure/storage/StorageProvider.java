package com.forgemind.infrastructure.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * Cloud-agnostic object storage abstraction.
 * Supports: AWS S3, MinIO, Google Cloud Storage, local filesystem.
 */
public interface StorageProvider {
  /**
   * Upload an object.
   * @param path path within the bucket, e.g. "orgs/abc/plugins/my-plugin.js"
   */
  void upload(String path, InputStream content, long contentLength, Map<String, String> metadata);

  /** Download an object as a stream. */
  InputStream download(String path);

  /** Delete an object. */
  void delete(String path);

  /** Generate a pre-signed URL for temporary direct access. */
  String signedUrl(String path, Duration ttl);

  /** Check whether an object exists. */
  boolean exists(String path);
}
