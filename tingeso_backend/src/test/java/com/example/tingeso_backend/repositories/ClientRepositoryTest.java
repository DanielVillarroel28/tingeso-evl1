// java
package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ClientEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void saveAndFindById_success() {
        ClientEntity client = new ClientEntity();
        client.setName("RepoNombre");
        client.setEmail("repo@example.com"); // <- campo requerido

        ClientEntity saved = clientRepository.save(client);

        Optional<ClientEntity> found = clientRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("RepoNombre", found.get().getName());
    }

    @Test
    void deleteById_removesEntity() {
        ClientEntity client = new ClientEntity();
        client.setName("ToDelete");
        client.setEmail("todelete@example.com"); // <- campo requerido

        ClientEntity saved = clientRepository.save(client);
        Long id = saved.getId();

        clientRepository.deleteById(id);

        Optional<ClientEntity> found = clientRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_returnsSavedEntities() {
        ClientEntity a = new ClientEntity();
        a.setName("A");
        a.setEmail("a@example.com"); // <- campo requerido
        ClientEntity b = new ClientEntity();
        b.setName("B");
        b.setEmail("b@example.com"); // <- campo requerido

        clientRepository.save(a);
        clientRepository.save(b);

        List<ClientEntity> all = clientRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}
