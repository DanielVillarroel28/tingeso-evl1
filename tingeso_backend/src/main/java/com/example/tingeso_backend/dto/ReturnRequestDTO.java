package com.example.tingeso_backend.dto;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private String status; // "OK", "Dañada", "Irreparable"
}