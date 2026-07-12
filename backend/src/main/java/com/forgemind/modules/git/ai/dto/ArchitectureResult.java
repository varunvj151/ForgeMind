package com.forgemind.modules.git.ai.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArchitectureResult {
  private String summary;
  private List<Module> modules;
  private List<Risk> risks;
  private List<String> refactoringOpportunities;
  private String technicalDebt;
  private int overallHealthScore;

  @Data
  @NoArgsConstructor
  public static class Module {
    private String name;
    private String description;
    private int fileCount;
  }

  @Data
  @NoArgsConstructor
  public static class Risk {
    private String severity;
    private String description;
    private List<String> affectedFiles;
    private String recommendation;
  }
}
