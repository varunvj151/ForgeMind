package com.forgemind.modules.git.service;

import com.forgemind.modules.ai.embedding.EmbeddingProvider;
import com.forgemind.modules.git.config.GitMetrics;
import com.forgemind.modules.git.config.GitProperties;
import com.forgemind.modules.git.entity.CodeChunk;
import com.forgemind.modules.git.entity.GitRepository;
import com.forgemind.modules.git.parser.CodeChunker;
import com.forgemind.modules.git.parser.Language;
import com.forgemind.modules.git.parser.LanguageDetector;
import com.forgemind.modules.git.parser.SecurityFileFilter;
import com.forgemind.modules.git.provider.GitProvider;
import com.forgemind.modules.git.provider.GitProviderFactory;
import com.forgemind.modules.git.repository.CodeChunkRepository;
import com.forgemind.modules.git.repository.GitRepositoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CodeIndexingServiceImpl implements CodeIndexingService {

  private final GitRepositoryRepository gitRepositoryRepository;
  private final CodeChunkRepository codeChunkRepository;
  private final GitProviderFactory gitProviderFactory;
  private final CodeChunker codeChunker;
  private final LanguageDetector languageDetector;
  private final SecurityFileFilter securityFileFilter;
  private final EmbeddingProvider embeddingProvider;
  private final GitProperties gitProperties;
  private final GitMetrics gitMetrics;
  private final JdbcTemplate jdbcTemplate;

  public CodeIndexingServiceImpl(
      GitRepositoryRepository gitRepositoryRepository,
      CodeChunkRepository codeChunkRepository,
      GitProviderFactory gitProviderFactory,
      CodeChunker codeChunker,
      LanguageDetector languageDetector,
      SecurityFileFilter securityFileFilter,
      EmbeddingProvider embeddingProvider,
      GitProperties gitProperties,
      GitMetrics gitMetrics,
      JdbcTemplate jdbcTemplate) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.codeChunkRepository = codeChunkRepository;
    this.gitProviderFactory = gitProviderFactory;
    this.codeChunker = codeChunker;
    this.languageDetector = languageDetector;
    this.securityFileFilter = securityFileFilter;
    this.embeddingProvider = embeddingProvider;
    this.gitProperties = gitProperties;
    this.gitMetrics = gitMetrics;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void indexRepository(UUID repositoryId) {
    GitRepository repo = gitRepositoryRepository.findById(repositoryId)
        .orElseThrow(() -> new IllegalArgumentException("Repo not found"));

    GitProvider provider = gitProviderFactory.getProvider(repo.getProvider());
    
    // Default to main branch for now
    String ref = repo.getDefaultBranch();
    List<String> files = provider.listFiles(repo, ref);
    
    int filesIndexed = 0;
    int chunksGenerated = 0;

    for (String file : files) {
      if (filesIndexed >= gitProperties.getMaxFilesPerSync()) {
        break;
      }
      if (!securityFileFilter.isSafeToIndex(file)) {
        continue;
      }

      Language lang = languageDetector.detectLanguage(file);
      if (lang == Language.UNKNOWN) {
        continue; // Only index known code
      }

      Optional<String> contentOpt = provider.getFileContent(repo, file, ref);
      if (contentOpt.isEmpty()) {
        continue;
      }

      String content = contentOpt.get();
      if (content.getBytes().length > gitProperties.getMaxFileSizeBytes()) {
          continue;
      }

      // Delete old chunks for this file
      deleteFileIndex(repo.getId(), file);

      List<String> chunks = codeChunker.chunkCode(content, lang);
      List<CodeChunk> savedChunks = new ArrayList<>();

      for (int i = 0; i < chunks.size(); i++) {
        CodeChunk chunk = CodeChunk.builder()
            .repository(repo)
            .projectId(repo.getProject().getId())
            .filePath(file)
            .language(lang.name())
            .chunkIndex(i)
            .chunkText(chunks.get(i))
            .build();
        savedChunks.add(codeChunkRepository.save(chunk));
      }
      
      filesIndexed++;
      chunksGenerated += savedChunks.size();
      
      // Async generate embeddings using the JdbcTemplate (similar to KnowledgeIndexer)
      for (CodeChunk c : savedChunks) {
          generateAndStoreEmbedding(c.getId(), c.getChunkText());
      }
    }

    gitMetrics.recordFilesIndexed(filesIndexed);
    gitMetrics.recordEmbeddingsGenerated(chunksGenerated);
  }

  @Async
  @Override
  public CompletableFuture<Void> indexRepositoryAsync(UUID repositoryId) {
    indexRepository(repositoryId);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  @Transactional
  public void deleteRepositoryIndex(UUID repositoryId) {
    codeChunkRepository.deleteByRepositoryId(repositoryId);
  }

  @Transactional
  protected void deleteFileIndex(UUID repositoryId, String filePath) {
    codeChunkRepository.deleteByRepositoryIdAndFilePath(repositoryId, filePath);
  }
  
  @Async
  protected void generateAndStoreEmbedding(UUID chunkId, String text) {
      try {
          float[] vector = embeddingProvider.embed(text);
          if (vector == null || vector.length == 0) return;
          
          StringBuilder sb = new StringBuilder();
          sb.append("[");
          for (int i = 0; i < vector.length; i++) {
              sb.append(vector[i]);
              if (i < vector.length - 1) sb.append(",");
          }
          sb.append("]");
          String vectorStr = sb.toString();
          
          jdbcTemplate.update(
              "UPDATE code_chunks SET embedding = ?::vector WHERE id = ?",
              vectorStr, chunkId
          );
      } catch (Exception e) {
          log.warn("Failed to generate embedding for code chunk {}", chunkId, e);
      }
  }
}
