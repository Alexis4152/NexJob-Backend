package com.nexjob.platform.repository;

import com.nexjob.platform.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsActiveTrueOrderByNameAsc();
    Page<Category> findByIsActiveTrue(Pageable pageable);
    Page<Category> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Category> findBySlugAndIsActiveTrue(String slug);
    Optional<Category> findBySlug(String slug);
}
