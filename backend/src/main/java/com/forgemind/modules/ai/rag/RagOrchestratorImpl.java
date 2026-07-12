package com.forgemind.modules.ai.rag;

import com.forgemind.modules.ai.retrieval.RetrievalEngine;
import com.forgemind.modules.ai.retrieval.RetrievalQuery;
import com.forgemind.modules.ai.retrieval.RetrievalResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Default implementation of {@link RagOrchestrator}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagOrchestratorImpl implements RagOrchestrator {

  private final RetrievalEngine retrievalEngine;
  private final PromptAugmenter promptAugmenter;
  private final RagProperties properties;

  @Override
  public RagContext augmentPrompt(UUID projectId, String userPrompt) {
    if (!properties.isEnabled()) {
      log.debug("RAG is disabled. Skipping retrieval for project {}", projectId);
      return new RagContext(userPrompt, List.of(), userPrompt, false);
    }

    try {
      RetrievalQuery query =
          RetrievalQuery.builder()
              .projectId(projectId)
              .queryText(userPrompt)
              .topK(properties.getTopK())
              .minScore(properties.getMinScore())
              .build();

      RetrievalResult result = retrievalEngine.retrieve(query);

      if (result.chunks().isEmpty()) {
        log.debug("RAG retrieved 0 chunks above minScore {}. Prompt unchanged.", properties.getMinScore());
        return new RagContext(userPrompt, List.of(), userPrompt, false);
      }

      String augmented = promptAugmenter.augment(userPrompt, result.chunks());
      return new RagContext(userPrompt, result.chunks(), augmented, true);

    } catch (Exception e) {
      log.warn("RAG retrieval failed, falling back to un-augmented prompt. Reason: {}", e.getMessage());
      return new RagContext(userPrompt, List.of(), userPrompt, false);
    }
  }
}
