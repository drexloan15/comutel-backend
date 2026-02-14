package com.comutel.backend.repository;

import com.comutel.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNombreAsc();
    List<Categoria> findByActivaTrueOrderByNombreAsc();
    List<Categoria> findByProcessTypeIgnoreCaseOrderByNombreAsc(String processType);
    List<Categoria> findByProcessTypeIgnoreCaseAndActivaTrueOrderByNombreAsc(String processType);
}
