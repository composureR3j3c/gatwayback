package com.gateway.apigateway.repo;

import com.gateway.apigateway.model.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PluginRepository extends JpaRepository<Plugin, Long> {}
