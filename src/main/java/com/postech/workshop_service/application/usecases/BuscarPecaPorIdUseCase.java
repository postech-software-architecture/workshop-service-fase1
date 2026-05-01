package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsavel por buscar uma peca pelo seu identificador.
 */
@Service
public class BuscarPecaPorIdUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public BuscarPecaPorIdUseCase(PecaInsumoRepository pecaInsumoRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Busca uma peca pelo seu identificador.
	 * @param id identificador da peca.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return peca encontrada, se existir.
	 */
	@Transactional(readOnly = true)
	public Optional<PecaInsumo> executar(UUID id, boolean incluirInativos) {
		return pecaInsumoRepository.buscarPorId(id, incluirInativos);
	}

}
