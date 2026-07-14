package com.postech.workshop_service.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Payload para abertura de uma ordem de servico com itens iniciais. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para abertura de uma Ordem de Servico com servicos e pecas iniciais")
public class CriarOrdemServicoComItensRequest {

	@NotBlank(message = "O documento do cliente e obrigatorio")
	private String clienteDocumento;

	@NotBlank(message = "A placa do veiculo e obrigatoria")
	private String veiculoPlaca;

	@Valid
	private CriarOrdemServicoRequest.DadosVeiculoRequest veiculo;

	@Valid
	private List<ItemServicoRequest> servicos;

	@Valid
	private List<ItemPecaRequest> pecas;

	private String observacoes;

	@JsonIgnore
	@AssertTrue(message = "Informe pelo menos um servico ou uma peca")
	public boolean isPossuiAoMenosUmItem() {
		return servicos != null && !servicos.isEmpty() || pecas != null && !pecas.isEmpty();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Servico inicial da ordem de servico")
	public static class ItemServicoRequest {

		@NotNull(message = "O identificador do servico e obrigatorio")
		private UUID servicoId;

		@Positive(message = "A quantidade deve ser maior que zero")
		private int quantidade;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Peca ou insumo inicial da ordem de servico")
	public static class ItemPecaRequest {

		@NotNull(message = "O identificador da peca e obrigatorio")
		private UUID pecaId;

		@NotNull(message = "A quantidade e obrigatoria")
		@Positive(message = "A quantidade deve ser maior que zero")
		private BigDecimal quantidade;

	}

}
