// language: java
package com.example.tingeso_backend.repositories;

import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private LoanRepository loanRepository;

    private ClientEntity createClient(String keycloakId) {
        ClientEntity c = new ClientEntity();
        c.setName("Test User");
        c.setEmail("test@example.com");
        c.setKeycloakId(keycloakId);
        c.setPhone("12345678");
        c.setRut("11111111-1");
        c.setStateClient("Activo");
        c.setStatus("Activo");
        return em.persistAndFlush(c);
    }

    private ToolEntity createTool(String name) {
        ToolEntity t = new ToolEntity();
        t.setName(name);
        t.setCategory("Categoria");
        t.setReplacementValue(1); // cumple @Min(1)
        t.setAvailableStock(1);
        t.setStateInitial("Bueno");
        t.setStatus("Disponible");
        return em.persistAndFlush(t);
    }

    @Test
    public void countByClientIdAndExistsByClientIdToolIdAndStatus_workAsExpected() {
        ClientEntity client = createClient("kc-1");
        ToolEntity tool1 = createTool("Martillo");
        ToolEntity tool2 = createTool("Destornillador");

        // dos loans activos para same client (uno con tool1 y otro con tool2)
        LoanEntity loan1 = new LoanEntity();
        loan1.setClient(client);
        loan1.setTool(tool1);
        loan1.setLoanDate(LocalDate.now());
        loan1.setDueDate(LocalDate.now().plusDays(7));
        loan1.setStatus("Activo");
        em.persistAndFlush(loan1);

        LoanEntity loan2 = new LoanEntity();
        loan2.setClient(client);
        loan2.setTool(tool2);
        loan2.setLoanDate(LocalDate.now());
        loan2.setDueDate(LocalDate.now().plusDays(10));
        loan2.setStatus("Activo");
        em.persistAndFlush(loan2);

        int count = loanRepository.countByClientIdAndStatus(client.getId(), "Activo");
        assertEquals(2, count, "Debe contar 2 loans activos para el cliente");

        boolean existsTool1 = loanRepository.existsByClientIdAndToolIdAndStatus(client.getId(), tool1.getId(), "Activo");
        assertTrue(existsTool1, "Debe existir loan activo para tool1");

        boolean existsNon = loanRepository.existsByClientIdAndToolIdAndStatus(client.getId(), 9999L, "Activo");
        assertFalse(existsNon, "No debe existir loan para tool id inexistente");
    }

    @Test
    public void findByClientIdAndDueDateBeforeAndStatus_returnsOverdueLoans() {
        ClientEntity client = createClient("kc-2");
        ToolEntity tool = createTool("Llave");

        LoanEntity pastLoan = new LoanEntity();
        pastLoan.setClient(client);
        pastLoan.setTool(tool);
        pastLoan.setLoanDate(LocalDate.now().minusDays(10));
        pastLoan.setDueDate(LocalDate.now().minusDays(1));
        pastLoan.setStatus("Pendiente");
        em.persistAndFlush(pastLoan);

        LoanEntity futureLoan = new LoanEntity();
        futureLoan.setClient(client);
        futureLoan.setTool(tool);
        futureLoan.setLoanDate(LocalDate.now());
        futureLoan.setDueDate(LocalDate.now().plusDays(5));
        futureLoan.setStatus("Pendiente");
        em.persistAndFlush(futureLoan);

        List<LoanEntity> overdue = loanRepository.findByClientIdAndDueDateBeforeAndStatus(client.getId(), LocalDate.now(), "Pendiente");
        assertNotNull(overdue);
        assertEquals(1, overdue.size(), "Debe retornar solo el loan con dueDate antes de hoy");
        assertEquals(pastLoan.getId(), overdue.get(0).getId());
    }

    @Test
    public void findByClientKeycloakId_returnsLoansForKeycloakUser() {
        ClientEntity client = createClient("kc-user-123");
        ToolEntity tool = createTool("Sierra");

        LoanEntity loan = new LoanEntity();
        loan.setClient(client);
        loan.setTool(tool);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(3));
        loan.setStatus("Activo");
        em.persistAndFlush(loan);

        List<LoanEntity> result = loanRepository.findByClientKeycloakId("kc-user-123");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size(), "Debe retornar un loan asociado al keycloak id");
        assertEquals(loan.getId(), result.get(0).getId());
    }
}
