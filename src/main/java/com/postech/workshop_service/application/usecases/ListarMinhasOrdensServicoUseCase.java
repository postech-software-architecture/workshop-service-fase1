package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.FiltrosOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso responsavel por listar as ordens de servico do cliente autenticado.
 */
@Service
public class ListarMinhasOrdensServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 * @param buscarUsuarioAutenticadoUseCase caso de uso para extrair o cliente
	 * autenticado.
	 */
	public ListarMinhasOrdensServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.buscarUsuarioAutenticadoUseCase = buscarUsuarioAutenticadoUseCase;
	}

	/**
	 * Lista as ordens de servico do cliente autenticado, com filtro opcional de status.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param status status opcional para filtrar.
	 * @return resultado paginado das ordens do cliente autenticado.
	 */
	public PaginaResultado<OrdemServico> executar(int pagina, int tamanho, StatusOrdemServico status) {
		UUID clienteId = buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio();
		FiltrosOrdemServico filtros = FiltrosOrdemServico.listagem(status, clienteId, null, null);
		return ordemServicoRepository.listar(pagina, tamanho, filtros);
	}

}
