package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta paginada contendo servicos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada contendo servicos")
public class PaginaServicosResponse {

	private List<ServicoResponse> conteudo;

	private int pagina;

	private int tamanho;

	private long totalElementos;

	private int totalPaginas;

}
