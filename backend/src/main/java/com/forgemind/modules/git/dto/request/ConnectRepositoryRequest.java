package com.forgemind.modules.git.dto.request;

import com.forgemind.modules.git.provider.GitProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class ConnectRepositoryRequest {
  @NotNull
  private UUID projectId;

  @NotNull
  private GitProviderType provider;

  @NotBlank
  private String owner;

  @NotBlank
  private String repoName;

  @NotBlank
  private String accessToken;
}
