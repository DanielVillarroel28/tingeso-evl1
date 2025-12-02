// language: java
package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.ToolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolServiceTest {

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private KardexService kardexService;

    @InjectMocks
    private ToolService toolService;

    private ToolEntity makeTool(Long id, String name) {
        ToolEntity t = new ToolEntity();
        t.setId(id);
        t.setName(name);
        t.setCategory("Categoria");
        t.setReplacementValue(10);
        t.setAvailableStock(5);
        t.setStateInitial("Bueno");
        t.setStatus("Disponible");
        return t;
    }

    @Test
    public void getTools_returnsList() {
        ToolEntity t1 = makeTool(1L, "Martillo");
        ToolEntity t2 = makeTool(2L, "Destornillador");
        ArrayList<ToolEntity> list = new ArrayList<>(Arrays.asList(t1, t2));

        when(toolRepository.findAll()).thenReturn(list);

        ArrayList<ToolEntity> result = toolService.getTools();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(toolRepository).findAll();
    }

    @Test
    public void saveTool_setsStatusAndCallsKardex() {
        ToolEntity input = makeTool(null, "Taladro");
        input.setStatus(null);
        ToolEntity saved = makeTool(10L, "Taladro");
        saved.setStatus("Disponible");

        when(toolRepository.save(any(ToolEntity.class))).thenReturn(saved);

        ToolEntity result = toolService.saveTool(input);

        assertNotNull(result);
        assertEquals("Disponible", result.getStatus());
        verify(toolRepository).save(any(ToolEntity.class));
        verify(kardexService).createNewToolMovement(eq(saved), anyString());
    }

    @Test
    public void logicalDeleteTool_updatesStatusAndCallsKardex() {
        ToolEntity t = makeTool(7L, "Llave");
        t.setStatus("Disponible");
        when(toolRepository.findById(7L)).thenReturn(Optional.of(t));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean ok = toolService.logicalDeleteTool(7L);

        assertTrue(ok);
        ArgumentCaptor<ToolEntity> captor = ArgumentCaptor.forClass(ToolEntity.class);
        verify(toolRepository).save(captor.capture());
        ToolEntity captured = captor.getValue();
        assertEquals("Dada de baja", captured.getStatus());
        verify(kardexService).createWriteOffMovement(eq(captured), anyString());
    }
}
