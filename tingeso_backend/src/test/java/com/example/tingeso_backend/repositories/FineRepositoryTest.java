package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FineRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FineRepository fineRepository;

    private ToolEntity createAndPersistTool(String name) {
        ToolEntity tool = new ToolEntity();
        // set name if setter exists
        try { tool.getClass().getMethod("setName", String.class).invoke(tool, name); } catch (Exception ignored) {}

        // set mandatory category (String)
        try { tool.getClass().getMethod("setCategory", String.class).invoke(tool, "General"); } catch (Exception ignored) {}

        // set mandatory replacementValue (try multiple primitive/boxed types)
        boolean setReplacement = false;
        try { tool.getClass().getMethod("setReplacementValue", int.class).invoke(tool, 1); setReplacement = true; } catch (Exception ignored) {}
        if (!setReplacement) try { tool.getClass().getMethod("setReplacementValue", Integer.class).invoke(tool, 1); setReplacement = true; } catch (Exception ignored) {}
        if (!setReplacement) try { tool.getClass().getMethod("setReplacementValue", long.class).invoke(tool, 1L); setReplacement = true; } catch (Exception ignored) {}
        if (!setReplacement) try { tool.getClass().getMethod("setReplacementValue", Long.class).invoke(tool, 1L); setReplacement = true; } catch (Exception ignored) {}
        if (!setReplacement) try { tool.getClass().getMethod("setReplacementValue", double.class).invoke(tool, 1.0); setReplacement = true; } catch (Exception ignored) {}
        if (!setReplacement) try { tool.getClass().getMethod("setReplacementValue", Double.class).invoke(tool, 1.0); setReplacement = true; } catch (Exception ignored) {}

        // persist
        em.persist(tool);
        return tool;
    }

    @Test
    @Rollback
    void findByLoanId_returnsFine() {
        ClientEntity client = new ClientEntity();
        client.setKeycloakId("kc-1");
        client.setName("Cliente A");
        client.setEmail("clienteA@example.com");
        em.persist(client);

        ToolEntity tool = createAndPersistTool("Taladro A");

        LoanEntity loan = new LoanEntity();
        loan.setClient(client);
        loan.setTool(tool);
        em.persist(loan);
        em.flush();

        FineEntity fine = new FineEntity();
        fine.setLoan(loan);
        try { fine.getClass().getMethod("setLoanId", Long.class).invoke(fine, loan.getId()); } catch (Exception ignored) {}
        fine.setStatus("Pendiente");
        fine.setAmount(100);
        em.persist(fine);
        em.flush();

        FineEntity found = fineRepository.findByLoanId(loan.getId());
        assertThat(found).isNotNull();
        assertThat(found.getLoan()).isNotNull();
        assertThat(found.getLoan().getId()).isEqualTo(loan.getId());
    }

    @Test
    @Rollback
    void findPendingFinesByClientId_returnsOnlyPending() {
        ClientEntity client = new ClientEntity();
        client.setKeycloakId("kc-2");
        client.setName("Cliente B");
        client.setEmail("clienteB@example.com");
        em.persist(client);

        ToolEntity tool1 = createAndPersistTool("Multiherramienta 1");
        ToolEntity tool2 = createAndPersistTool("Multiherramienta 2");

        LoanEntity loan1 = new LoanEntity();
        loan1.setClient(client);
        loan1.setTool(tool1);
        em.persist(loan1);

        LoanEntity loan2 = new LoanEntity();
        loan2.setClient(client);
        loan2.setTool(tool2);
        em.persist(loan2);
        em.flush();

        FineEntity pending = new FineEntity();
        pending.setLoan(loan1);
        try { pending.getClass().getMethod("setLoanId", Long.class).invoke(pending, loan1.getId()); } catch (Exception ignored) {}
        pending.setStatus("Pendiente");
        pending.setAmount(50);
        em.persist(pending);

        FineEntity paid = new FineEntity();
        paid.setLoan(loan2);
        try { paid.getClass().getMethod("setLoanId", Long.class).invoke(paid, loan2.getId()); } catch (Exception ignored) {}
        paid.setStatus("Pagado");
        paid.setAmount(25);
        em.persist(paid);
        em.flush();

        List<FineEntity> pendingList = fineRepository.findPendingFinesByClientId(client.getId());
        assertThat(pendingList).isNotEmpty();
        assertThat(pendingList).allMatch(f -> "Pendiente".equals(f.getStatus()));
        assertThat(pendingList).extracting(f -> f.getLoan().getClient().getId()).containsOnly(client.getId());
    }

    @Test
    @Rollback
    void findByUserKeycloakId_returnsFinesForKeycloakId() {
        ClientEntity client = new ClientEntity();
        client.setKeycloakId("kc-user");
        client.setName("Cliente C");
        client.setEmail("clienteC@example.com");
        em.persist(client);

        ToolEntity tool = createAndPersistTool("Herramienta Usuario");

        LoanEntity loan = new LoanEntity();
        loan.setClient(client);
        loan.setTool(tool);
        em.persist(loan);
        em.flush();

        FineEntity fine = new FineEntity();
        fine.setLoan(loan);
        try { fine.getClass().getMethod("setLoanId", Long.class).invoke(fine, loan.getId()); } catch (Exception ignored) {}
        fine.setStatus("Pendiente");
        fine.setAmount(10);
        em.persist(fine);
        em.flush();

        List<FineEntity> fines = fineRepository.findByUserKeycloakId("kc-user");
        assertThat(fines).isNotEmpty();
        assertThat(fines).allMatch(f -> "kc-user".equals(f.getLoan().getClient().getKeycloakId()));
    }
}