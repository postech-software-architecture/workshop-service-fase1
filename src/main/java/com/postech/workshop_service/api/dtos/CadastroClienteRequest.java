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
@Schema(description = "Dados para cadastro de um novo cliente")
public class CadastroClienteRequest {

	@NotBlank(message = "O nome é obrigatório")
	@Schema(example = "João da Silva")
	private String nome;

	@NotBlank(message = "O documento (CPF/CNPJ) é obrigatório")
	@Schema(example = "123.456.789-01")
	private String documento;

	@Email(message = "E-mail inválido")
	@Schema(example = "joao@email.com")
	private String email;

	@Schema(example = "(11) 99999-9999")
	private String telefone;

	private EnderecoDTO endereco;

	@Schema(example = "1990-01-01")
	private LocalDate dataNascimentoFundacao;

	@Schema(example = "Cliente prefere contato via WhatsApp")
	private String observacoes;

}
