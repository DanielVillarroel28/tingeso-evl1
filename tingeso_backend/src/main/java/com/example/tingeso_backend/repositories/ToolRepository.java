package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ToolRepository extends JpaRepository<ToolEntity, Long> {

}
