package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payload para criacao de uma ordem de servico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para abertura de uma Ordem de Servico na recepcao do veiculo")
public class CriarOrdemServicoRequest {

	@NotBlank(message = "O documento do cliente e obrigatorio")
	@Schema(description = "CPF ou CNPJ do cliente (com ou sem mascara)", example = "123.456.789-09")
	private String clienteDocumento;

	@NotBlank(message = "A placa do veiculo e obrigatoria")
	@Schema(description = "Placa do veiculo (formato Mercosul ou antigo)", example = "ABC1D23")
	private String veiculoPlaca;

	@Valid
	@Schema(description = "Dados para cadastro do veiculo caso nao exista no sistema")
	private DadosVeiculoRequest veiculo;

	@Valid
	@Schema(description = "(Deprecated) Servicos do catalogo. A OS agora abre vazia em RECEBIDO; "
			+ "itens devem ser adicionados via POST /ordens-servico/{id}/itens apos o diagnostico.")
	private List<ItemServicoRequest> servicos;

	@Valid
	@Schema(description = "(Deprecated) Pecas e insumos. A OS agora abre vazia em RECEBIDO; "
			+ "itens devem ser adicionados via POST /ordens-servico/{id}/itens apos o diagnostico.")
	private List<ItemPecaRequest> pecas;

	@Schema(description = "Observacoes do atendente sobre o veiculo ou relato do cliente",
			example = "Cliente relatou barulho ao frear")
	private String observacoes;

	/**
	 * Dados do veiculo para cadastro quando a placa nao existir no sistema.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Dados basicos do veiculo para cadastro")
	public static class DadosVeiculoRequest {

		@NotBlank(message = "A marca do veiculo e obrigatoria")
		@Schema(example = "Toyota")
		private String marca;

		@NotBlank(message = "O modelo do veiculo e obrigatorio")
		@Schema(example = "Corolla")
		private String modelo;

		@NotNull(message = "O ano do veiculo e obrigatorio")
		@Positive(message = "O ano deve ser um valor positivo")
		@Schema(example = "2020")
		private Integer ano;

	}

	/**
	 * Item de servico do catalogo a ser incluido na OS.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Servico do catalogo")
	public static class ItemServicoRequest {

		@NotNull(message = "O identificador do servico e obrigatorio")
		@Schema(description = "UUID do servico no catalogo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID servicoId;

		@Positive(message = "A quantidade deve ser maior que zero")
		@Schema(example = "1")
		private int quantidade;

	}

	/**
	 * Item de peca ou insumo do catalogo a ser incluido na OS.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Peca ou insumo do catalogo")
	public static class ItemPecaRequest {

		@NotNull(message = "O identificador da peca e obrigatorio")
		@Schema(description = "UUID da peca no catalogo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID pecaId;

		@NotNull(message = "A quantidade e obrigatoria")
		@Positive(message = "A quantidade deve ser maior que zero")
		@Schema(example = "2.0")
		private BigDecimal quantidade;

	}

}
