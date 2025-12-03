package com.example.tingeso_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private ToolEntity tool;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<FineEntity> fines;
}
