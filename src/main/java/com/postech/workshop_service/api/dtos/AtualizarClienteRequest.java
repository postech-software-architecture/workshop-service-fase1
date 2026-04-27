package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de um cliente existente")
public class AtualizarClienteRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Schema(example = "João da Silva Sauro")
    private String nome;

    @Email(message = "E-mail inválido")
    @Schema(example = "joao.sauro@email.com")
    private String email;

    @Schema(example = "(11) 98888-8888")
    private String telefone;

    private EnderecoDTO endereco;

    @Schema(example = "1990-01-01")
    private LocalDate dataNascimentoFundacao;

    @Schema(example = "Cliente VIP")
    private String observacoes;
}
