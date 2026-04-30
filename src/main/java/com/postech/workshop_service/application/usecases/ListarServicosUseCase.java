package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;

/**
 * Caso de uso responsavel pela listagem paginada de servicos do catalogo.
 */
@Service
public class ListarServicosUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public ListarServicosUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Lista servicos com filtros opcionais e paginacao.
	 * @param pagina pagina desejada (base zero).
	 * @param tamanho quantidade de registros por pagina.
	 * @param nome nome opcional para filtro parcial.
	 * @param categoria categoria opcional para filtro exato.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return resultado paginado de servicos.
	 */
	public PaginaResultado<Servico> executar(int pagina, int tamanho, String nome, CategoriaServico categoria,
			boolean incluirInativos) {
		return servicoRepository.listar(pagina, tamanho, nome, categoria, incluirInativos);
	}

}
