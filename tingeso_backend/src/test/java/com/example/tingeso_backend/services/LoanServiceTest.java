// java
package com.example.tingeso_backend.services;

import com.example.tingeso_backend.dto.LoanDTO;
import com.example.tingeso_backend.dto.LoanWithFineInfoDTO;
import com.example.tingeso_backend.dto.ReturnRequestDTO;
import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import com.example.tingeso_backend.repositories.FineRepository;
import com.example.tingeso_backend.repositories.LoanRepository;
import com.example.tingeso_backend.repositories.ToolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void setupDefaults() {
        // por si se necesita alguna configuración común
    }

    @Test
    void getLoansWithFineInfo_mapsFineAndFields() {
        ClientEntity client = makeClient(1L);
        ToolEntity tool = makeTool(2L);
        LoanEntity loan = new LoanEntity();
        loan.setId(10L);
        loan.setClient(client);
        loan.setTool(tool);
        loan.setLoanDate(LocalDate.now().minusDays(5));
        loan.setDueDate(LocalDate.now().plusDays(5));
        loan.setStatus("Activo");

        when(loanRepository.findAll()).thenReturn(Collections.singletonList(loan));

        FineEntity fine = new FineEntity();
        fine.setId(99L);
        fine.setAmount(200);
        fine.setStatus("Pagada");
        when(fineRepository.findByLoanId(10L)).thenReturn(fine);

        var dtoList = loanService.getLoansWithFineInfo();
        assertNotNull(dtoList);
        assertEquals(1, dtoList.size());
        LoanWithFineInfoDTO dto = dtoList.get(0);
        assertEquals(10L, dto.getId());
        assertEquals("Cliente Test", dto.getClientName());
        assertEquals("Herramienta Test", dto.getToolName());
        assertEquals(99L, dto.getFineId());
        assertEquals(200, dto.getFineAmount());
    }

    @Test
    void createLoan_adminProvidedClientId_createsLoanSuccessfully() {
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

        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> {
            LoanEntity l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        LoanEntity result = loanService.createLoan(dto, mock(JwtAuthenticationToken.class));

        assertNotNull(result);
        assertEquals("Activo", result.getStatus());
        assertEquals(client.getId(), result.getClient().getId());
        verify(toolRepository).save(argThat(t -> "Prestada".equals(t.getStatus())));
        verify(kardexService).createLoanMovement(any(LoanEntity.class));
    }

    @Test
    void createLoan_userWithoutClientId_usesPrincipalClient() {
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

        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> {
            LoanEntity l = inv.getArgument(0);
            l.setId(2L);
            return l;
        });

        LoanEntity result = loanService.createLoan(dto, principal);

        assertNotNull(result);
        assertEquals(client.getId(), result.getClient().getId());
        verify(clientService).findOrCreateClient(principal);
        verify(toolRepository).save(argThat(t -> "Prestada".equals(t.getStatus())));
    }

    @Test
    void createLoan_toolNotAvailable_throws() {
        ClientEntity client = makeClient(12L);
        ToolEntity tool = makeTool(22L);
        tool.setStatus("Prestada");

        LoanDTO dto = new LoanDTO();
        dto.setClientId(client.getId());
        dto.setToolId(tool.getId());
        dto.setDueDate(LocalDate.now().plusDays(2));

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(toolRepository.findById(tool.getId())).thenReturn(Optional.of(tool));

        Exception ex = assertThrows(RuntimeException.class, () -> loanService.createLoan(dto, mock(JwtAuthenticationToken.class)));
        assertTrue(ex.getMessage().toLowerCase().contains("no está disponible") || ex.getMessage().toLowerCase().contains("no está disponible"));
    }

    @Test
    void createLoan_clientHasPendingFines_throws() {
        ClientEntity client = makeClient(13L);
        ToolEntity tool = makeTool(23L);

        LoanDTO dto = new LoanDTO();
        dto.setClientId(client.getId());
        dto.setToolId(tool.getId());
        dto.setDueDate(LocalDate.now().plusDays(2));

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(toolRepository.findById(tool.getId())).thenReturn(Optional.of(tool));
        when(loanRepository.findByClientIdAndDueDateBeforeAndStatus(eq(client.getId()), any(LocalDate.class), eq("Activo")))
                .thenReturn(Collections.emptyList());
        when(fineService.hasPendingFines(client.getId())).thenReturn(true);

        Exception ex = assertThrows(RuntimeException.class, () -> loanService.createLoan(dto, mock(JwtAuthenticationToken.class)));
        assertTrue(ex.getMessage().toLowerCase().contains("multas"));
    }

    @Test
    void processReturn_irreparableDamage_createsFineAndReturnsDTO() {
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
        loan.setDueDate(LocalDate.now().minusDays(1));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        FineEntity fine = new FineEntity();
        fine.setId(55L);
        fine.setAmount(500);
        fine.setStatus("Pendiente");
        when(fineRepository.findByLoanId(loanId)).thenReturn(fine);

        ReturnRequestDTO req = new ReturnRequestDTO();
        req.setStatus("Irreparable");
        LoanWithFineInfoDTO dto = loanService.processReturn(loanId, req);

        assertNotNull(dto);
        assertEquals("Devuelto", dto.getStatus());
        assertEquals(55L, dto.getFineId());
        verify(fineService).createFineForDamage(eq(loan), eq(tool.getReplacementValue()));
        verify(kardexService).createWriteOffMovement(eq(tool), anyString());
        verify(kardexService).createReturnMovement(eq(loan), anyString());
        verify(fineService).createFineForLateReturn(eq(loan));
        assertEquals("Dada de baja", tool.getStatus());
    }

    @Test
    void processReturn_damaged_createsRepairFine_and_setsRepairStatus() {
        Long loanId = 101L;
        ClientEntity client = makeClient(31L);
        ToolEntity tool = makeTool(41L);
        tool.setStatus("Prestada");

        LoanEntity loan = new LoanEntity();
        loan.setId(loanId);
        loan.setClient(client);
        loan.setTool(tool);
        loan.setStatus("Activo");
        loan.setLoanDate(LocalDate.now().minusDays(6));
        loan.setDueDate(LocalDate.now().minusDays(1));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.findByLoanId(loanId)).thenReturn(null);

        ReturnRequestDTO req = new ReturnRequestDTO();
        req.setStatus("Dañada");
        LoanWithFineInfoDTO dto = loanService.processReturn(loanId, req);

        assertNotNull(dto);
        assertEquals("Devuelto", dto.getStatus());
        verify(fineService).createFineForRepairableDamage(eq(loan));
        verify(kardexService).createRepairMovement(eq(tool), anyString());
        verify(kardexService).createReturnMovement(eq(loan), anyString());
        assertEquals("En reparación", tool.getStatus());
    }

    @Test
    void processReturn_normal_setsAvailable_and_noDamageFine() {
        Long loanId = 102L;
        ClientEntity client = makeClient(32L);
        ToolEntity tool = makeTool(42L);
        tool.setStatus("Prestada");

        LoanEntity loan = new LoanEntity();
        loan.setId(loanId);
        loan.setClient(client);
        loan.setTool(tool);
        loan.setStatus("Activo");
        loan.setLoanDate(LocalDate.now().minusDays(2));
        loan.setDueDate(LocalDate.now().plusDays(1));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.findByLoanId(loanId)).thenReturn(null);

        ReturnRequestDTO req = new ReturnRequestDTO();
        req.setStatus("Bueno");
        LoanWithFineInfoDTO dto = loanService.processReturn(loanId, req);

        assertNotNull(dto);
        assertEquals("Devuelto", dto.getStatus());
        verify(fineService, never()).createFineForDamage(any(), anyInt());
        verify(fineService).createFineForLateReturn(eq(loan));
        verify(kardexService).createReturnMovement(eq(loan), anyString());
        assertEquals("Disponible", tool.getStatus());
    }

    @Test
    void updateLoan_savesAndReturns() {
        LoanEntity loan = new LoanEntity();
        loan.setId(200L);
        loan.setStatus("Activo");

        when(loanRepository.save(loan)).thenReturn(loan);

        LoanEntity res = loanService.updateLoan(loan);
        assertEquals(200L, res.getId());
        verify(loanRepository).save(loan);
    }

    @Test
    void deleteLoan_success_whenAlreadyReturned() throws Exception {
        Long id = 300L;
        LoanEntity loan = new LoanEntity();
        loan.setId(id);
        loan.setReturnDate(LocalDate.now().minusDays(1));

        when(loanRepository.findById(id)).thenReturn(Optional.of(loan));
        doNothing().when(loanRepository).deleteById(id);

        boolean deleted = loanService.deleteLoan(id);
        assertTrue(deleted);
        verify(loanRepository).deleteById(id);
    }

    @Test
    void deleteLoan_throws_whenNotReturned() {
        Long id = 301L;
        LoanEntity loan = new LoanEntity();
        loan.setId(id);
        loan.setReturnDate(null);

        when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        Exception ex = assertThrows(Exception.class, () -> loanService.deleteLoan(id));
        assertTrue(ex.getMessage().toLowerCase().contains("aún no ha sido devuelto") || ex.getMessage().toLowerCase().contains("no se puede eliminar"));
    }

    @Test
    void getLoansForUser_mapsLoansForKeycloakId() {
        String keycloakId = "kc-abc";
        ClientEntity client = makeClient(400L);
        client.setKeycloakId(keycloakId);
        ToolEntity tool = makeTool(401L);

        LoanEntity loan = new LoanEntity();
        loan.setId(500L);
        loan.setClient(client);
        loan.setTool(tool);
        loan.setLoanDate(LocalDate.now().minusDays(1));
        loan.setDueDate(LocalDate.now().plusDays(4));
        when(loanRepository.findByClientKeycloakId(keycloakId)).thenReturn(Collections.singletonList(loan));
        when(fineRepository.findByLoanId(500L)).thenReturn(null);

        var list = loanService.getLoansForUser(keycloakId);
        assertEquals(1, list.size());
        assertEquals(500L, list.get(0).getId());
    }

}
