package com.forgemind.modules.git.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CodeChunkerTest {

  private final CodeChunker chunker = new CodeChunker();

  @Test
  void testChunkingEmptyContent() {
    assertThat(chunker.chunkCode("", Language.JAVA)).isEmpty();
    assertThat(chunker.chunkCode(null, Language.JAVA)).isEmpty();
  }

  @Test
  void testChunkingSmallContent() {
    String content = "public class Hello {\n  public static void main(String[] args) {}\n}";
    List<String> chunks = chunker.chunkCode(content, Language.JAVA);
    assertThat(chunks).hasSize(1);
    assertThat(chunks.get(0)).contains("public class Hello");
  }

  @Test
  void testChunkingLargeContent() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 200; i++) {
      sb.append("This is line number ").append(i).append(" which has a decent amount of text so we can hit the limit faster.\n");
    }
    List<String> chunks = chunker.chunkCode(sb.toString(), Language.JAVA);
    assertThat(chunks.size()).isGreaterThan(1);
    
    // Check overlap
    String chunk1 = chunks.get(0);
    String chunk2 = chunks.get(1);
    
    String[] lines1 = chunk1.split("\n");
    String[] lines2 = chunk2.split("\n");
    
    // The last few lines of chunk1 should be the first few lines of chunk2
    assertThat(lines2[0]).isEqualTo(lines1[lines1.length - 6]);
  }
}
