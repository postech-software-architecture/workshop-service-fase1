package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Caso de uso responsavel por criar um novo estoque para uma peca.
 */
@Service
public class CriarEstoqueUseCase {

	private final EstoqueRepository estoqueRepository;

	private final PecaInsumoRepository pecaInsumoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param estoqueRepository repositorio de estoques.
	 * @param pecaInsumoRepository repositorio de pecas.
	 */
	public CriarEstoqueUseCase(EstoqueRepository estoqueRepository, PecaInsumoRepository pecaInsumoRepository) {
		this.estoqueRepository = estoqueRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	/**
	 * Executa a criacao de um novo estoque.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao fisica.
	 * @param quantidade quantidade inicial.
	 * @return estoque criado.
	 */
	@Transactional
	public Estoque executar(UUID pecaInsumoId, String localizacao, BigDecimal quantidade) {
		try {
			validarPecaExiste(pecaInsumoId);

			String localizacaoNormalizada = normalizarLocalizacao(localizacao);

			if (estoqueRepository.existeLocalizacao(pecaInsumoId, localizacaoNormalizada, null)) {
				throw new RegraDeNegocioException("Ja existe um estoque com esta localizacao para a peca informada.");
			}

			Estoque estoque = new Estoque(null, pecaInsumoId, localizacaoNormalizada, quantidade);

			return estoqueRepository.salvar(estoque);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private void validarPecaExiste(UUID pecaInsumoId) {
		if (pecaInsumoRepository.buscarPorId(pecaInsumoId, false).isEmpty()) {
			throw new RegraDeNegocioException("Peca nao encontrada com o identificador informado.");
		}
	}

	private String normalizarLocalizacao(String localizacao) {
		if (localizacao == null || localizacao.isBlank()) {
			throw new IllegalArgumentException("A localizacao e obrigatoria.");
		}
		return localizacao.trim();
	}

}
