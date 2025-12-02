package com.example.tingeso_backend.services;

import com.example.tingeso_backend.dto.FineDTO;
import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.repositories.FineRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FineService {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private ConfigurationService configurationService;


    public List<FineDTO> getAllFines() {
        return fineRepository.findAll().stream()
                .map(this::buildFineDTO) // Usa un método helper para la conversión
                .collect(Collectors.toList());
    }

    // MÉTODO HELPER NUEVO: Convierte una FineEntity a FineDTO
    private FineDTO buildFineDTO(FineEntity fine) {
        FineDTO dto = new FineDTO();
        dto.setId(fine.getId());
        dto.setLoanId(fine.getLoan().getId());
        dto.setClientName(fine.getLoan().getClient().getName()); // Aplanando los datos
        dto.setToolName(fine.getLoan().getTool().getName());     // Aplanando los datos
        dto.setFineType(fine.getFineType());
        dto.setAmount(fine.getAmount());
        dto.setStatus(fine.getStatus());
        dto.setCreationDate(fine.getCreationDate());
        dto.setPaymentDate(fine.getPaymentDate());
        return dto;
    }


    public void createFineForLateReturn(LoanEntity loan) {
        // Asegurarse de que la fecha de devolución no sea nula
        if (loan.getReturnDate() == null) {
            return; // No se puede calcular multa si no ha sido devuelto
        }

        // Comprobar si la devolución fue después de la fecha límite
        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            // Calcular los días de atraso
            long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());

            if (overdueDays > 0) {
                // Obtener la tarifa por día de multa desde la configuración
                int lateFeePerDay = configurationService.getFee("daily_late_fee");

                // Calcular el monto total de la multa
                int totalFineAmount = (int) overdueDays * lateFeePerDay;

                // Crear la nueva entidad de multa
                FineEntity fine = new FineEntity();
                fine.setLoan(loan);
                fine.setFineType("Atraso");
                fine.setAmount(totalFineAmount);
                fine.setCreationDate(LocalDate.now());
                fine.setStatus("Pendiente");

                // Actualizar la relación en el préstamo para mantener la consistencia
                if (loan.getFines() == null) {
                    loan.setFines(new ArrayList<>());
                }
                loan.getFines().add(fine);

                ClientEntity client = loan.getClient();
                client.setStatus("Restringido");
            }
        }
    }

    public void createFineForDamage(LoanEntity loan, int amount) {
        FineEntity fine = new FineEntity();
        fine.setLoan(loan);
        fine.setFineType("Daño irreparable");
        fine.setAmount(amount); // El monto es el valor de reposición
        fine.setCreationDate(LocalDate.now());
        fine.setStatus("Pendiente");

        if (loan.getFines() == null) loan.setFines(new ArrayList<>());
        loan.getFines().add(fine);

        // Actualizar el estado del cliente
        ClientEntity client = loan.getClient();
        client.setStatus("Restringido");
    }

    // Verifica si un cliente tiene multas pendientes
    public boolean hasPendingFines(Long clientId) {
        return !fineRepository.findPendingFinesByClientId(clientId).isEmpty();
    }

    @Transactional
    public void payFine(Long fineId) {
        // 1. Buscar y actualizar la multa
        FineEntity fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Multa no encontrada"));

        fine.setStatus("Pagada");
        fine.setPaymentDate(LocalDate.now());
        fineRepository.save(fine);

        // 2. Revisar si el cliente puede volver a estado "Activo"
        ClientEntity client = fine.getLoan().getClient();
        boolean hasOtherPendingFines = hasPendingFines(client.getId());

        if (!hasOtherPendingFines) {
            client.setStatus("Activo");
        }
    }

    public void createFineForRepairableDamage(LoanEntity loan) {
        // Obtiene el monto fijo de la configuración
        int repairAmount = configurationService.getFee("repair_fee");

        if (repairAmount > 0) {
            FineEntity fine = new FineEntity();
            fine.setLoan(loan);
            fine.setFineType("Daño reparable");
            fine.setAmount(repairAmount);
            fine.setCreationDate(LocalDate.now());
            fine.setStatus("Pendiente");

            if (loan.getFines() == null) loan.setFines(new ArrayList<>());
            loan.getFines().add(fine);

            ClientEntity client = loan.getClient();
            client.setStatus("Restringido");
        }
    }

    public List<FineDTO> getFinesForUser(String keycloakId) {
        return fineRepository.findByUserKeycloakId(keycloakId).stream()
                .map(this::buildFineDTO)
                .collect(Collectors.toList());
    }
}