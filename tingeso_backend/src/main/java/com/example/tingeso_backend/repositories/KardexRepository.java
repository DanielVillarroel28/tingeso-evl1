package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {

    List<KardexEntity> findByToolNameIgnoreCase(String toolName);

    List<KardexEntity> findByMovementDateGreaterThanEqualAndMovementDateLessThan(LocalDateTime startDate, LocalDateTime endDate);

    List<KardexEntity> findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan(String toolName, LocalDateTime startDate, LocalDateTime endDate);
}