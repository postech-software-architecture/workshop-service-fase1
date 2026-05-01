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
@Schema(description = "Dados para cadastro de um novo serviço no catálogo")
public class CadastroServicoRequest {

	@NotBlank(message = "O nome é obrigatório")
	@Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
	@Schema(example = "Troca de óleo")
	private String nome;

	@NotBlank(message = "A descrição é obrigatória")
	@Schema(example = "Substituição do óleo do motor e filtro de óleo")
	private String descricao;

	@NotNull(message = "O valor é obrigatório")
	@Positive(message = "O valor deve ser maior que zero")
	@Schema(example = "150.00")
	private BigDecimal valor;

	@NotNull(message = "O tempo estimado é obrigatório")
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

	@Schema(example = "Utilizar óleo sintético 5W30 conforme manual do fabricante")
	private String observacoesTecnicas;

}
