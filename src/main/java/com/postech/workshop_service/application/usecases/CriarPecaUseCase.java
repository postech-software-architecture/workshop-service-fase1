package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Caso de uso responsavel por cadastrar uma nova peca ou insumo.
 */
@Service
public class CriarPecaUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public CriarPecaUseCase(PecaInsumoRepository pecaInsumoRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Executa o cadastro de uma nova peca.
	 * @param sku codigo SKU.
	 * @param nome nome da peca.
	 * @param valorUnitario valor unitario.
	 * @param estoqueMinimo nivel minimo para alerta.
	 * @param unidadeMedida unidade de medida.
	 * @param fornecedor fornecedor opcional.
	 * @param codigoBarras codigo de barras opcional.
	 * @param marca marca opcional.
	 * @param categoria categoria opcional.
	 * @param aplicacao aplicacao opcional.
	 * @param observacoes observacoes opcionais.
	 * @return peca persistida.
	 */
	@Transactional
	public PecaInsumo executar(String sku, String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			String unidadeMedida, String tipoItem, String fornecedor, String codigoBarras, String marca,
			String categoria, String aplicacao, String observacoes) {
		try {
			String skuNormalizado = normalizarSku(sku);
			UnidadeMedida unidade = parseUnidadeMedida(unidadeMedida);
			TipoItem tipo = parseTipoItem(tipoItem);

			if (pecaInsumoRepository.existeSkuAtivo(skuNormalizado, null)) {
				throw new RegraDeNegocioException("Ja existe uma peca ativa cadastrada com o SKU informado.");
			}

			PecaInsumo peca = new PecaInsumo(null, skuNormalizado, nome, valorUnitario, estoqueMinimo, unidade, tipo);
			peca.atualizarDados(nome, valorUnitario, estoqueMinimo, unidade, tipo, fornecedor, codigoBarras, marca,
					categoria, aplicacao, observacoes);

			return pecaInsumoRepository.salvar(peca);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private String normalizarSku(String sku) {
		if (sku == null || sku.isBlank()) {
			throw new IllegalArgumentException("O SKU e obrigatorio.");
		}
		return sku.trim().toUpperCase();
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
