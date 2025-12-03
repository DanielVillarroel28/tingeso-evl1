// java
package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ConfigurationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ConfigurationRepositoryTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Test
    void findByConfigKey_returnsEntity_whenExists() {
        ConfigurationEntity cfg = new ConfigurationEntity();
        cfg.setConfigKey("daily_rental_fee");
        cfg.setConfigValue("15");
        configurationRepository.save(cfg);

        Optional<ConfigurationEntity> found = configurationRepository.findByConfigKey("daily_rental_fee");

        assertTrue(found.isPresent());
        assertEquals("15", found.get().getConfigValue());
    }

    @Test
    void findByConfigKey_returnsEmpty_whenNotFound() {
        Optional<ConfigurationEntity> found = configurationRepository.findByConfigKey("non_existent_key");
        assertTrue(found.isEmpty());
    }

    @Test
    void savingDuplicateConfigKey_throwsDueToUniqueConstraint() {
        ConfigurationEntity a = new ConfigurationEntity();
        a.setConfigKey("dup_key");
        a.setConfigValue("1");
        configurationRepository.saveAndFlush(a);

        ConfigurationEntity b = new ConfigurationEntity();
        b.setConfigKey("dup_key");
        b.setConfigValue("2");

        assertThrows(Exception.class, () -> configurationRepository.saveAndFlush(b));
    }
}
