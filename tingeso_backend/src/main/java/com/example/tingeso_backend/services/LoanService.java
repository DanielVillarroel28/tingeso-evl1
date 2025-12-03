package com.example.tingeso_backend.services;

import com.example.tingeso_backend.dto.LoanDTO;
import com.example.tingeso_backend.dto.LoanWithFineInfoDTO;
import com.example.tingeso_backend.dto.ReturnRequestDTO;
import com.example.tingeso_backend.entities.ClientEntity;
import com.example.tingeso_backend.entities.FineEntity;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.repositories.ClientRepository;
import com.example.tingeso_backend.repositories.FineRepository;
import com.example.tingeso_backend.repositories.LoanRepository;
import com.example.tingeso_backend.repositories.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ToolRepository toolRepository;
    @Autowired
    private KardexService kardexService;
    @Autowired
    private FineService fineService;
    @Autowired
    private FineRepository fineRepository;
    @Autowired
    private ClientService clientService;

    // Modifica o crea un nuevo método para devolver el DTO
    public List<LoanWithFineInfoDTO> getLoansWithFineInfo() {
        List<LoanEntity> loans = loanRepository.findAll();

        return loans.stream().map(loan -> {
            LoanWithFineInfoDTO dto = new LoanWithFineInfoDTO();
            dto.setId(loan.getId());
            dto.setClientName(loan.getClient().getName()); // Asumiendo que ClientEntity tiene un getName()
            dto.setToolName(loan.getTool().getName());     // Asumiendo que ToolEntity tiene un getName()
            dto.setLoanDate(loan.getLoanDate());
            dto.setDueDate(loan.getDueDate());
            dto.setReturnDate(loan.getReturnDate());
            dto.setStatus(loan.getStatus());

            // Buscar la multa asociada a este préstamo
            FineEntity fine = fineRepository.findByLoanId(loan.getId());
            if (fine != null) {
                dto.setFineId(fine.getId());
                dto.setFineAmount(fine.getAmount());
                dto.setFineStatus(fine.getStatus());
            }

            return dto;
        }).collect(Collectors.toList());
    }


    //Crea un nuevo préstamo, aplicando todas las validaciones de negocio.

    @Transactional
    public LoanEntity createLoan(LoanDTO loanRequest, JwtAuthenticationToken principal) {
        ClientEntity client;
        if (loanRequest.getClientId() != null) {
            // Flujo para ADMIN: Se proporcionó un ID de cliente
            client = clientRepository.findById(loanRequest.getClientId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + loanRequest.getClientId()));
        } else {
            // Flujo para USER: No se proporcionó ID, usar el del token
            client = clientService.findOrCreateClient(principal);
        }

        // El resto de la lógica no cambia
        ToolEntity tool = toolRepository.findById(loanRequest.getToolId())
                .orElseThrow(() -> new RuntimeException("Herramienta no encontrada con id: " + loanRequest.getToolId()));

        validateLoanRequest(client, tool, loanRequest.getDueDate());

        tool.setStatus("Prestada");
        toolRepository.save(tool);

        LoanEntity newLoan = new LoanEntity();
        newLoan.setClient(client);
        newLoan.setTool(tool);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setDueDate(loanRequest.getDueDate());
        newLoan.setStatus("Activo");

        LoanEntity savedLoan = loanRepository.save(newLoan);
        kardexService.createLoanMovement(savedLoan);

        return savedLoan;
    }

    public List<LoanWithFineInfoDTO> getLoansForUser(String keycloakId) {
        return loanRepository.findByClientKeycloakId(keycloakId).stream()
                .map(this::buildLoanWithFineInfoDTO)
                .collect(Collectors.toList());
    }

// Procesa la devolución de una herramienta, calcula multas y actualiza estados.
    @Transactional
    public LoanWithFineInfoDTO processReturn(Long loanId, ReturnRequestDTO returnRequest) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!"Activo".equals(loan.getStatus())) {
            throw new RuntimeException("Este préstamo no está activo.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus("Devuelto");

        ToolEntity tool = loan.getTool();
        String toolStatus = returnRequest.getStatus();

        String currentUser = "empleado_actual"; // Obtener del contexto de seguridad

        if ("Irreparable".equals(toolStatus)) {
            tool.setStatus("Dada de baja");
            fineService.createFineForDamage(loan, tool.getReplacementValue());
            kardexService.createWriteOffMovement(tool, currentUser); // Movimiento de baja
        } else if ("Dañada".equals(toolStatus)) {
            tool.setStatus("En reparación");
            fineService.createFineForRepairableDamage(loan);
            kardexService.createRepairMovement(tool, currentUser); // Movimiento de reparación
        } else {
            tool.setStatus("Disponible");
        }

        // Registrar siempre el movimiento de devolución
        kardexService.createReturnMovement(loan, currentUser);
        // Se calcula la multa por atraso independientemente del daño
        fineService.createFineForLateReturn(loan);

        toolRepository.save(tool);
        LoanEntity savedLoan = loanRepository.save(loan);
        return buildLoanWithFineInfoDTO(savedLoan);
    }

    // Crea un método helper para construir el DTO
    private LoanWithFineInfoDTO buildLoanWithFineInfoDTO(LoanEntity loan) {
        LoanWithFineInfoDTO dto = new LoanWithFineInfoDTO();
        dto.setId(loan.getId());
        dto.setClientName(loan.getClient().getName());
        dto.setToolName(loan.getTool().getName());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setStatus(loan.getStatus());

        // Busca la multa (que ahora sí existirá dentro de la misma transacción)
        FineEntity fine = fineRepository.findByLoanId(loan.getId());
        if (fine != null) {
            dto.setFineId(fine.getId());
            dto.setFineAmount(fine.getAmount());
            dto.setFineStatus(fine.getStatus());
        }
        return dto;
    }

    //Centraliza todas las validaciones de negocio antes de crear un préstamo.
    private void validateLoanRequest(ClientEntity client, ToolEntity tool, LocalDate dueDate) {
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la fecha actual.");
        }
        if (!"Disponible".equals(tool.getStatus())) { // Usando el campo 'status'
            throw new RuntimeException("La herramienta no está disponible o no tiene stock.");
        }
        if (!"Activo".equals(client.getStatus())) { // Asumiendo que ClientEntity tiene un campo 'status'
            throw new RuntimeException("El cliente está restringido y no puede solicitar préstamos.");
        }
        if (!loanRepository.findByClientIdAndDueDateBeforeAndStatus(client.getId(), LocalDate.now(), "Activo").isEmpty()) {
            throw new RuntimeException("El cliente tiene préstamos vencidos pendientes de devolución.");
        }
        if (fineService.hasPendingFines(client.getId())) {
            throw new RuntimeException("El cliente tiene multas impagas y no puede solicitar nuevos préstamos.");
        }
        if (loanRepository.countByClientIdAndStatus(client.getId(), "Activo") >= 5) {
            throw new RuntimeException("El cliente ha alcanzado el límite máximo de 5 préstamos activos.");
        }
        if (loanRepository.existsByClientIdAndToolIdAndStatus(client.getId(), tool.getId(), "Activo")) {
            throw new RuntimeException("El cliente ya tiene en préstamo una unidad de esta misma herramienta.");
        }
    }

    //Actualiza un préstamo existente.
    public LoanEntity updateLoan(LoanEntity loan) {
        return loanRepository.save(loan);
    }

    // Elimina un préstamo por su ID.
    @Transactional
    public boolean deleteLoan(Long id) throws Exception {
        // Buscar el préstamo en la base de datos
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el préstamo con ID: " + id));

        // Validar que la fecha de devolución real exista
        if (loan.getReturnDate() == null) {
            // Si es nula, lanzar una excepción con un mensaje claro
            throw new Exception("No se puede eliminar un préstamo que aún no ha sido devuelto.");
        }

        // Si la validación pasa, proceder con la eliminación
        try {
            loanRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            // Capturar otros posibles errores de la base de datos
            throw new Exception("Error al eliminar el préstamo: " + e.getMessage());
        }
    }


    public ArrayList<LoanEntity> getLoans() {
        return (ArrayList<LoanEntity>) loanRepository.findAll();
    }

}