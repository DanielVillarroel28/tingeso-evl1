// java
package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.KardexEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class KardexRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private KardexRepository kardexRepository;

    private ToolEntity persistTool(String name) {
        ToolEntity tool = new ToolEntity();
        // setear valores necesarios para cumplir las validaciones
        tool.setName(name);
        tool.setCategory("General");           // NotEmpty
        tool.setReplacementValue(1);           // @Min(1)
        tool.setAvailableStock(1);             // valor razonable
        tool.setStateInitial("Bueno");         // campo presente en la tabla
        return em.persistAndFlush(tool);
    }

    private KardexEntity persistMovement(ToolEntity tool, String movementType, LocalDateTime date) {
        KardexEntity k = new KardexEntity();
        // no fijar id manualmente: dejar que lo genere la BD
        k.setTool(tool);
        k.setMovementType(movementType);
        k.setMovementDate(date);
        k.setQuantityAffected(1);
        k.setUserResponsible("tester");
        return em.persistAndFlush(k);
    }

    @Test
    void findByToolNameIgnoreCase_returnsMovementsForToolNameIgnoringCase() {
        ToolEntity t1 = persistTool("TaladroA");
        ToolEntity t2 = persistTool("taladroa"); // mismo nombre distinto case
        persistMovement(t1, "Ingreso", LocalDateTime.of(2025,1,1,10,0));
        persistMovement(t2, "Préstamo", LocalDateTime.of(2025,1,2,11,0));

        List<KardexEntity> results = kardexRepository.findByToolNameIgnoreCase("TALADROA");

        assertThat(results).hasSize(2);
        assertThat(results).extracting("movementType").containsExactlyInAnyOrder("Ingreso", "Préstamo");
    }

    @Test
    void findByMovementDateGreaterThanEqualAndMovementDateLessThan_returnsMovementsInRange() {
        ToolEntity t = persistTool("HerramientaX");
        persistMovement(t, "Ingreso", LocalDateTime.of(2025,1,5,9,0));
        persistMovement(t, "Devolución", LocalDateTime.of(2025,2,1,9,0));

        LocalDateTime start = LocalDateTime.of(2025,1,1,0,0);
        LocalDateTime end = LocalDateTime.of(2025,1,31,23,59);

        List<KardexEntity> results = kardexRepository.findByMovementDateGreaterThanEqualAndMovementDateLessThan(start, end.plusSeconds(1)); // end exclusive in repo

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMovementType()).isEqualTo("Ingreso");
    }

    @Test
    void findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan_combinesFilters() {
        ToolEntity tMatch = persistTool("TaladroCombo");
        ToolEntity tOther = persistTool("OtraTool");

        persistMovement(tMatch, "Baja", LocalDateTime.of(2025,3,10,14,0));
        persistMovement(tOther, "Baja", LocalDateTime.of(2025,3,10,14,0));
        persistMovement(tMatch, "Préstamo", LocalDateTime.of(2025,4,1,10,0)); // fuera de rango

        LocalDateTime start = LocalDateTime.of(2025,3,1,0,0);
        LocalDateTime end = LocalDateTime.of(2025,3,31,23,59);

        List<KardexEntity> results = kardexRepository.findByToolNameIgnoreCaseAndMovementDateGreaterThanEqualAndMovementDateLessThan(
                "taladrocombo", start, end.plusSeconds(1));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTool().getName().toLowerCase()).isEqualTo("taladrocombo");
        assertThat(results.get(0).getMovementType()).isEqualTo("Baja");
    }
}
