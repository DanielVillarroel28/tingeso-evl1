// java
package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.entities.ConfigurationEntity;
import com.example.tingeso_backend.services.ConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationService configurationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getRentalFee_returnsValue() throws Exception {
        when(configurationService.getFee("daily_rental_fee")).thenReturn(15);

        mockMvc.perform(get("/config/rental-fee"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));

        verify(configurationService).getFee("daily_rental_fee");
    }

    @Test
    void getLateFee_returnsValue() throws Exception {
        when(configurationService.getFee("daily_late_fee")).thenReturn(7);

        mockMvc.perform(get("/config/late-fee"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));

        verify(configurationService).getFee("daily_late_fee");
    }

    @Test
    void getRepairFee_returnsValue() throws Exception {
        when(configurationService.getFee("repair_fee")).thenReturn(250);

        mockMvc.perform(get("/config/repair-fee"))
                .andExpect(status().isOk())
                .andExpect(content().string("250"));

        verify(configurationService).getFee("repair_fee");
    }

    @Test
    void setRentalFee_callsServiceAndReturnsOk() throws Exception {
        String body = objectMapper.writeValueAsString(new FeeDto(10));
        when(configurationService.updateFee(eq("daily_rental_fee"), eq(10))).thenReturn(new ConfigurationEntity());

        mockMvc.perform(put("/config/rental-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarifa de arriendo actualizada."));

        verify(configurationService).updateFee("daily_rental_fee", 10);
    }

    @Test
    void setLateFee_callsServiceAndReturnsOk() throws Exception {
        String body = objectMapper.writeValueAsString(new FeeDto(5));
        when(configurationService.updateFee(eq("daily_late_fee"), eq(5))).thenReturn(new ConfigurationEntity());

        mockMvc.perform(put("/config/late-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarifa de multa actualizada."));

        verify(configurationService).updateFee("daily_late_fee", 5);
    }

    @Test
    void setRepairFee_callsServiceAndReturnsOk() throws Exception {
        String body = objectMapper.writeValueAsString(new FeeDto(120));
        when(configurationService.updateFee(eq("repair_fee"), eq(120))).thenReturn(new ConfigurationEntity());

        mockMvc.perform(put("/config/repair-fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Cargo por reparación actualizado."));

        verify(configurationService).updateFee("repair_fee", 120);
    }

    // DTO auxiliar pequeño para serializar el cuerpo { "value": X }
    static class FeeDto {
        private final int value;
        FeeDto(int value) { this.value = value; }
        public int getValue() { return value; }
    }
}
