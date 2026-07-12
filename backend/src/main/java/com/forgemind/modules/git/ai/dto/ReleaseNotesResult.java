package com.forgemind.modules.git.ai.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReleaseNotesResult {
  private String version;
  private String releasedAt;
  private List<String> highlights;
  private List<String> features;
  private List<String> bugfixes;
  private List<String> improvements;
  private List<String> breakingChanges;
  private List<String> contributors;
  private String markdownSummary;
}
