package com.forgemind.modules.git.service;

import com.forgemind.modules.ai.embedding.EmbeddingProvider;
import com.forgemind.modules.git.dto.request.CodeSearchRequest;
import com.forgemind.modules.git.dto.response.CodeSearchResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeSearchServiceImpl implements CodeSearchService {

  private final JdbcTemplate jdbcTemplate;
  private final EmbeddingProvider embeddingProvider;

  public CodeSearchServiceImpl(JdbcTemplate jdbcTemplate, EmbeddingProvider embeddingProvider) {
    this.jdbcTemplate = jdbcTemplate;
    this.embeddingProvider = embeddingProvider;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CodeSearchResponse> search(CodeSearchRequest request) {
    float[] queryVector = embeddingProvider.embed(request.getQuery());
    if (queryVector == null || queryVector.length == 0) {
      return List.of();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < queryVector.length; i++) {
        sb.append(queryVector[i]);
        if (i < queryVector.length - 1) sb.append(",");
    }
    sb.append("]");
    String vectorStr = sb.toString();
    
    // Perform vector similarity search
    String sql = """
        SELECT id, repository_id, file_path, language, symbol_name, chunk_index, chunk_text,
               1 - (embedding <=> ?::vector) AS score
        FROM code_chunks
        WHERE project_id = ? AND embedding IS NOT NULL
        ORDER BY embedding <=> ?::vector
        LIMIT ?
        """;

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
        sql, vectorStr, request.getProjectId(), vectorStr, request.getTopK());

    List<CodeSearchResponse> results = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      results.add(CodeSearchResponse.builder()
          .id((UUID) row.get("id"))
          .repositoryId((UUID) row.get("repository_id"))
          .filePath((String) row.get("file_path"))
          .language((String) row.get("language"))
          .symbolName((String) row.get("symbol_name"))
          .chunkIndex((Integer) row.get("chunk_index"))
          .chunkText((String) row.get("chunk_text"))
          .score((Double) row.get("score"))
          .build());
    }

    return results;
  }
}
