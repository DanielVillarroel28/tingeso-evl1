package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.dto.LoanDTO;
import com.example.tingeso_backend.dto.LoanWithFineInfoDTO;
import com.example.tingeso_backend.dto.ReturnRequestDTO;
import com.example.tingeso_backend.entities.LoanEntity;
import com.example.tingeso_backend.entities.ToolEntity;
import com.example.tingeso_backend.services.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@CrossOrigin("*")
public class LoanController {

    @Autowired
    private LoanService loanService;


    @GetMapping("/")
    public ResponseEntity<List<LoanWithFineInfoDTO>> listLoans() {
        List<LoanWithFineInfoDTO> loans = loanService.getLoansWithFineInfo();
        return ResponseEntity.ok(loans);
    }



    @PutMapping("/")
    public ResponseEntity<LoanEntity> updateLoan(@RequestBody LoanEntity loan){
        LoanEntity loanUpdated = loanService.updateLoan(loan);
        return ResponseEntity.ok(loanUpdated);
    }


    @PostMapping("/{id}/return")
    public ResponseEntity<LoanWithFineInfoDTO> processReturn(
            @PathVariable Long id,
            @RequestBody ReturnRequestDTO returnRequest) { // <-- Recibe el DTO
        LoanWithFineInfoDTO updatedLoan = loanService.processReturn(id, returnRequest);
        return ResponseEntity.ok(updatedLoan);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ToolEntity> deleteLoanById(@PathVariable Long id) throws Exception{
        var isDeleted = loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/")
    public ResponseEntity<?> createLoan(@Valid @RequestBody LoanDTO loanRequest, JwtAuthenticationToken principal) {
        // Pasamos el 'principal' (la sesión del usuario) al servicio
        LoanEntity newLoan = loanService.createLoan(loanRequest, principal);
        return new ResponseEntity<>(newLoan, HttpStatus.CREATED);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanWithFineInfoDTO>> getMyLoans(JwtAuthenticationToken principal) {
        String keycloakId = principal.getName(); // 'sub' del token
        List<LoanWithFineInfoDTO> loans = loanService.getLoansForUser(keycloakId);
        return ResponseEntity.ok(loans);
    }


}