// java
package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

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
    void getClients_returnsList() {
        ClientEntity c1 = new ClientEntity();
        c1.setId(1L);
        c1.setName("A");
        ClientEntity c2 = new ClientEntity();
        c2.setId(2L);
        c2.setName("B");

        when(clientRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));

        var result = clientService.getClients();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        verify(clientRepository).findAll();
    }

    @Test
    void saveEmployee_savesAndReturns() {
        ClientEntity toSave = new ClientEntity();
        toSave.setName("Nuevo");
        toSave.setEmail("n@x.com");

        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(inv -> {
            ClientEntity c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });

        ClientEntity saved = clientService.saveEmployee(toSave);

        assertNotNull(saved);
        assertEquals(50L, saved.getId());
        assertEquals("Nuevo", saved.getName());
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void getClientById_returnsClient() {
        ClientEntity client = new ClientEntity();
        client.setId(3L);
        client.setName("ById");

        when(clientRepository.findById(3L)).thenReturn(Optional.of(client));

        ClientEntity result = clientService.getClientById(3L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("ById", result.getName());
        verify(clientRepository).findById(3L);
    }

    @Test
    void getClientByRut_returnsClient() {
        ClientEntity client = new ClientEntity();
        client.setId(4L);
        client.setRut("rut-123");
        client.setName("ClienteRut");

        when(clientRepository.findByRut("rut-123")).thenReturn(client);

        ClientEntity result = clientService.getClientByRut("rut-123");

        assertNotNull(result);
        assertEquals("rut-123", result.getRut());
        assertEquals("ClienteRut", result.getName());
        verify(clientRepository).findByRut("rut-123");
    }

    @Test
    void updateClient_success() {
        Long id = 5L;
        ClientEntity existing = new ClientEntity();
        existing.setId(id);
        existing.setName("Old");
        existing.setRut("oldRut");
        existing.setPhone("111");
        existing.setEmail("old@x.com");
        existing.setStatus("Activo");

        ClientEntity details = new ClientEntity();
        details.setName("New");
        details.setRut("newRut");
        details.setPhone("222");
        details.setEmail("new@x.com");
        details.setStatus("Inactivo");

        when(clientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientEntity updated = clientService.updateClient(id, details);

        assertEquals("New", updated.getName());
        assertEquals("newRut", updated.getRut());
        assertEquals("222", updated.getPhone());
        assertEquals("new@x.com", updated.getEmail());
        assertEquals("Inactivo", updated.getStatus());
        verify(clientRepository).findById(id);
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void updateClient_notFound_throws() {
        Long id = 99L;
        ClientEntity details = new ClientEntity();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> clientService.updateClient(id, details));
        verify(clientRepository).findById(id);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClient_success() throws Exception {
        Long id = 7L;
        doNothing().when(clientRepository).deleteById(id);

        boolean result = clientService.deleteClient(id);

        assertTrue(result);
        verify(clientRepository).deleteById(id);
    }

    @Test
    void deleteClient_throwsException() {
        Long id = 8L;
        doThrow(new RuntimeException("db error")).when(clientRepository).deleteById(id);

        assertThrows(Exception.class, () -> clientService.deleteClient(id));
        verify(clientRepository).deleteById(id);
    }

    @Test
    void findOrCreateClient_exists_returnsExisting() {
        String keycloakId = "kc-1";
        ClientEntity existing = new ClientEntity();
        existing.setId(7L);
        existing.setKeycloakId(keycloakId);
        existing.setName("Exist");

        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn(keycloakId);

        when(clientRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existing));

        ClientEntity result = clientService.findOrCreateClient(principal);

        assertEquals(7L, result.getId());
        assertEquals("Exist", result.getName());
        verify(clientRepository).findByKeycloakId(keycloakId);
        verify(clientRepository, never()).save(any());
    }


    @Test
    void getCurrentClient_success() {
        String keycloakId = "kc-me";
        ClientEntity existing = new ClientEntity();
        existing.setId(30L);
        existing.setKeycloakId(keycloakId);
        existing.setName("Me");

        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn(keycloakId);
        when(clientRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existing));

        ClientEntity result = clientService.getCurrentClient(principal);

        assertEquals(30L, result.getId());
        assertEquals("Me", result.getName());
        verify(clientRepository).findByKeycloakId(keycloakId);
    }

    @Test
    void getCurrentClient_notFound_throws() {
        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn("kc-missing");
        when(clientRepository.findByKeycloakId("kc-missing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> clientService.getCurrentClient(principal));
        verify(clientRepository).findByKeycloakId("kc-missing");
    }

    @Test
    void updateCurrentClient_success() {
        String keycloakId = "kc-update";
        ClientEntity existing = new ClientEntity();
        existing.setId(20L);
        existing.setKeycloakId(keycloakId);
        existing.setName("Before");
        existing.setRut("rut-before");
        existing.setPhone("000");
        existing.setEmail("before@x.com");

        ClientEntity details = new ClientEntity();
        details.setName("After");
        details.setRut("rut-after");
        details.setPhone("111");
        details.setEmail("after@x.com");

        JwtAuthenticationToken principal = mock(JwtAuthenticationToken.class);
        when(principal.getName()).thenReturn(keycloakId);
        when(clientRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientEntity updated = clientService.updateCurrentClient(principal, details);

        assertEquals("After", updated.getName());
        assertEquals("rut-after", updated.getRut());
        assertEquals("111", updated.getPhone());
        assertEquals("after@x.com", updated.getEmail());
        verify(clientRepository).findByKeycloakId(keycloakId);
        verify(clientRepository).save(any(ClientEntity.class));
    }
}
