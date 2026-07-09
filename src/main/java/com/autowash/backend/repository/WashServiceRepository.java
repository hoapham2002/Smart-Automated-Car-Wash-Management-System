package com.autowash.backend.repository;

import com.autowash.backend.entity.WashService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for the service CATALOG entity (com.autowash.entity.WashService -
 * see that class's javadoc for the naming-collision notice with
 * com.autowash.service.WashService, the business-logic interface).
 */
public interface WashServiceRepository extends JpaRepository<WashService, UUID> {

    List<WashService> findByActiveTrueOrderBySortOrderAsc();
}
