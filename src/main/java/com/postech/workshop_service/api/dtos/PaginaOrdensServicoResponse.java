package com.postech.workshop_service.api.dtos;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta paginada contendo ordens de servico em formato resumido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada contendo ordens de servico")
public class PaginaOrdensServicoResponse {

	private List<OrdemServicoResumoResponse> conteudo;

	private int pagina;

	private int tamanho;

	private long totalElementos;

	private int totalPaginas;

	/**
	 * Constroi a resposta a partir de um {@link PaginaResultado} de dominio.
	 * @param resultado pagina retornada pelo repositorio.
	 * @return resposta paginada com itens resumidos.
	 */
	public static PaginaOrdensServicoResponse from(PaginaResultado<OrdemServico> resultado) {
		return PaginaOrdensServicoResponse.builder()
			.conteudo(resultado.itens().stream().map(OrdemServicoResumoResponse::from).toList())
			.pagina(resultado.pagina())
			.tamanho(resultado.tamanho())
			.totalElementos(resultado.totalElementos())
			.totalPaginas(resultado.totalPaginas())
			.build();
	}

}
