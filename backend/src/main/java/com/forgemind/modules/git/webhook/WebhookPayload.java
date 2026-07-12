package com.forgemind.modules.git.webhook;

import com.forgemind.modules.git.provider.GitProviderType;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookPayload {
  private UUID repositoryId;
  private GitProviderType provider;
  private WebhookEventType eventType;
  private String rawPayload;
}
