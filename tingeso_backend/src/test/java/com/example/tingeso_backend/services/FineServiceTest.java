package com.example.tingeso_backend.services;

import com.example.tingeso_backend.dto.FineDTO;
import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.FineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private FineService fineService;

    private FineEntity buildFine(Long id, String clientName, String clientKeycloak, String toolName, int amount, String status) {
        ClientEntity client = new ClientEntity();
        client.setId(id != null ? id : 1L);
        client.setName(clientName);
        client.setKeycloakId(clientKeycloak);

        ToolEntity tool = new ToolEntity();
        try { tool.getClass().getMethod("setName", String.class).invoke(tool, toolName); } catch (Exception ignored) {}

        LoanEntity loan = new LoanEntity();
        loan.setId(id != null ? id : 1L);
        loan.setClient(client);
        loan.setTool(tool);

        FineEntity fine = new FineEntity();
        fine.setId(id);
        fine.setLoan(loan);
        fine.setAmount(amount);
        fine.setStatus(status);
        fine.setFineType("Atraso");
        fine.setCreationDate(LocalDate.of(2025,1,1));
        return fine;
    }

    @Test
    void getAllFines_returnsMappedDTOs() {
        FineEntity f1 = buildFine(1L, "Cliente A", "kc-a", "Taladro A", 100, "Pendiente");
        FineEntity f2 = buildFine(2L, "Cliente B", "kc-b", "Sierra B", 50, "Pagado");

        when(fineRepository.findAll()).thenReturn(List.of(f1, f2));

        List<FineDTO> result = fineService.getAllFines();

        assertThat(result).hasSize(2);
        FineDTO dto1 = result.stream().filter(d -> d.getId().equals(1L)).findFirst().orElseThrow();
        assertThat(dto1.getLoanId()).isEqualTo(1L);
        assertThat(dto1.getClientName()).isEqualTo("Cliente A");
        assertThat(dto1.getToolName()).isEqualTo("Taladro A");
        assertThat(dto1.getAmount()).isEqualTo(100);
        assertThat(dto1.getStatus()).isEqualTo("Pendiente");

        verify(fineRepository).findAll();
    }

    @Test
    void getFinesForUser_callsRepositoryAndMaps() {
        FineEntity f = buildFine(3L, "Cliente C", "kc-user", "Herramienta C", 75, "Pendiente");

        when(fineRepository.findByUserKeycloakId("kc-user")).thenReturn(List.of(f));

        List<FineDTO> result = fineService.getFinesForUser("kc-user");

        assertThat(result).hasSize(1);
        FineDTO dto = result.get(0);
        assertThat(dto.getClientName()).isEqualTo("Cliente C");
        assertThat(dto.getToolName()).isEqualTo("Herramienta C");
        assertThat(dto.getLoanId()).isEqualTo(3L);

        verify(fineRepository).findByUserKeycloakId("kc-user");
    }

    @Test
    void payFine_updatesStatusSetsPaymentDateAndSaves() {
        FineEntity fine = buildFine(4L, "Cliente D", "kc-d", "Taladro D", 40, "Pendiente");
        Long clientId = fine.getLoan().getClient().getId();

        when(fineRepository.findById(4L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(FineEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(fineRepository.findPendingFinesByClientId(clientId)).thenReturn(List.of()); // simula que no quedan pendientes

        fineService.payFine(4L);

        ArgumentCaptor<FineEntity> captor = ArgumentCaptor.forClass(FineEntity.class);
        verify(fineRepository).save(captor.capture());
        FineEntity saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(4L);
        assertThat(saved.getStatus()).isEqualTo("Pagada");
        assertThat(saved.getPaymentDate()).isNotNull();
        assertThat(saved.getPaymentDate()).isEqualTo(LocalDate.now());

        verify(fineRepository).findById(4L);
        verify(fineRepository).findPendingFinesByClientId(clientId);
    }
}