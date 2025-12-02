package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.KardexEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    /**
     * Registra el INGRESO de una nueva herramienta al inventario.
     * Cantidad Afectada: +1
     */
    public void createNewToolMovement(ToolEntity tool, String userResponsible) {
        KardexEntity movement = new KardexEntity();
        movement.setTool(tool);
        movement.setMovementType("Ingreso");
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityAffected(1);
        movement.setUserResponsible(userResponsible);
        kardexRepository.save(movement);
    }

    /**
     * Registra el PRÉSTAMO de una herramienta (salida de stock).
     * Cantidad Afectada: -1
     */
    public void createLoanMovement(LoanEntity loan) {
        KardexEntity movement = new KardexEntity();
        movement.setTool(loan.getTool());
        movement.setMovementType("Préstamo");
        movement.setMovementDate(loan.getLoanDate().atStartOfDay());
        movement.setQuantityAffected(-1);
        movement.setUserResponsible(loan.getClient().getName());
        kardexRepository.save(movement);
    }

    /**
     * Registra la DEVOLUCIÓN de una herramienta (reingreso a stock).
     * Cantidad Afectada: +1
     */
    public void createReturnMovement(LoanEntity loan, String userResponsible) {
        KardexEntity movement = new KardexEntity();
        movement.setTool(loan.getTool());
        movement.setMovementType("Devolución");
        movement.setMovementDate(loan.getReturnDate().atStartOfDay());
        movement.setQuantityAffected(1);
        movement.setUserResponsible(userResponsible); // Empleado que procesa la devolución
        kardexRepository.save(movement);
    }

    /**
     * Registra la BAJA de una herramienta (salida permanente de stock).
     * Cantidad Afectada: -1
     */
    public void createWriteOffMovement(ToolEntity tool, String userResponsible) {
        KardexEntity movement = new KardexEntity();
        movement.setTool(tool);
        movement.setMovementType("Baja");
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityAffected(-1);
        movement.setUserResponsible(userResponsible);
        kardexRepository.save(movement);
    }

    /**
     * Registra que una herramienta fue enviada a REPARACIÓN.
     * Este es un movimiento informativo, no afecta el conteo total de stock.
     * Cantidad Afectada: 0
     */
    public void createRepairMovement(ToolEntity tool, String userResponsible) {
        KardexEntity movement = new KardexEntity();
        movement.setTool(tool);
        movement.setMovementType("Reparación");
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantityAffected(0); // No cambia el stock total, solo el estado
        movement.setUserResponsible(userResponsible);
        kardexRepository.save(movement);
    }

    public List<KardexEntity> getMovements(String toolName, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        boolean hasToolName = toolName != null && !toolName.isEmpty();
        boolean hasDateRange = startDateTime != null && endDateTime != null;

        if (hasToolName && hasDateRange) {
            // Llama al nuevo método del repositorio
            return kardexRepository.findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan(toolName, startDateTime, endDateTime);
        } else if (hasToolName) {
            return kardexRepository.findByToolNameIgnoreCase(toolName);
        } else if (hasDateRange) {
            // Llama al nuevo método del repositorio
            return kardexRepository.findByMovementDateGreaterThanEqualAndMovementDateLessThan(startDateTime, endDateTime);
        } else {
            return kardexRepository.findAll();
        }
    }
}