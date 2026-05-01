package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso responsavel por listar pecas com paginacao e filtros.
 */
@Service
public class ListarPecasUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	private final EstoqueRepository estoqueRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 * @param estoqueRepository repositorio de estoques.
	 */
	public ListarPecasUseCase(PecaInsumoRepository pecaInsumoRepository, EstoqueRepository estoqueRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
		this.estoqueRepository = estoqueRepository;
	}

	/**
	 * Lista pecas com paginacao e filtros opcionais.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param nome filtro por nome (parcial).
	 * @param categoria filtro por categoria.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return resultado paginado de pecas.
	 */
	@Transactional(readOnly = true)
	public PaginaResultado<PecaInsumo> executar(int pagina, int tamanho, String nome, String categoria,
			boolean incluirInativos) {
		return pecaInsumoRepository.listar(pagina, tamanho, nome, categoria, incluirInativos);
	}

	/**
	 * Calcula a quantidade total de estoque de uma peca.
	 * @param pecaId identificador da peca.
	 * @return quantidade total.
	 */
	@Transactional(readOnly = true)
	public BigDecimal calcularQuantidadeTotal(java.util.UUID pecaId) {
		return estoqueRepository.calcularQuantidadeTotal(pecaId);
	}

}
