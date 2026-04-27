package com.postech.workshop_service.api.dtos;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resposta detalhada do veiculo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados detalhados do veiculo")
public class VeiculoResponse {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private int ano;
    private String cor;
    private String observacoes;
    private List<ClienteVinculadoResponse> clientes;
    private boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime dataRemocao;
}
