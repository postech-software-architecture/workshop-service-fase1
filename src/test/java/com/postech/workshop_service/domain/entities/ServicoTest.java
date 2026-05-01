package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicoTest {

	@Test
	void shouldCreateServicoWithValidData() {
		Servico servico = criarServico("Troca de oleo", "Descricao valida", new BigDecimal("100.00"),
				CategoriaServico.PREVENTIVA, NivelComplexidade.BAIXA, 30, "Obs tecnica");

		assertNotNull(servico.getId());
		assertEquals("Troca de oleo", servico.getNome());
		assertEquals("Descricao valida", servico.getDescricao());
		assertEquals(new BigDecimal("100.00"), servico.getValor());
		assertEquals(CategoriaServico.PREVENTIVA, servico.getCategoria());
		assertEquals(NivelComplexidade.BAIXA, servico.getNivelComplexidade());
		assertEquals(30, servico.getGarantiaDias());
		assertEquals("Obs tecnica", servico.getObservacoesTecnicas());
		assertTrue(servico.isAtivo());
		assertNotNull(servico.getDataCriacao());
		assertNotNull(servico.getDataUltimaAtualizacao());
		assertNull(servico.getDataRemocao());
	}

	@Test
	void shouldRemoveLogically() {
		Servico servico = criarServico("Balanceamento", "Descricao", new BigDecimal("80.00"), null, null, null, null);

		servico.removerLogicamente();

		assertFalse(servico.isAtivo());
		assertNotNull(servico.getDataRemocao());
	}

	@Test
	void shouldBeIdempotentWhenRemovingLogicallyTwice() {
		Servico servico = criarServico("Alinhamento", "Descricao", new BigDecimal("90.00"), null, null, null, null);

		servico.removerLogicamente();
		LocalDateTime primeiraRemocao = servico.getDataRemocao();

		servico.removerLogicamente();

		assertFalse(servico.isAtivo());
		assertEquals(primeiraRemocao, servico.getDataRemocao());
	}

	@Test
	void shouldReativarServicoLogicamenteRemovido() {
		Servico servico = criarServico("Lavagem", "Descricao", new BigDecimal("70.00"), null, null, null, null);

		servico.removerLogicamente();
		assertFalse(servico.isAtivo());
		assertNotNull(servico.getDataRemocao());

		servico.reativar();

		// Reativar deve restaurar o serviço sem deixar marcação de remoção. A
		// asserção foca nos efeitos funcionais, não em avanço de relógio (que
		// poderia gerar flakiness em CI com resolução de clock baixa).
		assertTrue(servico.isAtivo());
		assertNull(servico.getDataRemocao());
		assertNotNull(servico.getDataUltimaAtualizacao());
	}

	@Test
	void shouldBeIdempotentWhenReativarOnAlreadyActive() {
		Servico servico = criarServico("Polimento", "Descricao", new BigDecimal("120.00"), null, null, null, null);
		LocalDateTime atualizacaoOriginal = servico.getDataUltimaAtualizacao();

		servico.reativar();

		// Sem Thread.sleep: comparamos a referência exata. Como reativar() em
		// serviço já ativo retorna cedo sem chamar atualizarDataUltimaAtualizacao,
		// o timestamp deve ser exatamente o mesmo.
		assertTrue(servico.isAtivo());
		assertEquals(atualizacaoOriginal, servico.getDataUltimaAtualizacao());
		assertNull(servico.getDataRemocao());
	}

	@Test
	void shouldRejectNullNome() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico(null, "Descricao", new BigDecimal("100.00"), null, null, null, null));
	}

	@Test
	void shouldRejectBlankNome() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico("   ", "Descricao", new BigDecimal("100.00"), null, null, null, null));
	}

	@Test
	void shouldRejectZeroValor() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico("Servico X", "Descricao", BigDecimal.ZERO, null, null, null, null));
	}

	@Test
	void shouldRejectNegativeValor() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico("Servico X", "Descricao", new BigDecimal("-1.00"), null, null, null, null));
	}

	@Test
	void shouldRejectZeroGarantiaDias() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico("Servico X", "Descricao", new BigDecimal("100.00"), null, null, 0, null));
	}

	@Test
	void shouldRejectNegativeGarantiaDias() {
		assertThrows(IllegalArgumentException.class,
				() -> criarServico("Servico X", "Descricao", new BigDecimal("100.00"), null, null, -5, null));
	}

	@Test
	void shouldSanitizeNomeWithExtraSpaces() {
		Servico servico = criarServico("  Troca  de  oleo  ", "Descricao", new BigDecimal("100.00"), null, null, null,
				null);

		assertEquals("Troca de oleo", servico.getNome());
	}

	@Test
	void shouldAcceptNullGarantiaDias() {
		Servico servico = criarServico("Servico sem garantia", "Descricao", new BigDecimal("50.00"), null, null, null,
				null);

		assertNull(servico.getGarantiaDias());
	}

	@Test
	void shouldUpdateDataAndTimestamp() {
		Servico servico = criarServico("Nome original", "Descricao original", new BigDecimal("100.00"),
				CategoriaServico.MECANICA, NivelComplexidade.MEDIA, 30, "Obs original");
		LocalDateTime anteriorAtualizacao = servico.getDataUltimaAtualizacao();

		servico.atualizarDados("Nome atualizado", "Descricao atualizada", new BigDecimal("200.00"),
				CategoriaServico.ELETRICA, NivelComplexidade.ALTA, 60, "Nova obs");

		assertEquals("Nome atualizado", servico.getNome());
		assertEquals("Descricao atualizada", servico.getDescricao());
		assertEquals(new BigDecimal("200.00"), servico.getValor());
		assertEquals(CategoriaServico.ELETRICA, servico.getCategoria());
		assertEquals(NivelComplexidade.ALTA, servico.getNivelComplexidade());
		assertEquals(60, servico.getGarantiaDias());
		assertEquals("Nova obs", servico.getObservacoesTecnicas());
		assertTrue(servico.getDataUltimaAtualizacao().isAfter(anteriorAtualizacao)
				|| servico.getDataUltimaAtualizacao().isEqual(anteriorAtualizacao));
	}

	private Servico criarServico(String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		return new Servico(null, nome, descricao, valor, categoria, nivelComplexidade, garantiaDias,
				observacoesTecnicas);
	}

}
