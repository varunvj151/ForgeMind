package com.forgemind.modules.ai.indexing;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Splits text into overlapping fixed-size chunks, preferring sentence boundaries.
 *
 * <p>The algorithm:
 * <ol>
 *   <li>Advance a window of {@link ChunkingOptions#getMaxChunkSize()} characters.
 *   <li>Search backward from the window edge for a sentence boundary ({@code .  !  ?} followed by
 *       a space or end-of-string) to avoid splitting mid-sentence.
 *   <li>Emit the chunk and advance by {@code maxChunkSize - overlap} characters.
 *   <li>Discard any trailing chunk shorter than {@link ChunkingOptions#getMinChunkSize()}.
 * </ol>
 */
@Component
public class FixedSizeChunker implements ChunkingStrategy {

  @Override
  public List<String> chunk(String content, ChunkingOptions options) {
    if (content == null || content.isBlank()) {
      return List.of();
    }

    int maxSize = options.getMaxChunkSize();
    int overlap = options.getOverlap();
    int minSize = options.getMinChunkSize();
    int stride = Math.max(1, maxSize - overlap);

    List<String> chunks = new ArrayList<>();
    int start = 0;

    while (start < content.length()) {
      int end = Math.min(start + maxSize, content.length());

      // If we haven't reached the string end, try to split on a sentence boundary.
      if (end < content.length()) {
        int boundary = findSentenceBoundary(content, start, end);
        if (boundary > start + minSize) {
          end = boundary;
        }
      }

      String chunk = content.substring(start, end).strip();
      if (chunk.length() >= minSize) {
        chunks.add(chunk);
      }

      if (end >= content.length()) break;
      start = end - overlap;
      if (start < 0) start = 0;
    }

    return chunks;
  }

  /**
   * Searches backward from {@code end} for the last sentence-ending punctuation ({@code . ! ?})
   * followed by a whitespace character, returning the index immediately after that punctuation.
   * Returns {@code end} unchanged if no boundary is found.
   */
  private int findSentenceBoundary(String text, int start, int end) {
    for (int i = end - 1; i > start; i--) {
      char c = text.charAt(i);
      if ((c == '.' || c == '!' || c == '?')
          && (i + 1 >= text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
        return i + 1;
      }
    }
    return end;
  }
}
