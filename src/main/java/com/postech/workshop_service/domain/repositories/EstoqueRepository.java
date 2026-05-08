package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Estoque;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para estoques.
 */
public interface EstoqueRepository {

	/**
	 * Persiste um estoque no repositorio.
	 * @param estoque entidade a ser persistida.
	 * @return entidade persistida.
	 */
	Estoque salvar(Estoque estoque);

	/**
	 * Busca um estoque pelo seu identificador tecnico.
	 * @param id identificador do estoque.
	 * @param incluirInativos indica se estoques inativos devem ser considerados.
	 * @return estoque encontrado, se existir.
	 */
	Optional<Estoque> buscarPorId(UUID id, boolean incluirInativos);

	/**
	 * Lista todos os estoques de uma peca.
	 * @param pecaInsumoId identificador da peca.
	 * @param incluirInativos indica se estoques inativos devem ser considerados.
	 * @return lista de estoques da peca.
	 */
	List<Estoque> listarPorPeca(UUID pecaInsumoId, boolean incluirInativos);

	/**
	 * Lista os estoques de uma peca ordenados do maior saldo disponivel para o menor.
	 * @param pecaInsumoId identificador da peca.
	 * @param incluirInativos indica se estoques inativos devem ser considerados.
	 * @return lista de estoques da peca ordenada por saldo disponivel decrescente.
	 */
	List<Estoque> listarPorPecaOrdenadoPorQuantidadeDisponivel(UUID pecaInsumoId, boolean incluirInativos);

	/**
	 * Busca um estoque especifico de uma peca por localizacao.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao do estoque.
	 * @return estoque encontrado, se existir.
	 */
	Optional<Estoque> buscarPorPecaELocalizacao(UUID pecaInsumoId, String localizacao);

	/**
	 * Calcula a quantidade total de estoque de uma peca.
	 * @param pecaInsumoId identificador da peca.
	 * @return soma das quantidades de todos os estoques ativos.
	 */
	BigDecimal calcularQuantidadeTotal(UUID pecaInsumoId);

	/**
	 * Verifica se existe localizacao para a peca.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao a verificar.
	 * @param estoqueIdIgnorado identificador do estoque a ignorar.
	 * @return verdadeiro se ja existir estoque com a localizacao.
	 */
	boolean existeLocalizacao(UUID pecaInsumoId, String localizacao, UUID estoqueIdIgnorado);

}
