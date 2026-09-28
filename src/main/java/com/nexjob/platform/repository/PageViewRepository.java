package com.nexjob.platform.repository;

import com.nexjob.platform.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    /** Proyeccion liviana para agregar metricas en memoria (ver AnalyticsServiceImpl):
     * solo lo necesario, sin traer referrer ni el resto de la fila. */
    interface Stats {
        LocalDateTime getCreatedAt();
        String getVisitorId();
        String getPath();
        Integer getDurationSeconds();
    }

    @Query("SELECT p.createdAt AS createdAt, p.visitorId AS visitorId, p.path AS path, p.durationSeconds AS durationSeconds " +
           "FROM PageView p WHERE p.createdAt >= :from")
    List<Stats> findStatsSince(@Param("from") LocalDateTime from);
}
