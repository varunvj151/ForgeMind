package com.forgemind.modules.ai.retrieval;

/**
 * The core engine for Retrieval-Augmented Generation (RAG).
 *
 * <p>Translates natural language queries into semantic searches against the indexed knowledge base.
 */
public interface RetrievalEngine {

  /**
   * Executes a semantic search.
   *
   * @param query the retrieval parameters
   * @return the ordered list of retrieved chunks and telemetry data
   */
  RetrievalResult retrieve(RetrievalQuery query);
}
