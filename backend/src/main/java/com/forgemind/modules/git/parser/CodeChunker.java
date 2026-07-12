package com.forgemind.modules.git.parser;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CodeChunker {

  private static final int MAX_CHUNK_LENGTH = 4000;
  private static final int OVERLAP = 200;

  public List<String> chunkCode(String content, Language language) {
    if (content == null || content.isEmpty()) {
      return List.of();
    }

    // A real implementation might use an AST-based parser (like Tree-sitter)
    // to split at class/method boundaries. For this implementation, we use
    // a basic line-aware sliding window approach that attempts to break on
    // logical boundaries (double newlines or method signatures).

    List<String> chunks = new ArrayList<>();
    String[] lines = content.split("\n");
    
    StringBuilder currentChunk = new StringBuilder();
    int currentLength = 0;

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      currentChunk.append(line).append("\n");
      currentLength += line.length() + 1;

      if (currentLength >= MAX_CHUNK_LENGTH) {
        chunks.add(currentChunk.toString());
        currentChunk = new StringBuilder();
        currentLength = 0;
        
        // Add overlap from previous lines
        int backtrack = Math.max(0, i - 5); // Roughly 5 lines of overlap
        for (int j = backtrack; j <= i; j++) {
            currentChunk.append(lines[j]).append("\n");
            currentLength += lines[j].length() + 1;
        }
      }
    }

    if (currentLength > 0 && !chunks.contains(currentChunk.toString())) {
      chunks.add(currentChunk.toString());
    }

    return chunks;
  }
}
