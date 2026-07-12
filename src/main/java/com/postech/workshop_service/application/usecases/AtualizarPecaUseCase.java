package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Caso de uso responsavel por atualizar os dados de uma peca existente.
 */
@Service
public class AtualizarPecaUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public AtualizarPecaUseCase(PecaInsumoRepository pecaInsumoRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Executa a atualizacao dos dados de uma peca.
	 * @param id identificador da peca.
	 * @param nome novo nome.
	 * @param valorUnitario novo valor unitario.
	 * @param estoqueMinimo novo estoque minimo.
	 * @param unidadeMedida nova unidade de medida.
	 * @param fornecedor novo fornecedor.
	 * @param codigoBarras novo codigo de barras.
	 * @param marca nova marca.
	 * @param categoria nova categoria.
	 * @param aplicacao nova aplicacao.
	 * @param observacoes novas observacoes.
	 * @return peca atualizada.
	 */
	@Transactional
	public PecaInsumo executar(UUID id, String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			String unidadeMedida, String tipoItem, String fornecedor, String codigoBarras, String marca,
			String categoria, String aplicacao, String observacoes) {
		try {
			PecaInsumo peca = pecaInsumoRepository.buscarPorId(id, false)
				.orElseThrow(() -> new RegraDeNegocioException("Peca nao encontrada com o identificador informado."));

			UnidadeMedida unidade = parseUnidadeMedida(unidadeMedida);
			TipoItem tipo = parseTipoItem(tipoItem);

			peca.atualizarDados(nome, valorUnitario, estoqueMinimo, unidade, tipo, fornecedor, codigoBarras, marca,
					categoria, aplicacao, observacoes);

			return pecaInsumoRepository.salvar(peca);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (ObjectOptimisticLockingFailureException ex) {
			throw new RegraDeNegocioException(
					"Esta peca foi modificada por outro usuario. Por favor, tente novamente.");
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private UnidadeMedida parseUnidadeMedida(String unidadeMedida) {
		if (unidadeMedida == null || unidadeMedida.isBlank()) {
			throw new IllegalArgumentException("A unidade de medida e obrigatoria.");
		}
		try {
			return UnidadeMedida.valueOf(unidadeMedida.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unidade de medida invalida. Valores validos: UN, L, KG, M, ML, CX, PC");
		}
	}

	private TipoItem parseTipoItem(String tipoItem) {
		if (tipoItem == null || tipoItem.isBlank()) {
			throw new IllegalArgumentException("O tipo do item e obrigatorio.");
		}
		try {
			return TipoItem.valueOf(tipoItem.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Tipo do item invalido. Valores validos: PECA, INSUMO");
		}
	}

}
