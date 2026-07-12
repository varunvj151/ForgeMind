package com.forgemind.modules.git.parser;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SecurityFileFilter {

  private static final List<String> BLOCKED_EXTENSIONS = List.of(
      ".pem", ".key", ".p12", ".pfx", ".jks", ".keystore", ".truststore"
  );

  private static final List<String> BLOCKED_FILENAMES = List.of(
      ".env", "application-secret.yml", "application-secret.properties", "id_rsa", "id_ed25519"
  );

  private static final List<String> BLOCKED_SUBSTRINGS = List.of(
      "secret", "password", "credential", "api_key", "apikey"
  );

  public boolean isSafeToIndex(String filePath) {
    if (filePath == null) return false;
    
    String lowerPath = filePath.toLowerCase();
    String filename = lowerPath.substring(lowerPath.lastIndexOf('/') + 1);

    if (BLOCKED_EXTENSIONS.stream().anyMatch(lowerPath::endsWith)) {
      return false;
    }

    if (BLOCKED_FILENAMES.contains(filename)) {
      return false;
    }

    if (BLOCKED_SUBSTRINGS.stream().anyMatch(filename::contains)) {
      return false;
    }

    return true;
  }
}
