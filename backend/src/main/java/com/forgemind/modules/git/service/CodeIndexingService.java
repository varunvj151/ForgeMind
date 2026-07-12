package com.forgemind.modules.git.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CodeIndexingService {
  void indexRepository(UUID repositoryId);
  CompletableFuture<Void> indexRepositoryAsync(UUID repositoryId);
  void deleteRepositoryIndex(UUID repositoryId);
}
