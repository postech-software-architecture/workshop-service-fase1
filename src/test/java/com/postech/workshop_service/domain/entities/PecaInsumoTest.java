package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PecaInsumoTest {

	@Test
	void deveCriarPecaComDadosObrigatorios() {
		PecaInsumo peca = new PecaInsumo(null, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		assertNotNull(peca);
		assertEquals("FIL-001", peca.getSku());
		assertEquals("Filtro de Oleo", peca.getNome());
		assertEquals(new BigDecimal("45.90"), peca.getValorUnitario());
		assertEquals(new BigDecimal("5"), peca.getEstoqueMinimo());
		assertEquals(UnidadeMedida.UN, peca.getUnidadeMedida());
		assertTrue(peca.isAtivo());
	}

	@Test
	void deveCriarPecaComDadosCompletos() {
		UUID id = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		PecaInsumo peca = new PecaInsumo(id, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"), new BigDecimal("5"),
				UnidadeMedida.UN, TipoItem.PECA, "Bosch", "7891234567890", "Bosch", "Filtros", "Motor 1.0",
				"Observacao", true, 0, agora, agora, null);

		assertEquals(TipoItem.PECA, peca.getTipoItem());
		assertEquals("Bosch", peca.getFornecedor());
		assertEquals("7891234567890", peca.getCodigoBarras());
		assertEquals("Bosch", peca.getMarca());
		assertEquals("Filtros", peca.getCategoria());
		assertEquals("Motor 1.0", peca.getAplicacao());
		assertEquals("Observacao", peca.getObservacoes());
	}

	@Test
	void deveAtualizarDadosDaPeca() {
		PecaInsumo peca = new PecaInsumo(null, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		peca.atualizarDados("Filtro de Oleo Premium", new BigDecimal("55.90"), new BigDecimal("10"), UnidadeMedida.UN,
				TipoItem.INSUMO, "Bosch", "7891234567890", "Bosch Premium", "Filtros", "Motor 1.0 e 1.6", "Atualizado");

		assertEquals("Filtro de Oleo Premium", peca.getNome());
		assertEquals(new BigDecimal("55.90"), peca.getValorUnitario());
		assertEquals(new BigDecimal("10"), peca.getEstoqueMinimo());
		assertEquals(TipoItem.INSUMO, peca.getTipoItem());
		assertEquals("Bosch", peca.getFornecedor());
		assertNotNull(peca.getDataUltimaAtualizacao());
	}

	@Test
	void deveRemoverPecaComSoftDelete() {
		PecaInsumo peca = new PecaInsumo(null, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		peca.removerLogicamente();

		assertFalse(peca.isAtivo());
		assertNotNull(peca.getDataRemocao());
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComSkuVazio() {
		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, null, "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA));

		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, "", "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComNomeVazio() {
		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, "FIL-001", null,
				new BigDecimal("45.90"), new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComValorUnitarioNegativo() {
		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, "FIL-001", "Filtro de Oleo",
				new BigDecimal("-10.00"), new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComValorUnitarioZero() {
		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, "FIL-001", "Filtro de Oleo",
				BigDecimal.ZERO, new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComEstoqueMinimoNegativo() {
		assertThrows(IllegalArgumentException.class, () -> new PecaInsumo(null, "FIL-001", "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("-5"), UnidadeMedida.UN, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComUnidadeMedidaNula() {
		assertThrows(NullPointerException.class, () -> new PecaInsumo(null, "FIL-001", "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("5"), null, TipoItem.PECA));
	}

	@Test
	void deveLancarExcecaoAoCriarPecaComTipoItemNulo() {
		assertThrows(NullPointerException.class, () -> new PecaInsumo(null, "FIL-001", "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("5"), UnidadeMedida.UN, null));
	}

}
