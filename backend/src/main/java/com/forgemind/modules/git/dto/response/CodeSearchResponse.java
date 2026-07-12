package com.forgemind.modules.git.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeSearchResponse {
  private UUID id;
  private UUID repositoryId;
  private String filePath;
  private String language;
  private String symbolName;
  private int chunkIndex;
  private String chunkText;
  private double score;
}
