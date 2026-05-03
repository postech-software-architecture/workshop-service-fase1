package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.Placa;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainCoverageEdgesTest {

	@Test
	void deveCobrirBordasDeValueObjects() {
		assertThrows(IllegalArgumentException.class, () -> Placa.normalizar(" "));
		assertEquals("Peca", UnidadeMedida.PC.getDescricao());
	}

	@Test
	void deveCobrirBordasDeMovimentacao() {
		UUID id = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		MovimentacaoEstoque entrada = new MovimentacaoEstoque(id, estoqueId, TipoMovimentacao.ENTRADA, BigDecimal.ONE,
				BigDecimal.ZERO, BigDecimal.ONE, null);
		MovimentacaoEstoque ajuste = new MovimentacaoEstoque(null, estoqueId, TipoMovimentacao.AJUSTE, BigDecimal.ONE,
				BigDecimal.ZERO, BigDecimal.ONE, " ajuste ");
		MovimentacaoEstoque saidaSemMotivo = new MovimentacaoEstoque(null, estoqueId, TipoMovimentacao.SAIDA,
				BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("9"), " ");

		assertEquals(id, entrada.getId());
		assertNull(entrada.getMotivo());
		assertTrue(ajuste.isAjuste());
		assertFalse(ajuste.isEntrada());
		assertFalse(ajuste.isSaida());
		assertNull(saidaSemMotivo.getMotivo());
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(null, null, TipoMovimentacao.ENTRADA,
				BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, "Entrada"));
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(null, estoqueId, null, BigDecimal.ONE,
				BigDecimal.ZERO, BigDecimal.ONE, "Entrada"));
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(null, estoqueId,
				TipoMovimentacao.ENTRADA, BigDecimal.ONE, null, BigDecimal.ONE, "Entrada"));
		assertThrows(NullPointerException.class, () -> new MovimentacaoEstoque(null, estoqueId,
				TipoMovimentacao.ENTRADA, BigDecimal.ONE, BigDecimal.ZERO, null, "Entrada"));
	}

	@Test
	void deveCobrirBordasDeUsuario() {
		Usuario usuario = new Usuario("admin", null, "hash", Set.of(Role.ADMINISTRADOR), null);
		Usuario removido = new Usuario(UUID.randomUUID(), "removido", null, "hash", Set.of(Role.ADMINISTRADOR), null,
				true, false, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

		assertNull(usuario.getEmail());
		assertFalse(usuario.possuiRole(Role.CLIENTE));
		assertFalse(removido.podeAutenticar());
		assertEquals(java.util.Objects.hash(usuario.getId()), usuario.hashCode());
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario(" ", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null));
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario("admin", " ", "hash", Collections.emptySet(), null));
	}

	@Test
	void deveCobrirBordasDeEnderecoECliente() {
		Endereco endereco = new Endereco("Rua", "1", null, "Bairro", "Cidade", "SP", null);
		Endereco persistido = new Endereco(UUID.randomUUID(), "Rua", "1", null, "Bairro", "Cidade", "SP", "01234567",
				LocalDateTime.now(), LocalDateTime.now(), null);
		Endereco persistidoSemCep = new Endereco(UUID.randomUUID(), "Rua", "1", null, "Bairro", "Cidade", "SP", null,
				LocalDateTime.now(), LocalDateTime.now(), null);
		Cliente clienteComEndereco = new Cliente(null, "Cliente", new Documento("98765432100"), "e@e.com", null,
				endereco, null, null);
		Cliente clienteSemEndereco = new Cliente(null, "Cliente", new Documento("12345678000195"), null, "11");

		clienteComEndereco.removerLogicamente();
		clienteComEndereco.removerLogicamente();
		clienteSemEndereco.removerLogicamente();

		assertNull(endereco.getCep());
		assertEquals("01234567", persistido.getCep());
		assertNull(persistidoSemCep.getCep());
		assertFalse(clienteComEndereco.isAtivo());
		assertFalse(clienteSemEndereco.isAtivo());
		assertThrows(IllegalArgumentException.class,
				() -> new Endereco(null, null, "1", null, "Bairro", "Cidade", "SP", "01234567"));
		assertThrows(IllegalArgumentException.class,
				() -> new Endereco(null, "Rua", "1", null, "Bairro", null, "SP", "01234567"));
		assertThrows(IllegalArgumentException.class,
				() -> new Endereco(null, "Rua", "1", null, "Bairro", "Cidade", null, "01234567"));
	}

	@Test
	void deveCobrirBordasDeServicoPecaVeiculoEstoqueERefreshToken() {
		assertThrows(IllegalArgumentException.class,
				() -> new Servico(null, "Servico", "Descricao", null, null, null, null, null));
		assertThrows(IllegalArgumentException.class,
				() -> new PecaInsumo(null, "SKU", "Peca", null, BigDecimal.ZERO, UnidadeMedida.UN, TipoItem.PECA));
		assertThrows(IllegalArgumentException.class,
				() -> new PecaInsumo(null, "SKU", "Peca", BigDecimal.ONE, null, UnidadeMedida.UN, TipoItem.PECA));
		PecaInsumo peca = new PecaInsumo(null, "SKU", "Peca", BigDecimal.ONE, BigDecimal.ZERO, UnidadeMedida.UN,
				TipoItem.PECA);
		peca.removerLogicamente();
		peca.removerLogicamente();
		assertFalse(peca.isAtivo());
		assertThrows(IllegalArgumentException.class, () -> new Veiculo("BRA1D23", "Toyota", "Corolla",
				LocalDateTime.now().getYear() + 1, null, null, List.of(UUID.randomUUID())));
		assertThrows(IllegalArgumentException.class,
				() -> new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, null));
		assertThrows(IllegalArgumentException.class, () -> new Estoque(null, UUID.randomUUID(), "A1", null));
		Estoque estoque = new Estoque(null, UUID.randomUUID(), "A1", BigDecimal.ONE);
		assertThrows(IllegalArgumentException.class, () -> estoque.registrarEntrada(null, "Entrada"));
		assertThrows(IllegalArgumentException.class, () -> estoque.registrarSaida(null, "Saida"));
		assertThrows(IllegalArgumentException.class, () -> estoque.ajustar(null, "Ajuste"));
		assertThrows(IllegalArgumentException.class,
				() -> new RefreshToken(null, UUID.randomUUID(), LocalDateTime.now().plusDays(1)));
	}

}
