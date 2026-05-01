package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.PecaInsumo;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para o agregado de pecas e insumos.
 */
public interface PecaInsumoRepository {

	/**
	 * Persiste uma peca no repositorio.
	 * @param pecaInsumo agregado a ser persistido.
	 * @return agregado persistido.
	 */
	PecaInsumo salvar(PecaInsumo pecaInsumo);

	/**
	 * Busca uma peca pelo seu identificador tecnico.
	 * @param id identificador da peca.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return peca encontrada, se existir.
	 */
	Optional<PecaInsumo> buscarPorId(UUID id, boolean incluirInativos);

	/**
	 * Busca uma peca pelo SKU normalizado.
	 * @param skuNormalizado SKU normalizado.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return peca encontrada, se existir.
	 */
	Optional<PecaInsumo> buscarPorSku(String skuNormalizado, boolean incluirInativos);

	/**
	 * Lista pecas com filtros opcionais e paginacao.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param nome filtro por nome (parcial).
	 * @param categoria filtro por categoria.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return resultado paginado de pecas.
	 */
	PaginaResultado<PecaInsumo> listar(int pagina, int tamanho, String nome, String categoria, boolean incluirInativos);

	/**
	 * Verifica se existe SKU ativo para outro cadastro.
	 * @param skuNormalizado SKU normalizado.
	 * @param pecaIdIgnorado identificador da peca a ser ignorada, quando aplicavel.
	 * @return verdadeiro quando ja existir cadastro ativo com o SKU.
	 */
	boolean existeSkuAtivo(String skuNormalizado, UUID pecaIdIgnorado);

}
