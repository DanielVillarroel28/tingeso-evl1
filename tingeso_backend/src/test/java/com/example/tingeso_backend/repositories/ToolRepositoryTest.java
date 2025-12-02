// language: java
package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ToolEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ToolRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ToolRepository toolRepository;

    private ToolEntity makeTool(String name) {
        ToolEntity t = new ToolEntity();
        t.setName(name);
        t.setCategory("Categoria");
        t.setReplacementValue(10); // cumple @Min(1)
        t.setAvailableStock(5);
        t.setStateInitial("Bueno");
        t.setStatus("Disponible");
        return t;
    }

    @Test
    public void saveAndFindById_works() {
        ToolEntity tool = makeTool("Taladro");
        ToolEntity saved = toolRepository.save(tool);
        assertNotNull(saved.getId(), "El id debe ser generado al guardar");

        Optional<ToolEntity> fetched = toolRepository.findById(saved.getId());
        assertTrue(fetched.isPresent(), "Debe existir la herramienta recuperada por id");
        assertEquals("Taladro", fetched.get().getName());
    }

    @Test
    public void findAll_returnsPersistedTools() {
        ToolEntity t1 = makeTool("Martillo");
        ToolEntity t2 = makeTool("Destornillador");
        em.persistAndFlush(t1);
        em.persistAndFlush(t2);

        List<ToolEntity> list = toolRepository.findAll();
        List<String> names = list.stream().map(ToolEntity::getName).collect(Collectors.toList());

        assertTrue(names.contains("Martillo"));
        assertTrue(names.contains("Destornillador"));
        assertTrue(list.size() >= 2, "Debe retornar al menos las dos herramientas persistidas");
    }

    @Test
    public void deleteById_removesEntity() {
        ToolEntity t = makeTool("Llave");
        ToolEntity persisted = em.persistAndFlush(t);
        Long id = persisted.getId();

        toolRepository.deleteById(id);

        Optional<ToolEntity> afterDelete = toolRepository.findById(id);
        assertFalse(afterDelete.isPresent(), "La herramienta debe ser eliminada del repositorio");
    }
}
