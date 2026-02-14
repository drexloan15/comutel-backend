package com.comutel.backend.repository;

import com.comutel.backend.model.BrandingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandingConfigRepository extends JpaRepository<BrandingConfig, Long> {
    Optional<BrandingConfig> findFirstByOrderByIdAsc();
}
