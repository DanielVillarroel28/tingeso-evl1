package com.example.tingeso_backend.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanDTO {
    private Long clientId;
    @NotNull
    private Long toolId;
    @NotNull
    private LocalDate dueDate;
}