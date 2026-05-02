package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacao de uma ordem de servico retornada pela API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da Ordem de Servico criada")
public class OrdemServicoResponse {

	@Schema(description = "Identificador unico da OS")
	private UUID id;

	@Schema(description = "Numero sequencial da OS", example = "OS-2026-00001")
	private String numero;

	@Schema(description = "Status atual da OS", example = "AGUARDANDO_RESPOSTA_CLIENTE")
	private String status;

	@Schema(description = "Cliente identificado na recepcao")
	private ClienteResumoResponse cliente;

	@Schema(description = "Veiculo recebido na oficina")
	private VeiculoResumoResponse veiculo;

	@Schema(description = "Orcamento gerado automaticamente")
	private OrcamentoResumoResponse orcamento;

	@Schema(description = "Observacoes registradas pelo atendente")
	private String observacoes;

	@Schema(description = "Data e hora de criacao da OS")
	private LocalDateTime dataCriacao;

	@Schema(description = "Data e hora da ultima atualizacao")
	private LocalDateTime dataUltimaAtualizacao;

	/**
	 * Resumo do cliente vinculado a OS.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Dados resumidos do cliente")
	public static class ClienteResumoResponse {

		private UUID id;

		@Schema(example = "Joao da Silva")
		private String nome;

		@Schema(example = "123.456.789-09")
		private String documentoMascarado;

	}

	/**
	 * Resumo do veiculo vinculado a OS.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Dados resumidos do veiculo")
	public static class VeiculoResumoResponse {

		private UUID id;

		@Schema(example = "ABC1D23")
		private String placa;

		@Schema(example = "Toyota")
		private String marca;

		@Schema(example = "Corolla")
		private String modelo;

		@Schema(example = "2020")
		private int ano;

	}

	/**
	 * Resumo do orcamento gerado.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Orcamento gerado automaticamente na criacao da OS")
	public static class OrcamentoResumoResponse {

		private UUID id;

		@Schema(description = "Valor total calculado a partir do catalogo", example = "310.00")
		private BigDecimal valorTotal;

		@Schema(description = "Status do orcamento", example = "PENDENTE_APROVACAO")
		private String status;

		@Schema(description = "Data de criacao do orcamento")
		private LocalDateTime dataCriacao;

	}

}
