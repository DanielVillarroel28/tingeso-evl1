package com.example.tingeso_backend.controllers;

import com.example.tingeso_backend.entities.KardexEntity;
import com.example.tingeso_backend.services.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kardex")
@CrossOrigin("*")
public class KardexController {

    @Autowired
    private KardexService kardexService;

    @GetMapping("/")
    public ResponseEntity<List<KardexEntity>> getMovements(
            @RequestParam(required = false) String toolName, // <-- CAMBIO AQUÍ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<KardexEntity> movements = kardexService.getMovements(toolName, startDate, endDate);
        return ResponseEntity.ok(movements);
    }
}