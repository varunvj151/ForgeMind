package com.forgemind.modules.webhooks.repository;

import com.forgemind.modules.webhooks.entity.DeliveryStatus;
import com.forgemind.modules.webhooks.entity.WebhookDelivery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
  
  List<WebhookDelivery> findAllByWebhookEndpointIdOrderByCreatedAtDesc(UUID endpointId);
  
  List<WebhookDelivery> findByDeliveryStatusAndNextRetryAtBefore(DeliveryStatus status, Instant time);
}
