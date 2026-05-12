package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.OrdemServico;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resumo de uma ordem de servico para listagens paginadas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de uma ordem de servico para listagens")
public class OrdemServicoResumoResponse {

	@Schema(description = "Identificador unico da OS", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID id;

	@Schema(description = "Numero sequencial da OS", example = "OS-2026-00001")
	private String numero;

	@Schema(description = "Status atual da OS", example = "AGUARDANDO_APROVACAO")
	private String status;

	@Schema(description = "Identificador do cliente vinculado")
	private UUID idCliente;

	@Schema(description = "Identificador do veiculo vinculado")
	private UUID idVeiculo;

	@Schema(description = "Data e hora de criacao da OS")
	private LocalDateTime dataCriacao;

	@Schema(description = "Data e hora da ultima atualizacao")
	private LocalDateTime dataUltimaAtualizacao;

	/**
	 * Constroi a resposta a partir do agregado de dominio.
	 * @param ordem ordem de servico de origem.
	 * @return DTO resumido.
	 */
	public static OrdemServicoResumoResponse from(OrdemServico ordem) {
		return OrdemServicoResumoResponse.builder()
			.id(ordem.getId())
			.numero(ordem.getNumero())
			.status(ordem.getStatus().name())
			.idCliente(ordem.getIdCliente())
			.idVeiculo(ordem.getIdVeiculo())
			.dataCriacao(ordem.getDataCriacao())
			.dataUltimaAtualizacao(ordem.getDataUltimaAtualizacao())
			.build();
	}

}
