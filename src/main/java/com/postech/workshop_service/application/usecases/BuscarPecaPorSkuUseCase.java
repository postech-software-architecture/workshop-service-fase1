package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Caso de uso responsavel por buscar uma peca pelo seu SKU.
 */
@Service
public class BuscarPecaPorSkuUseCase {

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public BuscarPecaPorSkuUseCase(PecaInsumoRepository pecaInsumoRepository) {
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Busca uma peca pelo seu SKU.
	 * @param sku codigo SKU da peca.
	 * @param incluirInativos indica se pecas inativas devem ser consideradas.
	 * @return peca encontrada, se existir.
	 */
	@Transactional(readOnly = true)
	public Optional<PecaInsumo> executar(String sku, boolean incluirInativos) {
		String skuNormalizado = sku != null ? sku.trim().toUpperCase() : null;
		return pecaInsumoRepository.buscarPorSku(skuNormalizado, incluirInativos);
	}

}
