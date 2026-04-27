package com.postech.workshop_service.api.dtos;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de atualizacao de veiculo sem alteracao do vinculo de clientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualizacao de um veiculo existente")
public class AtualizarVeiculoRequest {

    @NotBlank(message = "A placa e obrigatoria")
    @Size(max = 10, message = "A placa deve possuir no maximo 10 caracteres antes da normalizacao")
    @Schema(example = "BRA1D23")
    private String placa;

    @NotBlank(message = "A marca e obrigatoria")
    @Size(max = 60, message = "A marca deve possuir no maximo 60 caracteres")
    @Schema(example = "Toyota")
    private String marca;

    @NotBlank(message = "O modelo e obrigatorio")
    @Size(max = 80, message = "O modelo deve possuir no maximo 80 caracteres")
    @Schema(example = "Corolla")
    private String modelo;

    @Min(value = 1900, message = "O ano deve ser igual ou superior a 1900")
    @Max(value = 3000, message = "O ano informado e invalido")
    @Schema(example = "2020")
    private int ano;

    @Size(max = 30, message = "A cor deve possuir no maximo 30 caracteres")
    @Schema(example = "Prata")
    private String cor;

    @Size(max = 2000, message = "As observacoes devem possuir no maximo 2000 caracteres")
    @Schema(example = "Veiculo compartilhado entre familiares")
    private String observacoes;
}
