package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta detalhada do servico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados detalhados do serviço")
public class ServicoResponse {

	@Schema(example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID id;

	private String nome;

	private String descricao;

	private BigDecimal valor;

	private int tempoEstimadoMinutos;

	private CategoriaServico categoria;

	private NivelComplexidade nivelComplexidade;

	private Integer garantiaDias;

	private String observacoesTecnicas;

	private boolean ativo;

	private LocalDateTime dataCriacao;

	private LocalDateTime dataUltimaAtualizacao;

	private LocalDateTime dataRemocao;

}
