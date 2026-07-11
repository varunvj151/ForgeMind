package com.forgemind.modules.ai.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptTemplateManagerTest {

    private final PromptTemplateManager promptTemplateManager = new PromptTemplateManager();

    @Test
    void resolveTemplate_ShouldReplacePlaceholders() {
        String template = "Project: ${projectName}, Tasks: ${taskCount}";
        Map<String, Object> context = Map.of(
                "projectName", "ForgeMind",
                "taskCount", 42
        );

        String result = promptTemplateManager.resolveTemplate(template, context);

        assertEquals("Project: ForgeMind, Tasks: 42", result);
    }

    @Test
    void resolveTemplate_ShouldReplaceMissingPlaceholdersWithEmptyString() {
        String template = "Project: ${projectName}, Details: ${missingData}";
        Map<String, Object> context = Map.of(
                "projectName", "ForgeMind"
        );

        String result = promptTemplateManager.resolveTemplate(template, context);

        assertEquals("Project: ForgeMind, Details: ", result);
    }
}
