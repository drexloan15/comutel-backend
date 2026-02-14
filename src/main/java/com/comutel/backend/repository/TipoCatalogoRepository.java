package com.comutel.backend.repository;

import com.comutel.backend.model.TipoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoCatalogoRepository extends JpaRepository<TipoCatalogo, Long> {
    List<TipoCatalogo> findAllByOrderByNombreAsc();
    List<TipoCatalogo> findByActivoTrueOrderByNombreAsc();
}
