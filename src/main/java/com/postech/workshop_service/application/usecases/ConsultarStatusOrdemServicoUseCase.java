package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;

/**
 * Caso de uso responsavel por permitir a consulta publica do status atual de uma ordem de
 * servico pelo numero da OS. Endpoint publico sem autenticacao.
 */
@Service
public class ConsultarStatusOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 */
	public ConsultarStatusOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
	}

	/**
	 * Consulta o status atual de uma ordem de servico pelo numero sequencial.
	 * @param numeroOrdemServico numero da ordem (formato OS-{ANO}-{NNNNN}).
	 * @return ordem encontrada (controller monta o DTO especifico).
	 * @throws RecursoNaoEncontradoException se a ordem nao existir.
	 */
	public OrdemServico executar(String numeroOrdemServico) {
		return ordemServicoRepository.buscarPorNumero(numeroOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
	}

}
