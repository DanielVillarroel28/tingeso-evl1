// java
package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.dto.LoanDTO;
import com.example.tingeso_backend.dto.LoanWithFineInfoDTO;
import com.example.tingeso_backend.dto.ReturnRequestDTO;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.services.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad para las pruebas de controlador
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    @Test
    void listLoans_returnsOkWithBody() throws Exception {
        LoanWithFineInfoDTO dto = new LoanWithFineInfoDTO();
        dto.setId(1L);
        dto.setClientName("Usuario A");
        dto.setToolName("Taladro");
        when(loanService.getLoansWithFineInfo()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/loans/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
        verify(loanService, times(1)).getLoansWithFineInfo();
    }

    @Test
    void createLoan_withPrincipal_returnsCreated() throws Exception {
        LoanDTO request = new LoanDTO();
        request.setToolId(10L);
        request.setDueDate(LocalDate.now().plusDays(3));

        LoanEntity saved = new LoanEntity();
        saved.setId(42L);
        when(loanService.createLoan(any(LoanDTO.class), any(JwtAuthenticationToken.class))).thenReturn(saved);

        JwtAuthenticationToken principal = Mockito.mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn("user-123");

        mockMvc.perform(post("/api/v1/loans/")
                        .principal(principal)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));

        verify(loanService, times(1)).createLoan(any(LoanDTO.class), eq(principal));
    }

    @Test
    void processReturn_callsService_andReturnsOk() throws Exception {
        Long loanId = 5L;
        ReturnRequestDTO req = new ReturnRequestDTO();
        req.setStatus("Disponible");

        LoanWithFineInfoDTO responseDto = new LoanWithFineInfoDTO();
        responseDto.setId(loanId);
        when(loanService.processReturn(eq(loanId), any(ReturnRequestDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/loans/{id}/return", loanId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId));

        verify(loanService, times(1)).processReturn(eq(loanId), any(ReturnRequestDTO.class));
    }

    @Test
    void deleteLoan_returnsNoContent_whenServiceSucceeds() throws Exception {
        Long loanId = 7L;
        when(loanService.deleteLoan(loanId)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/loans/{id}", loanId))
                .andExpect(status().isNoContent());

        verify(loanService, times(1)).deleteLoan(loanId);
    }

    @Test
    void getMyLoans_usesPrincipal_andReturnsLoans() throws Exception {
        JwtAuthenticationToken principal = Mockito.mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn("kc-abc");

        LoanWithFineInfoDTO dto = new LoanWithFineInfoDTO();
        dto.setId(11L);
        when(loanService.getLoansForUser("kc-abc")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/loans/my-loans").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));

        verify(loanService, times(1)).getLoansForUser("kc-abc");
    }
}
