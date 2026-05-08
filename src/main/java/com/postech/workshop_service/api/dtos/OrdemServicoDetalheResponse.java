package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.OrdemServico;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detalhe completo de uma ordem de servico para consulta administrativa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhe completo de uma ordem de servico")
public class OrdemServicoDetalheResponse {

	@Schema(description = "Identificador unico da OS", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID id;

	@Schema(description = "Numero sequencial da OS", example = "OS-2026-00001")
	private String numero;

	@Schema(description = "Status atual da OS", example = "AGUARDANDO_RESPOSTA_CLIENTE")
	private String status;

	@Schema(description = "Identificador do cliente vinculado")
	private UUID idCliente;

	@Schema(description = "Identificador do veiculo vinculado")
	private UUID idVeiculo;

	@Schema(description = "Itens da composicao tecnica registrados na recepcao")
	private List<ItemComposicaoResponse> itens;

	@Schema(description = "Observacoes registradas pelo atendente")
	private String observacoes;

	@Schema(description = "Data e hora de criacao da OS")
	private LocalDateTime dataCriacao;

	@Schema(description = "Data e hora da ultima atualizacao")
	private LocalDateTime dataUltimaAtualizacao;

	/**
	 * Constroi a resposta a partir do agregado de dominio.
	 * @param ordem ordem de servico de origem.
	 * @return DTO com os campos mapeados.
	 */
	public static OrdemServicoDetalheResponse from(OrdemServico ordem) {
		return OrdemServicoDetalheResponse.builder()
			.id(ordem.getId())
			.numero(ordem.getNumero())
			.status(ordem.getStatus().name())
			.idCliente(ordem.getIdCliente())
			.idVeiculo(ordem.getIdVeiculo())
			.itens(ordem.getItensComposicao().stream().map(ItemComposicaoResponse::from).toList())
			.observacoes(ordem.getObservacoes())
			.dataCriacao(ordem.getDataCriacao())
			.dataUltimaAtualizacao(ordem.getDataUltimaAtualizacao())
			.build();
	}

	/**
	 * Item da composicao tecnica.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Item da composicao tecnica registrado na ordem")
	public static class ItemComposicaoResponse {

		@Schema(description = "Tipo do item", example = "SERVICO")
		private String tipo;

		@Schema(description = "Descricao do item")
		private String descricao;

		@Schema(description = "Valor congelado no momento do registro", example = "150.00")
		private BigDecimal valor;

		@Schema(description = "Identificador da peca/insumo (apenas itens do tipo PECA)")
		private UUID idPecaInsumo;

		/**
		 * Mapeia um item de composicao tecnica para sua resposta.
		 * @param item item a ser convertido.
		 * @return DTO equivalente.
		 */
		public static ItemComposicaoResponse from(ItemComposicaoTecnica item) {
			return ItemComposicaoResponse.builder()
				.tipo(item.getTipo().name())
				.descricao(item.getDescricao())
				.valor(item.getValor())
				.idPecaInsumo(item.getIdPecaInsumo())
				.build();
		}

	}

}
