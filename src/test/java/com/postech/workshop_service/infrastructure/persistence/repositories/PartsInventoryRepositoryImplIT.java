package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
class PartsInventoryRepositoryImplIT extends PostgresTestContainer {

	@Autowired
	private PecaInsumoRepositoryImpl pecaRepository;

	@Autowired
	private EstoqueRepositoryImpl estoqueRepository;

	@Autowired
	private MovimentacaoEstoqueRepositoryImpl movimentacaoRepository;

	@Test
	void shouldPersistAndFilterPartsInventoryAndMovements() {
		PecaInsumo filtro = pecaRepository.salvar(criarPeca("FIL-001", "Filtro de Oleo", "Filtros"));
		PecaInsumo vela = pecaRepository.salvar(criarPeca("VEL-001", "Vela de Ignicao", "Ignicao"));

		PaginaResultado<PecaInsumo> porNome = pecaRepository.listar(0, 10, "filtro", null, false);
		PaginaResultado<PecaInsumo> porCategoria = pecaRepository.listar(0, 10, null, "ignicao", false);

		assertEquals(1, porNome.totalElementos());
		assertEquals(filtro.getId(), porNome.itens().get(0).getId());
		assertEquals(1, porCategoria.totalElementos());
		assertEquals(vela.getId(), porCategoria.itens().get(0).getId());
		assertTrue(pecaRepository.existeSkuAtivo("FIL-001", null));
		assertFalse(pecaRepository.existeSkuAtivo("FIL-001", filtro.getId()));

		Estoque prateleiraA = estoqueRepository.salvar(new Estoque(null, filtro.getId(), "A1", new BigDecimal("10")));
		Estoque prateleiraB = estoqueRepository.salvar(new Estoque(null, filtro.getId(), "B1", new BigDecimal("5")));
		prateleiraB.removerLogicamente();
		estoqueRepository.salvar(prateleiraB);

		assertEquals(1, estoqueRepository.listarPorPeca(filtro.getId(), false).size());
		assertEquals(2, estoqueRepository.listarPorPeca(filtro.getId(), true).size());
		assertEquals(new BigDecimal("10.000"), estoqueRepository.calcularQuantidadeTotal(filtro.getId()));
		assertTrue(estoqueRepository.buscarPorPecaELocalizacao(filtro.getId(), "A1").isPresent());
		assertTrue(estoqueRepository.existeLocalizacao(filtro.getId(), "A1", null));
		assertFalse(estoqueRepository.existeLocalizacao(filtro.getId(), "A1", prateleiraA.getId()));

		MovimentacaoEstoque entrada = movimentacaoRepository
			.salvar(new MovimentacaoEstoque(null, prateleiraA.getId(), TipoMovimentacao.ENTRADA, new BigDecimal("2"),
					new BigDecimal("10"), new BigDecimal("12"), "Reposicao"));
		MovimentacaoEstoque saida = movimentacaoRepository.salvar(new MovimentacaoEstoque(null, prateleiraA.getId(),
				TipoMovimentacao.SAIDA, BigDecimal.ONE, new BigDecimal("12"), new BigDecimal("11"), "Venda"));

		Optional<MovimentacaoEstoque> encontrada = movimentacaoRepository.buscarPorId(entrada.getId());
		List<MovimentacaoEstoque> entradas = movimentacaoRepository.listarPorEstoque(prateleiraA.getId(),
				TipoMovimentacao.ENTRADA, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
		List<MovimentacaoEstoque> porPeca = movimentacaoRepository.listarPorPeca(filtro.getId(), null, null, null);

		assertTrue(encontrada.isPresent());
		assertEquals(TipoMovimentacao.ENTRADA, entradas.get(0).getTipo());
		assertEquals(2, porPeca.size());
		assertNotNull(saida.getId());
	}

	@Test
	void shouldHideInactivePartUnlessRequested() {
		PecaInsumo peca = pecaRepository.salvar(criarPeca("REM-001", "Removida", "Teste"));
		peca.removerLogicamente();
		pecaRepository.salvar(peca);

		assertTrue(pecaRepository.buscarPorId(peca.getId(), false).isEmpty());
		assertTrue(pecaRepository.buscarPorSku("REM-001", false).isEmpty());
		assertTrue(pecaRepository.buscarPorId(peca.getId(), true).isPresent());
		assertTrue(pecaRepository.buscarPorSku("REM-001", true).isPresent());
	}

	private PecaInsumo criarPeca(String sku, String nome, String categoria) {
		return new PecaInsumo(UUID.randomUUID(), sku, nome, new BigDecimal("40.00"), BigDecimal.ONE, UnidadeMedida.UN,
				TipoItem.PECA, "Fornecedor", null, "Marca", categoria, null, null, true, 0, LocalDateTime.now(),
				LocalDateTime.now(), null);
	}

}
