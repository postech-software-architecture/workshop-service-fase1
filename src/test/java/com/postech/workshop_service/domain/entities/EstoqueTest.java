package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueTest {

	@Test
	void deveCriarEstoqueComDadosObrigatorios() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertNotNull(estoque.getId());
		assertEquals(pecaId, estoque.getPecaInsumoId());
		assertEquals("Prateleira A1", estoque.getLocalizacao());
		assertEquals(new BigDecimal("10"), estoque.getQuantidade());
		assertTrue(estoque.isAtivo());
		assertEquals(0, estoque.getVersao());
	}

	@Test
	void deveRegistrarEntrada() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		MovimentacaoEstoque movimentacao = estoque.registrarEntrada(new BigDecimal("5"), "Reposicao");

		assertEquals(new BigDecimal("15"), estoque.getQuantidade());
		assertEquals(TipoMovimentacao.ENTRADA, movimentacao.getTipo());
		assertEquals(new BigDecimal("5"), movimentacao.getQuantidade());
		assertEquals(new BigDecimal("10"), movimentacao.getQuantidadeAnterior());
		assertEquals(new BigDecimal("15"), movimentacao.getQuantidadePosterior());
		assertEquals("Reposicao", movimentacao.getMotivo());
	}

	@Test
	void deveRegistrarSaida() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		MovimentacaoEstoque movimentacao = estoque.registrarSaida(new BigDecimal("3"), "Venda");

		assertEquals(new BigDecimal("7"), estoque.getQuantidade());
		assertEquals(TipoMovimentacao.SAIDA, movimentacao.getTipo());
		assertEquals(new BigDecimal("3"), movimentacao.getQuantidade());
		assertEquals(new BigDecimal("10"), movimentacao.getQuantidadeAnterior());
		assertEquals(new BigDecimal("7"), movimentacao.getQuantidadePosterior());
	}

	@Test
	void deveLancarExcecaoAoRegistrarSaidaMaiorQueEstoque() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.registrarSaida(new BigDecimal("15"), "Venda"));
	}

	@Test
	void deveLancarExcecaoAoRegistrarSaidaComQuantidadeNegativa() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.registrarSaida(new BigDecimal("-5"), "Venda"));
	}

	@Test
	void deveLancarExcecaoAoRegistrarSaidaComQuantidadeZero() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.registrarSaida(BigDecimal.ZERO, "Venda"));
	}

	@Test
	void deveAjustarEstoqueParaMais() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		MovimentacaoEstoque movimentacao = estoque.ajustar(new BigDecimal("20"), "Contagem fisica");

		assertEquals(new BigDecimal("20"), estoque.getQuantidade());
		assertEquals(TipoMovimentacao.AJUSTE, movimentacao.getTipo());
		assertEquals(new BigDecimal("10"), movimentacao.getQuantidadeAnterior());
		assertEquals(new BigDecimal("20"), movimentacao.getQuantidadePosterior());
	}

	@Test
	void deveAjustarEstoqueParaMenos() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		MovimentacaoEstoque movimentacao = estoque.ajustar(new BigDecimal("5"), "Correcao");

		assertEquals(new BigDecimal("5"), estoque.getQuantidade());
		assertEquals(new BigDecimal("10"), movimentacao.getQuantidadeAnterior());
		assertEquals(new BigDecimal("5"), movimentacao.getQuantidadePosterior());
	}

	@Test
	void deveAjustarEstoqueParaZero() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		MovimentacaoEstoque movimentacao = estoque.ajustar(BigDecimal.ZERO, "Zerado");

		assertEquals(BigDecimal.ZERO, estoque.getQuantidade());
	}

	@Test
	void deveLancarExcecaoAoAjustarEstoqueParaNegativo() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.ajustar(new BigDecimal("-5"), "Invalido"));
	}

	@Test
	void deveLancarExcecaoAoAjustarEstoqueSemMotivo() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.ajustar(new BigDecimal("20"), null));
		assertThrows(IllegalArgumentException.class, () -> estoque.ajustar(new BigDecimal("20"), ""));
	}

	@Test
	void deveLancarExcecaoAoCriarEstoqueComPecaInsumoIdNulo() {
		assertThrows(NullPointerException.class, () -> new Estoque(null, null, "Prateleira A1", new BigDecimal("10")));
	}

	@Test
	void deveLancarExcecaoAoCriarEstoqueComLocalizacaoVazia() {
		assertThrows(IllegalArgumentException.class,
				() -> new Estoque(null, UUID.randomUUID(), null, new BigDecimal("10")));
		assertThrows(IllegalArgumentException.class,
				() -> new Estoque(null, UUID.randomUUID(), "", new BigDecimal("10")));
	}

	@Test
	void deveLancarExcecaoAoCriarEstoqueComQuantidadeNegativa() {
		assertThrows(IllegalArgumentException.class,
				() -> new Estoque(null, UUID.randomUUID(), "Prateleira A1", new BigDecimal("-5")));
	}

	@Test
	void deveCriarEstoqueComQuantidadeZero() {
		Estoque estoque = new Estoque(null, UUID.randomUUID(), "Prateleira A1", BigDecimal.ZERO);

		assertEquals(BigDecimal.ZERO, estoque.getQuantidade());
	}

	@Test
	void deveRegistrarEntradaComQuantidadeZero() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.registrarEntrada(BigDecimal.ZERO, "Teste"));
	}

	@Test
	void deveRegistrarEntradaComQuantidadeNegativa() {
		UUID pecaId = UUID.randomUUID();
		Estoque estoque = new Estoque(null, pecaId, "Prateleira A1", new BigDecimal("10"));

		assertThrows(IllegalArgumentException.class, () -> estoque.registrarEntrada(new BigDecimal("-5"), "Teste"));
	}

}
