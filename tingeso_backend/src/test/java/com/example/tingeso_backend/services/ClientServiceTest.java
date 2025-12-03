package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getClientById_returnsClient() {
        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setName("Nombre");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientEntity result = clientService.getClientById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nombre", result.getName());
        verify(clientRepository).findById(1L);
    }

    @Test
    void saveEmployee_returnsSavedEntity() {
        ClientEntity input = new ClientEntity();
        input.setName("Nuevo");

        ClientEntity saved = new ClientEntity();
        saved.setId(2L);
        saved.setName("Nuevo");

        when(clientRepository.save(any(ClientEntity.class))).thenReturn(saved);

        ClientEntity result = clientService.saveEmployee(input);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Nuevo", result.getName());
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void deleteClient_existingId_returnsTrue() throws Exception {
    
        doNothing().when(clientRepository).deleteById(1L);

        boolean result = clientService.deleteClient(1L);

        assertTrue(result);
        verify(clientRepository).deleteById(1L);
    }
}
