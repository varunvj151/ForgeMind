package com.forgemind.modules.git.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CodeSearchRequest {
  @NotBlank
  private String query;

  @NotNull
  private UUID projectId;

  private int topK = 5;
}
