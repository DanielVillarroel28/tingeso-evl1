// java
package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.entities.KardexEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.services.KardexService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KardexController.class)
@WithMockUser
class KardexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KardexService kardexService;

    private KardexEntity buildMovement(Long id, String toolName, String movementType, LocalDateTime date, int qty, String user) {
        ToolEntity tool = new ToolEntity();
        try {
            Method m = tool.getClass().getMethod("setName", String.class);
            m.invoke(tool, toolName);
        } catch (Exception ignored) {}

        KardexEntity k = new KardexEntity();
        k.setId(id);
        k.setTool(tool);
        k.setMovementType(movementType);
        k.setMovementDate(date);
        k.setQuantityAffected(qty);
        k.setUserResponsible(user);
        return k;
    }

    @Test
    void getMovements_noParams_returnsAll() throws Exception {
        KardexEntity m1 = buildMovement(1L, "Taladro A", "Ingreso", LocalDateTime.of(2025,1,1,10,0), 1, "Usuario1");
        KardexEntity m2 = buildMovement(2L, "Sierra B", "Préstamo", LocalDateTime.of(2025,1,2,11,0), -1, "ClienteX");

        when(kardexService.getMovements(null, null, null)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/v1/kardex/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].movementType").value("Ingreso"))
                .andExpect(jsonPath("$[0].tool.name").value("Taladro A"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(kardexService).getMovements(null, null, null);
    }

    @Test
    void getMovements_withToolName_returnsFiltered() throws Exception {
        KardexEntity m = buildMovement(3L, "TaladroFiltro", "Préstamo", LocalDateTime.of(2025,2,1,9,0), -1, "ClienteY");

        when(kardexService.getMovements(eq("TaladroFiltro"), eq(null), eq(null))).thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1/kardex/").param("toolName", "TaladroFiltro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].tool.name").value("TaladroFiltro"))
                .andExpect(jsonPath("$[0].movementType").value("Préstamo"));

        verify(kardexService).getMovements("TaladroFiltro", null, null);
    }

    @Test
    void getMovements_withDateRange_returnsFiltered() throws Exception {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        KardexEntity m = buildMovement(4L, "HerramientaD", "Devolución", LocalDateTime.of(2025,1,15,8,0), 1, "EmpleadoZ");

        when(kardexService.getMovements(eq(null), eq(start), eq(end))).thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1/kardex/")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].movementType").value("Devolución"))
                .andExpect(jsonPath("$[0].tool.name").value("HerramientaD"));

        verify(kardexService).getMovements(null, start, end);
    }
}
