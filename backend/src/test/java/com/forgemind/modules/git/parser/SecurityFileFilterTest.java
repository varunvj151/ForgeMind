package com.forgemind.modules.git.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityFileFilterTest {

  private final SecurityFileFilter filter = new SecurityFileFilter();

  @Test
  void testSafeFiles() {
    assertThat(filter.isSafeToIndex("src/main/java/Application.java")).isTrue();
    assertThat(filter.isSafeToIndex("README.md")).isTrue();
    assertThat(filter.isSafeToIndex("package.json")).isTrue();
  }

  @Test
  void testBlockedExtensions() {
    assertThat(filter.isSafeToIndex("cert.pem")).isFalse();
    assertThat(filter.isSafeToIndex("private.key")).isFalse();
    assertThat(filter.isSafeToIndex("keystore.jks")).isFalse();
  }

  @Test
  void testBlockedFilenames() {
    assertThat(filter.isSafeToIndex(".env")).isFalse();
    assertThat(filter.isSafeToIndex("src/main/resources/application-secret.yml")).isFalse();
    assertThat(filter.isSafeToIndex("id_rsa")).isFalse();
  }

  @Test
  void testBlockedSubstrings() {
    assertThat(filter.isSafeToIndex("db_password.txt")).isFalse();
    assertThat(filter.isSafeToIndex("aws_secret_key.json")).isFalse();
    assertThat(filter.isSafeToIndex("api_key.ts")).isFalse();
  }
}
