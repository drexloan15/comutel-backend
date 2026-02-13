package com.comutel.backend.repository;

import com.comutel.backend.model.PermisosConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermisosConfigRepository extends JpaRepository<PermisosConfig, Long> {
}
