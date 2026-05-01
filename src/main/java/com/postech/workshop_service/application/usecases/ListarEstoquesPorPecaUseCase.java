package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por listar os estoques de uma peca.
 */
@Service
public class ListarEstoquesPorPecaUseCase {

	private final EstoqueRepository estoqueRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param estoqueRepository repositorio de estoques.
	 */
	public ListarEstoquesPorPecaUseCase(EstoqueRepository estoqueRepository) {
		this.estoqueRepository = estoqueRepository;
	}

	/**
	 * Lista todos os estoques vinculados a uma peca.
	 * @param pecaInsumoId identificador da peca.
	 * @param incluirInativos indica se estoques inativos devem ser considerados.
	 * @return lista de estoques encontrados.
	 */
	@Transactional(readOnly = true)
	public List<Estoque> executar(UUID pecaInsumoId, boolean incluirInativos) {
		return estoqueRepository.listarPorPeca(pecaInsumoId, incluirInativos);
	}

}
