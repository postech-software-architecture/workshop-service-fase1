package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.application.usecases.ResultadoCriacaoOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
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

	@Schema(description = "Identificador unico da OS", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID id;

	@Schema(description = "Numero sequencial da OS", example = "OS-2026-00001")
	private String numero;

	@Schema(description = "Status atual da OS", example = "AGUARDANDO_APROVACAO")
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

	@Schema(description = "Data e hora de inicio da execucao tecnica")
	private LocalDateTime dataInicioExecucao;

	@Schema(description = "Data e hora de finalizacao da execucao tecnica")
	private LocalDateTime dataFinalizacao;

	@Schema(description = "Data e hora de entrega do veiculo ao cliente")
	private LocalDateTime dataEntrega;

	/**
	 * Resumo do cliente vinculado a OS.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Dados resumidos do cliente")
	public static class ClienteResumoResponse {

		@Schema(description = "Identificador unico do cliente", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
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

		@Schema(description = "Identificador unico do veiculo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
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
	 * Constroi a resposta a partir do resultado do caso de uso.
	 * @param resultado resultado retornado por {@code CriarOrdemServicoUseCase}.
	 * @return resposta mapeada.
	 */
	public static OrdemServicoResponse from(ResultadoCriacaoOrdemServico resultado) {
		return OrdemServicoResponse.builder()
			.id(resultado.ordemServico().getId())
			.numero(resultado.ordemServico().getNumero())
			.status(resultado.ordemServico().getStatus().name())
			.cliente(ClienteResumoResponse.builder()
				.id(resultado.cliente().getId())
				.nome(resultado.cliente().getNome())
				.documentoMascarado(resultado.cliente().getDocumento().mascarado())
				.build())
			.veiculo(VeiculoResumoResponse.builder()
				.id(resultado.veiculo().getId())
				.placa(resultado.veiculo().getPlaca().getValor())
				.marca(resultado.veiculo().getMarca())
				.modelo(resultado.veiculo().getModelo())
				.ano(resultado.veiculo().getAno())
				.build())
			.orcamento(resultado.orcamento() == null ? null
					: OrcamentoResumoResponse.builder()
						.id(resultado.orcamento().getId())
						.valorTotal(resultado.orcamento().getValor())
						.status(resultado.orcamento().getStatus().name())
						.dataCriacao(resultado.orcamento().getDataCriacao())
						.build())
			.observacoes(resultado.ordemServico().getObservacoes())
			.dataCriacao(resultado.ordemServico().getDataCriacao())
			.dataUltimaAtualizacao(resultado.ordemServico().getDataUltimaAtualizacao())
			.dataInicioExecucao(resultado.ordemServico().getDataInicioExecucao())
			.dataFinalizacao(resultado.ordemServico().getDataFinalizacao())
			.dataEntrega(resultado.ordemServico().getDataEntrega())
			.build();
	}

	/**
	 * Constroi a resposta resumida a partir do agregado de ordem de servico.
	 * @param ordemServico ordem de servico atualizada.
	 * @return resposta mapeada.
	 */
	public static OrdemServicoResponse from(OrdemServico ordemServico) {
		return OrdemServicoResponse.builder()
			.id(ordemServico.getId())
			.numero(ordemServico.getNumero())
			.status(ordemServico.getStatus().name())
			.observacoes(ordemServico.getObservacoes())
			.dataCriacao(ordemServico.getDataCriacao())
			.dataUltimaAtualizacao(ordemServico.getDataUltimaAtualizacao())
			.dataInicioExecucao(ordemServico.getDataInicioExecucao())
			.dataFinalizacao(ordemServico.getDataFinalizacao())
			.dataEntrega(ordemServico.getDataEntrega())
			.orcamento(ordemServico.getOrcamentoAtual() == null ? null
					: OrcamentoResumoResponse.builder()
						.id(ordemServico.getOrcamentoAtual().getId())
						.valorTotal(ordemServico.getOrcamentoAtual().getValor())
						.status(ordemServico.getOrcamentoAtual().getStatus().name())
						.dataCriacao(ordemServico.getOrcamentoAtual().getDataCriacao())
						.build())
			.build();
	}

	/**
	 * Resumo do orcamento gerado.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Orcamento atual da OS, quando a composicao tecnica ja foi encerrada")
	public static class OrcamentoResumoResponse {

		@Schema(description = "Identificador unico do orcamento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID id;

		@Schema(description = "Valor total calculado a partir do catalogo", example = "310.00")
		private BigDecimal valorTotal;

		@Schema(description = "Status do orcamento", example = "PENDENTE_APROVACAO")
		private String status;

		@Schema(description = "Data de criacao do orcamento")
		private LocalDateTime dataCriacao;

	}

}
