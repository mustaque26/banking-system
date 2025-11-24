package com.example.banking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerNumber;
    private String fullName;
    private String email;
    private String mobile;
}
