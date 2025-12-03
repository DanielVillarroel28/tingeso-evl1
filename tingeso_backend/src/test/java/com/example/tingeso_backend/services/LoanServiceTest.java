package com.example.tingeso_backend.services;

import com.example.tingeso_backend.dto.LoanDTO;
import com.example.tingeso_backend.dto.ReturnRequestDTO;
import com.example.tingeso_backend.dto.LoanWithFineInfoDTO;
import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import com.example.tingeso_backend.repositories.FineRepository;
import com.example.tingeso_backend.repositories.LoanRepository;
import com.example.tingeso_backend.repositories.ToolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ToolRepository toolRepository;
    @Mock
    private KardexService kardexService;
    @Mock
    private FineService fineService;
    @Mock
    private FineRepository fineRepository;
    @Mock
    private ClientService clientService;

    @InjectMocks
    private LoanService loanService;

    private ClientEntity makeClient(Long id) {
        ClientEntity c = new ClientEntity();
        c.setId(id);
        c.setName("Cliente Test");
        c.setStatus("Activo");
        return c;
    }

    private ToolEntity makeTool(Long id) {
        ToolEntity t = new ToolEntity();
        t.setId(id);
        t.setName("Herramienta Test");
        t.setStatus("Disponible");
        t.setReplacementValue(100);
        return t;
    }

    @Test
    public void createLoan_adminProvidedClientId_createsLoanSuccessfully() {
        // Arrange
        ClientEntity client = makeClient(10L);
        ToolEntity tool = makeTool(20L);

        LoanDTO dto = new LoanDTO();
        dto.setClientId(client.getId());
        dto.setToolId(tool.getId());
        dto.setDueDate(LocalDate.now().plusDays(5));

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(toolRepository.findById(tool.getId())).thenReturn(Optional.of(tool));
        when(loanRepository.findByClientIdAndDueDateBeforeAndStatus(eq(client.getId()), any(LocalDate.class), eq("Activo")))
                .thenReturn(Collections.emptyList());
        when(fineService.hasPendingFines(client.getId())).thenReturn(false);
        when(loanRepository.countByClientIdAndStatus(client.getId(), "Activo")).thenReturn(0);
        when(loanRepository.existsByClientIdAndToolIdAndStatus(client.getId(), tool.getId(), "Activo")).thenReturn(false);

        LoanEntity saved = new LoanEntity();
        saved.setId(1L);
        saved.setClient(client);
        saved.setTool(tool);
        saved.setLoanDate(LocalDate.now());
        saved.setDueDate(dto.getDueDate());
        saved.setStatus("Activo");
        when(loanRepository.save(any(LoanEntity.class))).thenReturn(saved);

        // Act
        LoanEntity result = loanService.createLoan(dto, mock(JwtAuthenticationToken.class));

        // Assert
        assertNotNull(result);
        assertEquals("Activo", result.getStatus());
        assertEquals(client.getId(), result.getClient().getId());
        verify(toolRepository).save(argThat(t -> "Prestada".equals(t.getStatus())));
        verify(kardexService).createLoanMovement(saved);
    }

    @Test
    public void createLoan_userWithoutClientId_usesPrincipalClient() {
        // Arrange
        ClientEntity client = makeClient(11L);
        ToolEntity tool = makeTool(21L);

        LoanDTO dto = new LoanDTO();
        dto.setClientId(null);
        dto.setToolId(tool.getId());
        dto.setDueDate(LocalDate.now().plusDays(3));

        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(clientService.findOrCreateClient(principal)).thenReturn(client);
        when(toolRepository.findById(tool.getId())).thenReturn(Optional.of(tool));
        when(loanRepository.findByClientIdAndDueDateBeforeAndStatus(eq(client.getId()), any(LocalDate.class), eq("Activo")))
                .thenReturn(Collections.emptyList());
        when(fineService.hasPendingFines(client.getId())).thenReturn(false);
        when(loanRepository.countByClientIdAndStatus(client.getId(), "Activo")).thenReturn(0);
        when(loanRepository.existsByClientIdAndToolIdAndStatus(client.getId(), tool.getId(), "Activo")).thenReturn(false);

        LoanEntity saved = new LoanEntity();
        saved.setId(2L);
        saved.setClient(client);
        saved.setTool(tool);
        when(loanRepository.save(any(LoanEntity.class))).thenReturn(saved);

        // Act
        LoanEntity result = loanService.createLoan(dto, principal);

        // Assert
        assertNotNull(result);
        assertEquals(client.getId(), result.getClient().getId());
        verify(clientService).findOrCreateClient(principal);
        verify(toolRepository).save(argThat(t -> "Prestada".equals(t.getStatus())));
    }

    @Test
    public void processReturn_irreparableDamage_createsFineAndReturnsDTO() {
        // Arrange
        Long loanId = 100L;
        ClientEntity client = makeClient(30L);
        ToolEntity tool = new ToolEntity();
        tool.setId(40L);
        tool.setName("Taladro");
        tool.setStatus("Prestada");
        tool.setReplacementValue(500);

        LoanEntity loan = new LoanEntity();
        loan.setId(loanId);
        loan.setClient(client);
        loan.setTool(tool);
        loan.setStatus("Activo");
        loan.setLoanDate(LocalDate.now().minusDays(10));
        loan.setDueDate(LocalDate.now().minusDays(1)); // overdue

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        // simulate save returning loan updated
        ArgumentCaptor<LoanEntity> loanCaptor = ArgumentCaptor.forClass(LoanEntity.class);
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // fineRepository should return a fine when buildLoanWithFineInfoDTO is called
        FineEntity fine = new FineEntity();
        fine.setId(55L);
        fine.setAmount((int) 500.0);
        fine.setStatus("Pendiente");
        when(fineRepository.findByLoanId(loanId)).thenReturn(fine);

        // Act
        ReturnRequestDTO req = new ReturnRequestDTO();
        req.setStatus("Irreparable");
        LoanWithFineInfoDTO dto = loanService.processReturn(loanId, req);

        // Assert
        assertNotNull(dto);
        assertEquals("Devuelto", dto.getStatus());
        assertEquals(55L, dto.getFineId());
        // verify kardex and fine service interactions
        verify(fineService).createFineForDamage(eq(loan), eq(tool.getReplacementValue()));
        verify(kardexService).createWriteOffMovement(eq(tool), anyString());
        verify(kardexService).createReturnMovement(eq(loan), anyString());
        verify(fineService).createFineForLateReturn(eq(loan));
        // tool status should be changed to "Dada de baja"
        assertEquals("Dada de baja", tool.getStatus());
    }
}
