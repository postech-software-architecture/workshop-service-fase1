package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por remover logicamente uma peca.
 */
@Service
public class RemoverPecaUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public RemoverPecaUseCase(PecaInsumoRepository pecaInsumoRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Executa a remocao logica de uma peca.
	 * @param id identificador da peca.
	 */
	@Transactional
	public void executar(UUID id) {
		PecaInsumo peca = pecaInsumoRepository.buscarPorId(id, false)
			.orElseThrow(() -> new RegraDeNegocioException("Peca nao encontrada com o identificador informado."));

		peca.removerLogicamente();
		pecaInsumoRepository.salvar(peca);
	}

}
