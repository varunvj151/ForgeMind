package com.forgemind.modules.webhooks.service;

import com.forgemind.modules.webhooks.entity.DeliveryStatus;
import com.forgemind.modules.webhooks.entity.WebhookDelivery;
import com.forgemind.modules.webhooks.entity.WebhookEndpoint;
import com.forgemind.modules.webhooks.repository.WebhookDeliveryRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Handles HTTP delivery of webhooks with HMAC-SHA256 signatures and retries. */
@Slf4j
@Service
public class WebhookDispatcher {

  private static final int MAX_RETRIES = 5;
  private static final int TIMEOUT_SECONDS = 10;
  private static final String SIGNATURE_HEADER = "X-ForgeMind-Signature";
  
  private final WebhookDeliveryRepository deliveryRepository;
  private final HttpClient httpClient;

  public WebhookDispatcher(WebhookDeliveryRepository deliveryRepository) {
    this.deliveryRepository = deliveryRepository;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .build();
  }

  /** Asynchronously dispatches a webhook event. */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void dispatch(WebhookEndpoint endpoint, String eventType, UUID eventId, String payload) {
    WebhookDelivery delivery = WebhookDelivery.builder()
        .webhookEndpoint(endpoint)
        .eventId(eventId)
        .eventType(eventType)
        .requestPayload(payload)
        .deliveryStatus(DeliveryStatus.RETRYING)
        .attemptCount(0)
        .build();
    
    delivery = deliveryRepository.save(delivery);
    attemptDelivery(delivery, endpoint.getUrl(), endpoint.getSecret());
  }

  /**
   * Attempts to send the HTTP request and updates the delivery status.
   * Can be called by a retry scheduled job for failed deliveries.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void attemptDelivery(WebhookDelivery delivery, String url, String secret) {
    delivery.setAttemptCount(delivery.getAttemptCount() + 1);
    String payload = delivery.getRequestPayload();
    String signature = generateHmacSha256(payload, secret);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .header("Content-Type", "application/json")
        .header(SIGNATURE_HEADER, signature)
        .header("X-ForgeMind-Event", delivery.getEventType())
        .header("X-ForgeMind-Delivery", delivery.getId().toString())
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      delivery.setResponseStatus(response.statusCode());
      // Truncate response body if it's too large
      String body = response.body();
      if (body != null && body.length() > 2000) {
        body = body.substring(0, 2000) + "...";
      }
      delivery.setResponseBody(body);

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        delivery.setDeliveryStatus(DeliveryStatus.SUCCESS);
        delivery.setNextRetryAt(null);
        log.debug("Webhook delivered successfully: {}", delivery.getId());
      } else {
        handleFailure(delivery);
      }
    } catch (Exception e) {
      delivery.setResponseStatus(null);
      delivery.setResponseBody(e.getMessage());
      handleFailure(delivery);
      log.warn("Webhook delivery failed: {}", delivery.getId(), e);
    }
    
    deliveryRepository.save(delivery);
  }

  private void handleFailure(WebhookDelivery delivery) {
    if (delivery.getAttemptCount() >= MAX_RETRIES) {
      delivery.setDeliveryStatus(DeliveryStatus.FAILED);
      delivery.setNextRetryAt(null);
    } else {
      delivery.setDeliveryStatus(DeliveryStatus.RETRYING);
      // Exponential backoff: 1m, 5m, 25m, 125m...
      long backoffMinutes = (long) Math.pow(5, delivery.getAttemptCount() - 1);
      delivery.setNextRetryAt(Instant.now().plusSeconds(backoffMinutes * 60));
    }
  }

  private String generateHmacSha256(String data, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKeySpec);
      byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return "sha256=" + Base64.getEncoder().encodeToString(hmacBytes);
    } catch (Exception e) {
      log.error("Failed to generate HMAC signature", e);
      return "";
    }
  }
}
