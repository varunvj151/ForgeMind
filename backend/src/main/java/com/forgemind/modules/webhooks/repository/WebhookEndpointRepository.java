package com.forgemind.modules.webhooks.repository;

import com.forgemind.modules.webhooks.entity.WebhookEndpoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {
  
  List<WebhookEndpoint> findAllByOrganizationId(UUID organizationId);

  // PostgreSQL JSONB contains operator @> to check if the 'events' JSON array contains the eventType string.
  @Query(value = "SELECT * FROM webhook_endpoints w WHERE w.organization_id = :organizationId AND w.active = true AND w.events @> CONCAT('\"', :eventType, '\"')\\:\\:jsonb", nativeQuery = true)
  List<WebhookEndpoint> findActiveEndpointsForEvent(UUID organizationId, String eventType);
}
