package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.services.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ToolController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ToolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToolService toolService;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void listTools_returnsList() throws Exception {
        ToolEntity t1 = makeTool(1L, "Martillo");
        ToolEntity t2 = makeTool(2L, "Destornillador");
        ArrayList<ToolEntity> list = new ArrayList<>(Arrays.asList(t1, t2));

        when(toolService.getTools()).thenReturn(list);

        mockMvc.perform(get("/api/v1/tools/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(toolService).getTools();
    }

    @Test
    public void getToolById_returnsTool() throws Exception {
        ToolEntity t = makeTool(10L, "Sierra");
        when(toolService.getToolById(10L)).thenReturn(t);

        mockMvc.perform(get("/api/v1/tools/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Sierra")));

        verify(toolService).getToolById(10L);
    }

    @Test
    public void saveTool_createsTool() throws Exception {
        ToolEntity payload = makeTool(null, "Taladro");
        ToolEntity saved = makeTool(20L, "Taladro");
        saved.setStatus("Disponible");

        when(toolService.saveTool(any(ToolEntity.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/tools/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(20)))
                .andExpect(jsonPath("$.name", is("Taladro")))
                .andExpect(jsonPath("$.status", is("Disponible")));

        verify(toolService).saveTool(any(ToolEntity.class));
    }
}
