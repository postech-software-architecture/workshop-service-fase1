package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacao de uma transicao de status da ordem de servico.
 */
@Builder
@Schema(description = "Registro de historico de status da Ordem de Servico")
public record HistoricoStatusOrdemServicoResponse(@Schema(description = "Identificador unico do historico") UUID id,
		@Schema(description = "Identificador da Ordem de Servico") UUID idOrdemServico,
		@Schema(description = "Status anterior da OS") String statusAnterior,
		@Schema(description = "Novo status da OS") String statusNovo,
		@Schema(description = "Data e hora da transicao") LocalDateTime dataTransicao,
		@Schema(description = "Identificador do usuario responsavel") UUID idUsuario,
		@Schema(description = "Username do usuario responsavel") String usernameUsuario) {

	public static HistoricoStatusOrdemServicoResponse from(HistoricoStatusOrdemServico historico) {
		return HistoricoStatusOrdemServicoResponse.builder()
			.id(historico.getId())
			.idOrdemServico(historico.getIdOrdemServico())
			.statusAnterior(historico.getStatusAnterior().name())
			.statusNovo(historico.getStatusNovo().name())
			.dataTransicao(historico.getDataTransicao())
			.idUsuario(historico.getIdUsuario())
			.usernameUsuario(historico.getUsernameUsuario())
			.build();
	}

}
