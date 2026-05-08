package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Caso de uso responsavel por registrar movimentacoes de estoque.
 */
@Service
public class RegistrarMovimentacaoUseCase {

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param estoqueRepository repositorio de estoques.
	 * @param movimentacaoEstoqueRepository repositorio de movimentacoes.
	 */
	public RegistrarMovimentacaoUseCase(EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	/**
	 * Registra uma movimentacao de estoque.
	 * @param estoqueId identificador do estoque.
	 * @param tipo tipo de movimentacao.
	 * @param quantidade quantidade movimentada.
	 * @param motivo motivo da movimentacao.
	 * @return movimentacao registrada.
	 */
	@Transactional
	public MovimentacaoEstoque executar(UUID estoqueId, String tipo, BigDecimal quantidade, String motivo) {
		try {
			TipoMovimentacao tipoMovimentacao = parseTipoMovimentacao(tipo);

			Estoque estoque = estoqueRepository.buscarPorId(estoqueId, false)
				.orElseThrow(
						() -> new RegraDeNegocioException("Estoque nao encontrado com o identificador informado."));

			MovimentacaoEstoque movimentacao = switch (tipoMovimentacao) {
				case ENTRADA -> estoque.registrarEntrada(quantidade, motivo);
				case SAIDA -> estoque.registrarSaida(quantidade, motivo);
				case AJUSTE -> throw new RegraDeNegocioException(
						"Os tipos AJUSTE, RESERVA e LIBERACAO sao gerenciados internamente pelo sistema e nao podem ser registrados manualmente.");
				case RESERVA, LIBERACAO -> throw new RegraDeNegocioException(
						"Os tipos AJUSTE, RESERVA e LIBERACAO sao gerenciados internamente pelo sistema e nao podem ser registrados manualmente.");
			};

			estoqueRepository.salvar(estoque);
			return movimentacaoEstoqueRepository.salvar(movimentacao);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (ObjectOptimisticLockingFailureException ex) {
			throw new RegraDeNegocioException(
					"Este estoque foi modificado por outro usuario. Por favor, tente novamente.");
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private TipoMovimentacao parseTipoMovimentacao(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			throw new IllegalArgumentException("O tipo de movimentacao e obrigatorio.");
		}
		try {
			return TipoMovimentacao.valueOf(tipo.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Tipo de movimentacao invalido. Valores validos: ENTRADA, SAIDA");
		}
	}

}
