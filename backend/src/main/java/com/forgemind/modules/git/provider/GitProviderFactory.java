package com.forgemind.modules.git.provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Factory to resolve the correct GitProvider implementation based on provider type.
 */
@Component
public class GitProviderFactory {

  private final Map<GitProviderType, GitProvider> providers;

  public GitProviderFactory(List<GitProvider> providerList) {
    this.providers = providerList.stream()
        .collect(Collectors.toMap(GitProvider::getType, Function.identity()));
  }

  /**
   * Returns the GitProvider for the given type.
   *
   * @param type the provider type (e.g., GITHUB, GITLAB)
   * @return the corresponding GitProvider implementation
   * @throws IllegalArgumentException if no provider is configured for the type
   */
  public GitProvider getProvider(GitProviderType type) {
    GitProvider provider = providers.get(type);
    if (provider == null) {
      throw new IllegalArgumentException("No GitProvider configured for type: " + type);
    }
    return provider;
  }
}
