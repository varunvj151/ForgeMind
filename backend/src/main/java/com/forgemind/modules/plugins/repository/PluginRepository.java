package com.forgemind.modules.plugins.repository;

import com.forgemind.modules.plugins.entity.Plugin;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, UUID> {
  
  List<Plugin> findAllByOrganizationId(UUID organizationId);
  
  List<Plugin> findAllByOrganizationIdAndActiveTrue(UUID organizationId);
}
