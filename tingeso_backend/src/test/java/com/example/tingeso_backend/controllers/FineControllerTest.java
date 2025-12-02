package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.dto.FineDTO;
import com.example.tingeso_backend.services.FineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FineControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private FineService fineService;

    @InjectMocks
    private FineController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllFines_success() throws Exception {
        FineDTO f1 = new FineDTO();
        f1.setId(1L);
        f1.setAmount(100); // amount es int
        f1.setStatus("UNPAID"); // reemplaza setPaid

        FineDTO f2 = new FineDTO();
        f2.setId(2L);
        f2.setAmount(50);
        f2.setStatus("PAID");

        List<FineDTO> fines = List.of(f1, f2);

        when(fineService.getAllFines()).thenReturn(fines);

        mockMvc.perform(get("/api/v1/fines/"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(fines)));

        verify(fineService).getAllFines();
    }

    @Test
    void getMyFines_success() throws Exception {
        FineDTO f = new FineDTO();
        f.setId(3L);
        f.setAmount(75);
        f.setStatus("UNPAID");

        List<FineDTO> fines = List.of(f);

        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn("user-keycloak-id");

        when(fineService.getFinesForUser("user-keycloak-id")).thenReturn(fines);

        mockMvc.perform(get("/api/v1/fines/my-fines").principal(principal))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(fines)));

        verify(fineService).getFinesForUser("user-keycloak-id");
    }

    @Test
    void payFine_success() throws Exception {
        doNothing().when(fineService).payFine(1L);

        mockMvc.perform(put("/api/v1/fines/1/pay"))
                .andExpect(status().isOk())
                .andExpect(content().string("Multa pagada exitosamente."));

        verify(fineService).payFine(1L);
    }
}
