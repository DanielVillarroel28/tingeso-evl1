package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.KardexEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.KardexRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexServiceTest {

    @Mock
    private KardexRepository kardexRepository;

    @InjectMocks
    private KardexService kardexService;

    @Captor
    private ArgumentCaptor<KardexEntity> kardexCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateTimeCaptor;

    @Test
    void createNewToolMovement_savesIngresoMovement() {
        ToolEntity tool = new ToolEntity();
        tool.setName("TaladroTest");

        kardexService.createNewToolMovement(tool, "usuarioX");

        verify(kardexRepository, times(1)).save(kardexCaptor.capture());
        KardexEntity saved = kardexCaptor.getValue();

        assertNotNull(saved);
        assertEquals("Ingreso", saved.getMovementType());
        assertEquals(1, saved.getQuantityAffected());
        assertEquals("usuarioX", saved.getUserResponsible());
        assertSame(tool, saved.getTool());
        assertNotNull(saved.getMovementDate());
    }

    @Test
    void getMovements_withToolName_delegatesToFindByToolNameIgnoreCase() {
        List<KardexEntity> expected = List.of(new KardexEntity());
        when(kardexRepository.findByToolNameIgnoreCase("TaladroX")).thenReturn(expected);

        List<KardexEntity> result = kardexService.getMovements("TaladroX", null, null);

        verify(kardexRepository, times(1)).findByToolNameIgnoreCase("TaladroX");
        assertSame(expected, result);
    }

    @Test
    void getMovements_withToolNameAndDateRange_delegatesToCombinedRepositoryMethod_withConvertedDateTimes() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        List<KardexEntity> expected = List.of(new KardexEntity());
        when(kardexRepository.findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan(
                eq("taladrocombo"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expected);

        List<KardexEntity> result = kardexService.getMovements("taladrocombo", start, end);

        // capturar los LocalDateTime pasados al repositorio
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(kardexRepository, times(1))
                .findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan(
                        eq("taladrocombo"), startCaptor.capture(), endCaptor.capture());

        LocalDateTime expectedStart = start.atStartOfDay();
        LocalDateTime expectedEnd = end.plusDays(1).atStartOfDay();

        assertEquals(expectedStart, startCaptor.getValue());
        assertEquals(expectedEnd, endCaptor.getValue());
        assertSame(expected, result);
    }
}
