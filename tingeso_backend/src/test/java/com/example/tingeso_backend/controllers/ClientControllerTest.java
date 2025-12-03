package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.services.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getClientById_success() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setName("Nombre");

        when(clientService.getClientById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(client)));

        verify(clientService).getClientById(1L);
    }

    @Test
    void createClient_success() throws Exception {
        ClientEntity input = new ClientEntity();
        input.setName("Nuevo");

        ClientEntity saved = new ClientEntity();
        saved.setId(2L);
        saved.setName("Nuevo");

        when(clientService.saveEmployee(any(ClientEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/clients/")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(saved)));

        verify(clientService).saveEmployee(any(ClientEntity.class));
    }

    @Test
    void deleteClient_returnsNoContent_success() throws Exception {
        when(clientService.deleteClient(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(1L);
    }
}
