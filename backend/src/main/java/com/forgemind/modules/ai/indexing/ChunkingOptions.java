package com.forgemind.modules.ai.indexing;

import lombok.Builder;
import lombok.Getter;

/** Tuning parameters for a {@link ChunkingStrategy}. */
@Getter
@Builder
public class ChunkingOptions {

  /** Target maximum number of characters per chunk (≈ 500 tokens at 4 chars/token). */
  @Builder.Default
  private final int maxChunkSize = 2000;

  /** Character overlap between adjacent chunks to preserve inter-sentence context. */
  @Builder.Default
  private final int overlap = 200;

  /** Minimum characters a chunk must have to be kept (avoids tiny trailing fragments). */
  @Builder.Default
  private final int minChunkSize = 50;

  /** Convenience factory: returns the default options. */
  public static ChunkingOptions defaults() {
    return ChunkingOptions.builder().build();
  }
}
