package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualizacao de uma peca ou insumo")
public class AtualizarPecaRequest {

	@NotBlank(message = "O nome e obrigatorio")
	@Size(max = 200, message = "O nome deve ter no maximo 200 caracteres")
	@Schema(example = "Filtro de oleo 5W30 Premium", description = "Nome ou descricao da peca")
	private String nome;

	@NotNull(message = "O valor unitario e obrigatorio")
	@Positive(message = "O valor unitario deve ser maior que zero")
	@Schema(example = "49.90", description = "Preco unitario da peca")
	private BigDecimal valorUnitario;

	@NotNull(message = "O estoque minimo e obrigatorio")
	@PositiveOrZero(message = "O estoque minimo nao pode ser negativo")
	@Schema(example = "10", description = "Nivel minimo para alerta de reposicao")
	private BigDecimal estoqueMinimo;

	@NotBlank(message = "A unidade de medida e obrigatoria")
	@Schema(example = "UN", description = "Unidade de medida: UN, L, KG, M, ML, CX, PC")
	private String unidadeMedida;

	@NotBlank(message = "O tipo do item e obrigatorio")
	@Schema(example = "PECA", description = "Tipo principal do item: PECA ou INSUMO")
	private String tipoItem;

	@Size(max = 200, message = "O fornecedor deve ter no maximo 200 caracteres")
	@Schema(example = "Bosch", description = "Nome do fornecedor")
	private String fornecedor;

	@Size(max = 50, message = "O codigo de barras deve ter no maximo 50 caracteres")
	@Schema(example = "7891234567890", description = "Codigo de barras do produto")
	private String codigoBarras;

	@Size(max = 100, message = "A marca deve ter no maximo 100 caracteres")
	@Schema(example = "Bosch", description = "Marca do produto")
	private String marca;

	@Size(max = 100, message = "A categoria deve ter no maximo 100 caracteres")
	@Schema(example = "Filtros", description = "Categoria da peca")
	private String categoria;

	@Size(max = 500, message = "A aplicacao deve ter no maximo 500 caracteres")
	@Schema(example = "Hatch compacto ate 2.0", description = "Veiculos ou servicos onde a peca e aplicada")
	private String aplicacao;

	@Schema(example = "Compativel com Gol, Fox e Polo", description = "Observacoes adicionais")
	private String observacoes;

}
