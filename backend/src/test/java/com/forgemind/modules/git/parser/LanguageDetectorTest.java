package com.forgemind.modules.git.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LanguageDetectorTest {

  private final LanguageDetector detector = new LanguageDetector();

  @Test
  void testDetectJava() {
    assertThat(detector.detectLanguage("src/main/java/MyClass.java")).isEqualTo(Language.JAVA);
  }

  @Test
  void testDetectTypeScript() {
    assertThat(detector.detectLanguage("frontend/src/app.ts")).isEqualTo(Language.TYPESCRIPT);
    assertThat(detector.detectLanguage("frontend/src/Component.tsx")).isEqualTo(Language.TYPESCRIPT);
  }

  @Test
  void testDetectUnknown() {
    assertThat(detector.detectLanguage("Makefile")).isEqualTo(Language.UNKNOWN);
    assertThat(detector.detectLanguage("src/assets/image.png")).isEqualTo(Language.UNKNOWN);
    assertThat(detector.detectLanguage(null)).isEqualTo(Language.UNKNOWN);
  }
}
