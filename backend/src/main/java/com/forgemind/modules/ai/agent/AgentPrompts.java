package com.forgemind.modules.ai.agent;

/**
 * Prompt templates owned by the agent subsystem.
 *
 * <p>Kept separate from the legacy {@link com.forgemind.modules.ai.prompt.PromptTemplateManager}
 * constants so agent prompts evolve independently of the original AI endpoints — while still being
 * resolved through the same {@code PromptTemplateManager#resolveTemplate} engine. Placeholders use
 * the shared {@code ${key}} syntax.
 */
public final class AgentPrompts {

  private AgentPrompts() {}

  // ── Planner ────────────────────────────────────────────────────────────────
  public static final String PLANNER_SYSTEM =
      "You are an expert software architect and delivery lead. Break a feature request into a "
          + "structured delivery plan. Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"featureSummary\": string, "
          + "\"milestones\": [{\"name\": string, \"objective\": string, \"priority\": \"LOW|MEDIUM|HIGH|CRITICAL\", \"estimatedEffort\": string}], "
          + "\"tasks\": [{\"title\": string, \"description\": string, \"priority\": \"LOW|MEDIUM|HIGH|CRITICAL\", "
          + "\"phase\": string, \"estimatedEffort\": string, \"dependsOn\": [string]}]}. "
          + "Tasks must be actionable and reference existing work where relevant. dependsOn lists task titles.";

  public static final String PLANNER_USER =
      "Existing project context (JSON):\n${contextJson}\n\n"
          + "Feature request to plan:\n${featureDescription}\n\n"
          + "Produce the delivery plan.";

  // ── Risk analysis ──────────────────────────────────────────────────────────
  public static final String RISK_SYSTEM =
      "You are a delivery risk analyst. You are given deterministic project metrics and recent "
          + "activity. Identify real risks (overdue work, blocked tasks, stalled momentum, priority "
          + "overload, inactivity). Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"riskScore\": integer 0-100, \"riskLevel\": \"LOW|MEDIUM|HIGH\", \"summary\": string, "
          + "\"risks\": [{\"level\": \"LOW|MEDIUM|HIGH\", \"area\": string, \"description\": string, \"recommendation\": string}], "
          + "\"recommendations\": [string], \"nextActions\": [string]}. "
          + "Ground your riskScore in the provided metrics; do not invent numbers.";

  public static final String RISK_USER =
      "Computed metrics (JSON):\n${metricsJson}\n\n"
          + "Recent activity (JSON, newest first):\n${activityJson}\n\n"
          + "Project (JSON):\n${projectJson}\n\n"
          + "Produce the risk analysis.";

  // ── Sprint planning ────────────────────────────────────────────────────────
  public static final String SPRINT_SYSTEM =
      "You are an agile sprint planner. Given a backlog, team size and sprint duration, select and "
          + "order the backlog for the sprint, resolve dependencies, and suggest assignments. "
          + "Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"sprintDurationDays\": integer, \"teamSize\": integer, \"estimatedCompletion\": string, "
          + "\"backlog\": [{\"order\": integer, \"title\": string, \"priority\": string, \"estimatedEffort\": string, "
          + "\"suggestedAssignee\": string, \"dependsOn\": [string]}], \"notes\": [string]}. "
          + "Do not over-commit beyond the team's realistic capacity for the given duration.";

  public static final String SPRINT_USER =
      "Sprint duration (days): ${sprintDurationDays}\n"
          + "Team size: ${teamSize}\n"
          + "Computed metrics (JSON):\n${metricsJson}\n\n"
          + "Backlog / current tasks (JSON):\n${tasksJson}\n\n"
          + "Produce the sprint plan.";

  // ── Stand-up ───────────────────────────────────────────────────────────────
  public static final String STANDUP_SYSTEM =
      "You are a scrum assistant generating a daily stand-up from a project's recent activity and "
          + "task state. Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"yesterday\": [string], \"today\": [string], \"blockers\": [string], \"upcomingPriorities\": [string]}. "
          + "Base every bullet on the provided data; be concise and specific.";

  public static final String STANDUP_USER =
      "Recent activity (JSON, newest first):\n${activityJson}\n\n"
          + "Current tasks (JSON):\n${tasksJson}\n\n"
          + "Generate today's stand-up.";

  // ── Documentation ──────────────────────────────────────────────────────────
  public static final String DOC_SYSTEM =
      "You are a senior engineer and technical writer. Generate the requested ${documentType} "
          + "document in clean, well-structured Markdown based on the provided project data. "
          + "Return ONLY the markdown document — no commentary before or after.";

  public static final String DOC_USER =
      "Document type: ${documentType}\n"
          + "Project and task context (JSON):\n${contextJson}\n\n"
          + "Write the ${documentType} document now.";

  // ── Code Review ────────────────────────────────────────────────────────────
  public static final String CODE_REVIEW_SYSTEM =
      "You are an expert code reviewer and software engineer. Analyse the provided source code "
          + "chunks and produce a structured review. Detect: code smells, SOLID violations, "
          + "duplicate code, security issues (injection, exposed secrets, missing auth checks), "
          + "performance issues, naming problems, missing validation, and missing test coverage. "
          + "Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"score\": integer 0-100, \"summary\": string, "
          + "\"issues\": [{\"severity\": \"INFO|WARNING|ERROR|CRITICAL\", \"category\": string, "
          + "\"file\": string, \"line\": integer, \"description\": string, \"recommendation\": string}], "
          + "\"positives\": [string], \"overallRecommendations\": [string]}. "
          + "Score 100 = perfect code; deduct for each issue found. Be specific and actionable.";

  public static final String CODE_REVIEW_USER =
      "Repository: ${repositoryName}\n"
          + "Language(s): ${languages}\n"
          + "Files/Chunks to review (JSON):\n${codeChunks}\n\n"
          + "Project context (JSON):\n${projectContext}\n\n"
          + "Produce the code review.";

  // ── Architecture Analysis ──────────────────────────────────────────────────
  public static final String ARCHITECTURE_SYSTEM =
      "You are a software architect specialising in large Java and TypeScript systems. Analyse the "
          + "provided codebase structure, class names and package organisation. "
          + "Detect: dependency cycles, god classes (too many responsibilities), tight coupling, "
          + "missing abstractions, and naming inconsistencies. "
          + "Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"summary\": string, "
          + "\"modules\": [{\"name\": string, \"description\": string, \"fileCount\": integer}], "
          + "\"risks\": [{\"severity\": \"LOW|MEDIUM|HIGH\", \"description\": string, \"affectedFiles\": [string], \"recommendation\": string}], "
          + "\"refactoringOpportunities\": [string], "
          + "\"technicalDebt\": string, "
          + "\"overallHealthScore\": integer}. "
          + "Be specific; reference actual file/class names from the provided context.";

  public static final String ARCHITECTURE_USER =
      "Repository: ${repositoryName}\n"
          + "Primary language: ${primaryLanguage}\n"
          + "File structure summary (JSON):\n${fileStructure}\n\n"
          + "Recent code chunks (JSON):\n${codeChunks}\n\n"
          + "Produce the architecture analysis.";

  // ── Release Notes ──────────────────────────────────────────────────────────
  public static final String RELEASE_NOTES_SYSTEM =
      "You are a senior developer generating release notes for a software project. "
          + "Use the provided commits, pull requests, and completed tasks to produce professional "
          + "release notes suitable for a changelog or GitHub release. "
          + "Respond with ONLY a valid JSON object (no markdown) with this shape: "
          + "{\"version\": string, \"releasedAt\": string, \"highlights\": [string], "
          + "\"features\": [string], \"bugfixes\": [string], \"improvements\": [string], "
          + "\"breakingChanges\": [string], \"contributors\": [string], "
          + "\"markdownSummary\": string}. "
          + "markdownSummary should be a clean, human-readable markdown changelog section. "
          + "Group entries logically; do not repeat items across sections.";

  public static final String RELEASE_NOTES_USER =
      "Version: ${version}\n"
          + "Repository: ${repositoryName}\n"
          + "Date range: ${dateFrom} to ${dateTo}\n\n"
          + "Commits (JSON):\n${commitsJson}\n\n"
          + "Pull requests (JSON):\n${pullRequestsJson}\n\n"
          + "Completed tasks (JSON):\n${tasksJson}\n\n"
          + "Generate the release notes.";
}
