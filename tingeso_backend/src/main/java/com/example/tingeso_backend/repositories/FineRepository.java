package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.FineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<FineEntity, Long> {

    FineEntity findByLoanId(Long loanId);

    @Query("SELECT f FROM FineEntity f WHERE f.loan.client.id = :clientId AND f.status = 'Pendiente'")
    List<FineEntity> findPendingFinesByClientId(@Param("clientId") Long clientId);

    @Query("SELECT f FROM FineEntity f WHERE f.loan.client.keycloakId = :keycloakId")
    List<FineEntity> findByUserKeycloakId(@Param("keycloakId") String keycloakId);

}