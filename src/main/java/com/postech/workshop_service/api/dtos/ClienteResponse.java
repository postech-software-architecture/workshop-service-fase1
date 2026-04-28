package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com os dados do cliente")
public class ClienteResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID id;

	@Schema(example = "João da Silva")
	private String nome;

	@Schema(example = "***.456.789-**")
	private String documento;

	@Schema(example = "joao@email.com")
	private String email;

	@Schema(example = "(11) 99999-9999")
	private String telefone;

	private EnderecoDTO endereco;

	@Schema(example = "1990-01-01")
	private LocalDate dataNascimentoFundacao;

	@Schema(example = "Cliente VIP")
	private String observacoes;

	@Schema(example = "true")
	private boolean ativo;

	@Schema(example = "2024-04-24T10:00:00")
	private LocalDateTime dataCriacao;

	@Schema(example = "2024-04-24T10:00:00")
	private LocalDateTime dataUltimaAtualizacao;

	@Schema(example = "2024-04-24T10:00:00")
	private LocalDateTime dataRemocao;

}
