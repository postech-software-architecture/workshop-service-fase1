package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso responsavel por permitir que um cliente consulte o status atual de uma
 * ordem de servico, garantindo que ele so acesse OS proprias.
 */
@Service
public class ConsultarStatusOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 * @param buscarUsuarioAutenticadoUseCase caso de uso para extrair o cliente
	 * autenticado.
	 */
	public ConsultarStatusOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.buscarUsuarioAutenticadoUseCase = buscarUsuarioAutenticadoUseCase;
	}

	/**
	 * Consulta o status atual de uma ordem de servico, validando que pertence ao cliente
	 * autenticado.
	 * @param idOrdemServico identificador da ordem.
	 * @return ordem encontrada (controller monta o DTO especifico).
	 * @throws RecursoNaoEncontradoException se a ordem nao existir.
	 * @throws AcessoNegadoException se a ordem pertencer a outro cliente.
	 */
	public OrdemServico executar(UUID idOrdemServico) {
		UUID clienteAutenticadoId = buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio();
		OrdemServico ordem = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		if (!ordem.getIdCliente().equals(clienteAutenticadoId)) {
			throw new AcessoNegadoException("A ordem de servico solicitada nao pertence ao cliente autenticado.");
		}
		return ordem;
	}

}
