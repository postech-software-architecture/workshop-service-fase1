package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por remover logicamente um servico do catalogo.
 */
@Service
public class RemoverServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public RemoverServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Remove logicamente um servico do catalogo, preservando seu historico.
	 * @param id identificador do servico a ser removido.
	 */
	@Transactional
	public void executar(UUID id) {
		var servico = servicoRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID informado."));
		servico.removerLogicamente();
		servicoRepository.salvar(servico);
	}

}
