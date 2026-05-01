package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsavel por buscar um estoque pelo seu identificador.
 */
@Service
public class BuscarEstoquePorIdUseCase {

	private final EstoqueRepository estoqueRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param estoqueRepository repositorio de estoques.
	 */
	public BuscarEstoquePorIdUseCase(EstoqueRepository estoqueRepository) {
		this.estoqueRepository = estoqueRepository;
	}

	/**
	 * Busca um estoque pelo seu identificador.
	 * @param id identificador do estoque.
	 * @param incluirInativos indica se estoques inativos devem ser considerados.
	 * @return estoque encontrado, se existir.
	 */
	@Transactional(readOnly = true)
	public Optional<Estoque> executar(UUID id, boolean incluirInativos) {
		return estoqueRepository.buscarPorId(id, incluirInativos);
	}

}
