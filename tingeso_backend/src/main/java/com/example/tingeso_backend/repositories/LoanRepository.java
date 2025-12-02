package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    int countByClientIdAndStatus(Long clientId, String status);

    boolean existsByClientIdAndToolIdAndStatus(Long clientId, Long toolId, String status);

    List<LoanEntity> findByClientIdAndDueDateBeforeAndStatus(Long clientId, LocalDate today, String status);

    List<LoanEntity> findByClientKeycloakId(String keycloakId);
}