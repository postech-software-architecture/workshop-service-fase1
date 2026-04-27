package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "enderecos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @OneToOne
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    private ClienteJpaEntity cliente;

    @Column(nullable = false)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(length = 10)
    private String cep;
}
