package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por reativar logicamente um servico previamente removido.
 */
@Service
public class ReativarServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public ReativarServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Reativa logicamente um servico previamente removido, restaurando-o ao catalogo
	 * ativo.
	 * @param id identificador do servico.
	 * @return servico reativado.
	 */
	@Transactional
	public Servico executar(UUID id) {
		Servico servico = servicoRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID informado."));

		if (servicoRepository.existeNomeAtivo(servico.getNome(), id)) {
			throw new RegraDeNegocioException(
					"Já existe um serviço ativo cadastrado com o nome informado; renomeie o serviço antes de reativar.");
		}

		servico.reativar();
		return servicoRepository.salvar(servico);
	}

}
