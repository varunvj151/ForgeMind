package com.forgemind.modules.git.ai.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeReviewResult {
  private int score;
  private String summary;
  private List<CodeReviewIssue> issues;
  private List<String> positives;
  private List<String> overallRecommendations;
}
