package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimentacaoEstoqueTest {

	@Test
	void deveCriarEntradaComMotivoNormalizado() {
		MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(null, UUID.randomUUID(), TipoMovimentacao.ENTRADA,
				new BigDecimal("5"), BigDecimal.TEN, new BigDecimal("15"), "  compra   fornecedor ");

		assertNotNull(movimentacao.getId());
		assertTrue(movimentacao.isEntrada());
		assertFalse(movimentacao.isSaida());
		assertFalse(movimentacao.isAjuste());
		assertEquals("compra fornecedor", movimentacao.getMotivo());
		assertEquals(movimentacao.getDataMovimentacao(), movimentacao.getDataCriacao());
	}

	@Test
	void deveReconstituirMovimentacaoPersistida() {
		UUID id = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime dataMovimentacao = LocalDateTime.now().minusHours(2);
		LocalDateTime dataCriacao = LocalDateTime.now().minusHours(1);

		MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(id, estoqueId, TipoMovimentacao.SAIDA,
				BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("9"), "Venda", dataMovimentacao, dataCriacao);

		assertEquals(id, movimentacao.getId());
		assertEquals(estoqueId, movimentacao.getEstoqueId());
		assertTrue(movimentacao.isSaida());
		assertEquals(dataMovimentacao, movimentacao.getDataMovimentacao());
		assertEquals(dataCriacao, movimentacao.getDataCriacao());
	}

	@Test
	void deveExigirMotivoParaAjuste() {
		assertThrows(IllegalArgumentException.class, () -> new MovimentacaoEstoque(null, UUID.randomUUID(),
				TipoMovimentacao.AJUSTE, BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("11"), " "));
	}

	@Test
	void deveValidarCamposObrigatoriosNaReconstituicao() {
		UUID id = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(id, estoqueId, TipoMovimentacao.ENTRADA,
				null, BigDecimal.TEN, BigDecimal.TEN, "Entrada", agora, agora));
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(id, estoqueId, TipoMovimentacao.ENTRADA,
				BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "Entrada", null, agora));
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(id, estoqueId, TipoMovimentacao.ENTRADA,
				BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "Entrada", agora, null));
	}

}
