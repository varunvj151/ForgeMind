package com.forgemind.modules.git.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeReviewIssue {
  private String severity;
  private String category;
  private String file;
  private int line;
  private String description;
  private String recommendation;
}
