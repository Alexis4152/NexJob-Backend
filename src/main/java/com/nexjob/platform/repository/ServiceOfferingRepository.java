package com.nexjob.platform.repository;

import com.nexjob.platform.entity.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findByProvider_IdAndIsActiveTrue(Long providerId);
    List<ServiceOffering> findByProvider_IdAndCategory_IdAndIsActiveTrue(Long providerId, Long categoryId);
    Page<ServiceOffering> findByProvider_Id(Long providerId, Pageable pageable);
    Optional<ServiceOffering> findByIdAndProvider_Id(Long id, Long providerId);
    Optional<ServiceOffering> findByIdAndIsActiveTrue(Long id);

    // "Tipo especifico" del buscador: no hay tabla de subcategorias, asi que se ofrecen
    // los titulos de servicios reales que ya existen dentro de la categoria elegida.
    @Query("SELECT DISTINCT so.title FROM ServiceOffering so " +
           "WHERE so.category.id = :categoryId AND so.isActive = true ORDER BY so.title")
    List<String> findDistinctActiveTitlesByCategory(@Param("categoryId") Long categoryId);
}
