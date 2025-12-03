// java
package com.example.tingeso_backend.services;

import com.example.tingeso_backend.entities.ConfigurationEntity;
import com.example.tingeso_backend.repositories.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @InjectMocks
    private ConfigurationService configurationService;

    private final String KEY_RENTAL = "daily_rental_fee";

    @BeforeEach
    void setup() {
        // no-op, configurado por MockitoExtension
    }

    @Test
    void getFee_returnsInteger_whenConfigExists() {
        ConfigurationEntity cfg = new ConfigurationEntity();
        cfg.setId(1L);
        cfg.setConfigKey(KEY_RENTAL);
        cfg.setConfigValue("15");

        when(configurationRepository.findByConfigKey(KEY_RENTAL)).thenReturn(Optional.of(cfg));

        int fee = configurationService.getFee(KEY_RENTAL);

        assertEquals(15, fee);
        verify(configurationRepository).findByConfigKey(KEY_RENTAL);
    }

    @Test
    void getFee_throwsRuntimeException_whenConfigMissing() {
        when(configurationRepository.findByConfigKey("missing_key")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> configurationService.getFee("missing_key"));
        assertTrue(ex.getMessage().contains("Configuración no encontrada"));
        verify(configurationRepository).findByConfigKey("missing_key");
    }

    @Test
    void getFee_throwsNumberFormatException_whenValueNotNumeric() {
        ConfigurationEntity cfg = new ConfigurationEntity();
        cfg.setConfigKey(KEY_RENTAL);
        cfg.setConfigValue("not-a-number");

        when(configurationRepository.findByConfigKey(KEY_RENTAL)).thenReturn(Optional.of(cfg));

        assertThrows(NumberFormatException.class, () -> configurationService.getFee(KEY_RENTAL));
        verify(configurationRepository).findByConfigKey(KEY_RENTAL);
    }

    @Test
    void updateFee_updatesExistingConfiguration() {
        ConfigurationEntity existing = new ConfigurationEntity();
        existing.setId(2L);
        existing.setConfigKey(KEY_RENTAL);
        existing.setConfigValue("10");

        when(configurationRepository.findByConfigKey(KEY_RENTAL)).thenReturn(Optional.of(existing));
        when(configurationRepository.save(any(ConfigurationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfigurationEntity res = configurationService.updateFee(KEY_RENTAL, 20);

        assertNotNull(res);
        assertEquals(KEY_RENTAL, res.getConfigKey());
        assertEquals("20", res.getConfigValue());

        ArgumentCaptor<ConfigurationEntity> captor = ArgumentCaptor.forClass(ConfigurationEntity.class);
        verify(configurationRepository).save(captor.capture());
        ConfigurationEntity saved = captor.getValue();
        assertEquals(KEY_RENTAL, saved.getConfigKey());
        assertEquals("20", saved.getConfigValue());
        verify(configurationRepository).findByConfigKey(KEY_RENTAL);
    }

    @Test
    void updateFee_createsNewConfiguration_whenNotExists() {
        when(configurationRepository.findByConfigKey("new_key")).thenReturn(Optional.empty());
        when(configurationRepository.save(any(ConfigurationEntity.class))).thenAnswer(inv -> {
            ConfigurationEntity c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        ConfigurationEntity res = configurationService.updateFee("new_key", 33);

        assertNotNull(res);
        assertEquals(5L, res.getId());
        assertEquals("new_key", res.getConfigKey());
        assertEquals("33", res.getConfigValue());

        ArgumentCaptor<ConfigurationEntity> captor = ArgumentCaptor.forClass(ConfigurationEntity.class);
        verify(configurationRepository).save(captor.capture());
        ConfigurationEntity saved = captor.getValue();
        assertEquals("new_key", saved.getConfigKey());
        assertEquals("33", saved.getConfigValue());
    }

    @Test
    void updateFee_throwsIllegalArgumentException_whenNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> configurationService.updateFee(KEY_RENTAL, -1));
        verify(configurationRepository, never()).save(any());
    }
}
