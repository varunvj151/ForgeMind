package com.forgemind.modules.git.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GitSyncService {
  void syncRepository(UUID repositoryId);
  CompletableFuture<Void> syncRepositoryAsync(UUID repositoryId);
}
