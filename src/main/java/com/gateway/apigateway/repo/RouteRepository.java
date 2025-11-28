package com.gateway.apigateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gateway.apigateway.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {}
