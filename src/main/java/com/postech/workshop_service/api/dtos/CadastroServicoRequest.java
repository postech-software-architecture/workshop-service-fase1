package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload de cadastro de servico no catalogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para cadastro de um novo servico no catalogo")
public class CadastroServicoRequest {

	@NotBlank(message = "O nome e obrigatorio")
	@Size(max = 100, message = "O nome deve possuir no maximo 100 caracteres")
	@Schema(example = "Troca de oleo")
	private String nome;

	@NotBlank(message = "A descricao e obrigatoria")
	@Schema(example = "Substituicao do oleo do motor e filtro de oleo")
	private String descricao;

	@NotNull(message = "O valor e obrigatorio")
	@Positive(message = "O valor deve ser maior que zero")
	@Schema(example = "150.00")
	private BigDecimal valor;

	@NotNull(message = "O tempo estimado e obrigatorio")
	@Positive(message = "O tempo estimado deve ser maior que zero")
	@Schema(example = "60")
	private Integer tempoEstimadoMinutos;

	@Schema(example = "PREVENTIVA")
	private CategoriaServico categoria;

	@Schema(example = "BAIXA")
	private NivelComplexidade nivelComplexidade;

	@Positive(message = "A garantia em dias deve ser maior que zero")
	@Schema(example = "30")
	private Integer garantiaDias;

	@Schema(example = "Utilizar oleo sintetico 5W30 conforme manual do fabricante")
	private String observacoesTecnicas;

}
