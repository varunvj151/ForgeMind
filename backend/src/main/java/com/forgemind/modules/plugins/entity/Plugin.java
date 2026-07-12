package com.forgemind.modules.plugins.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "plugins")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plugin {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "plugin_id", nullable = false, length = 255)
  private String pluginId;

  @Column(name = "version", nullable = false, length = 50)
  private String version;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "entrypoint", nullable = false, columnDefinition = "TEXT")
  private String entrypoint;

  @Column(name = "active", nullable = false)
  @Builder.Default
  private boolean active = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
