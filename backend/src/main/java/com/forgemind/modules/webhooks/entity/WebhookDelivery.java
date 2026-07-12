package com.forgemind.modules.webhooks.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "webhook_endpoint_id", nullable = false)
  private WebhookEndpoint webhookEndpoint;

  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "request_payload", nullable = false)
  private String requestPayload; // JSON string

  @Column(name = "response_status")
  private Integer responseStatus;

  @Column(name = "response_body", columnDefinition = "TEXT")
  private String responseBody;

  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_status", nullable = false, length = 50)
  private DeliveryStatus deliveryStatus;

  @Column(name = "attempt_count", nullable = false)
  @Builder.Default
  private int attemptCount = 1;

  @Column(name = "next_retry_at")
  private Instant nextRetryAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
