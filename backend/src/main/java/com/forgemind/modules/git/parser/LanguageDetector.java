package com.forgemind.modules.git.parser;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LanguageDetector {

  private static final Map<String, Language> EXTENSION_MAP = new HashMap<>();

  static {
    EXTENSION_MAP.put("java", Language.JAVA);
    EXTENSION_MAP.put("kt", Language.KOTLIN);
    EXTENSION_MAP.put("ts", Language.TYPESCRIPT);
    EXTENSION_MAP.put("tsx", Language.TYPESCRIPT);
    EXTENSION_MAP.put("js", Language.JAVASCRIPT);
    EXTENSION_MAP.put("jsx", Language.JAVASCRIPT);
    EXTENSION_MAP.put("html", Language.HTML);
    EXTENSION_MAP.put("css", Language.CSS);
    EXTENSION_MAP.put("sql", Language.SQL);
    EXTENSION_MAP.put("yml", Language.YAML);
    EXTENSION_MAP.put("yaml", Language.YAML);
    EXTENSION_MAP.put("md", Language.MARKDOWN);
    EXTENSION_MAP.put("json", Language.JSON);
    EXTENSION_MAP.put("xml", Language.XML);
    EXTENSION_MAP.put("sh", Language.BASH);
    EXTENSION_MAP.put("py", Language.PYTHON);
  }

  public Language detectLanguage(String filePath) {
    if (filePath == null) return Language.UNKNOWN;
    int dotIndex = filePath.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
      String extension = filePath.substring(dotIndex + 1).toLowerCase();
      return EXTENSION_MAP.getOrDefault(extension, Language.UNKNOWN);
    }
    return Language.UNKNOWN;
  }
}
