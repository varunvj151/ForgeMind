package com.forgemind.modules.ai.prompt;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

@Component
public class PromptTemplateManager {

    private final PropertyPlaceholderHelper placeholderHelper;

    public PromptTemplateManager() {
        // Use ${} format for placeholders
        this.placeholderHelper = new PropertyPlaceholderHelper("${", "}");
    }

    /**
     * Interpolates the variables in the template string with values from the context map.
     *
     * @param template The template string containing ${key} placeholders.
     * @param context  The map of values to substitute.
     * @return The resolved prompt.
     */
    public String resolveTemplate(String template, Map<String, Object> context) {
        return placeholderHelper.replacePlaceholders(template, placeholderName -> {
            Object value = context.get(placeholderName);
            return value != null ? value.toString() : "";
        });
    }

    // --- Pre-defined Prompt Templates ---

    public static final String TASK_GENERATION_SYSTEM_PROMPT = 
        "You are an expert technical project manager and software architect. " +
        "Your role is to break down feature requests into detailed, actionable Jira-style Kanban tasks. " +
        "Output ONLY a valid JSON array of objects, where each object has 'title', 'description' (detailed, with acceptance criteria), " +
        "and 'priority' (LOW, MEDIUM, HIGH, URGENT). Do not include markdown formatting or extra text.";

    public static final String TASK_GENERATION_USER_PROMPT = 
        "Context: Here is the current state of the project and existing tasks in JSON format:\n" +
        "${contextJson}\n\n" +
        "Based on this context, please break down the following feature request into actionable tasks:\n" +
        "Feature Request: ${featureDescription}";

    public static final String PROJECT_SUMMARY_SYSTEM_PROMPT =
        "You are an agile coach. Generate a concise, markdown-formatted executive summary of the project's current status, " +
        "based on the completed, in-progress, and pending tasks. Highlight any bottlenecks.";

    public static final String PROJECT_SUMMARY_USER_PROMPT =
        "Project Context (JSON):\n${contextJson}\n\n" +
        "Please provide the project summary.";
        
    public static final String RISK_ASSESSMENT_SYSTEM_PROMPT =
        "You are a risk management AI. Analyze the provided project tasks and identify potential technical or scheduling risks, " +
        "such as too many high-priority tasks in progress, stale tasks, or lack of task descriptions. " +
        "Output ONLY a valid JSON array of objects, where each object has 'riskLevel' (LOW, MEDIUM, HIGH), 'summary', and 'recommendation'.";

    public static final String RISK_ASSESSMENT_USER_PROMPT =
        "Project Context (JSON):\n${contextJson}\n\n" +
        "Please generate a risk assessment.";
}
