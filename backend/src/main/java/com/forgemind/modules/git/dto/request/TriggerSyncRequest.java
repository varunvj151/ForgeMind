package com.forgemind.modules.git.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class TriggerSyncRequest {
  @NotNull
  private UUID repositoryId;
}
