package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.OrdemServico;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resposta contendo o status atual de uma ordem de servico com os itens da composicao
 * tecnica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Status atual de uma ordem de servico com itens da composicao")
public class StatusOrdemServicoResponse {

	@Schema(description = "Identificador unico da OS")
	private UUID id;

	@Schema(description = "Numero sequencial da OS", example = "OS-2026-00001")
	private String numero;

	@Schema(description = "Status atual da OS", example = "AGUARDANDO_APROVACAO")
	private String status;

	@Schema(description = "Data da ultima atualizacao registrada")
	private LocalDateTime dataUltimaAtualizacao;

	@Schema(description = "Itens da composicao tecnica da ordem de servico")
	private List<ItemComposicaoTecnicaResponse> itens;

	/**
	 * Constroi a resposta a partir do agregado de dominio.
	 * @param ordem ordem de servico consultada.
	 * @return DTO compacto com o status e itens.
	 */
	public static StatusOrdemServicoResponse from(OrdemServico ordem) {
		return StatusOrdemServicoResponse.builder()
			.id(ordem.getId())
			.numero(ordem.getNumero())
			.status(ordem.getStatus().name())
			.dataUltimaAtualizacao(ordem.getDataUltimaAtualizacao())
			.itens(ordem.getItensComposicao().stream().map(ItemComposicaoTecnicaResponse::from).toList())
			.build();
	}

}
