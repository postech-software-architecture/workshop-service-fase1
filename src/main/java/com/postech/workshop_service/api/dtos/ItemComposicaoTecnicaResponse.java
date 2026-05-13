package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informacoes de um item da composicao tecnica da ordem de servico")
public class ItemComposicaoTecnicaResponse {

	@Schema(description = "Identificador unico do item")
	private UUID id;

	@Schema(description = "Descricao do servico ou peca")
	private String descricao;

	@Schema(description = "Valor unitario do item", example = "150.00")
	private BigDecimal valor;

	@Schema(description = "Tipo do item", example = "SERVICO")
	private String tipo;

	@Schema(description = "Identificador da peca (se tipo PECA)")
	private UUID idPecaInsumo;

	@Schema(description = "Identificador do servico (se tipo SERVICO)")
	private UUID idServico;

	@Schema(description = "Status de execucao (apenas para itens tipo SERVICO)", example = "PENDENTE")
	private String statusExecucao;

	public static ItemComposicaoTecnicaResponse from(ItemComposicaoTecnica item) {
		return ItemComposicaoTecnicaResponse.builder()
			.id(item.getId())
			.descricao(item.getDescricao())
			.valor(item.getValor())
			.tipo(item.getTipo().name())
			.idPecaInsumo(item.getIdPecaInsumo())
			.idServico(item.getIdServico())
			.statusExecucao(item.getStatusExecucao() != null ? item.getStatusExecucao().name() : null)
			.build();
	}

}
