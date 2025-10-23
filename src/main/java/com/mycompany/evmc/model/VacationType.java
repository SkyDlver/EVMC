package com.mycompany.evmc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vacation_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String key; // annual, sick, unpaid

    @Column(nullable = false)
    private String displayName;

    private boolean accrual;
}
