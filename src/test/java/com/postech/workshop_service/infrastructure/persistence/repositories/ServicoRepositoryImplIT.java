package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
class ServicoRepositoryImplIT extends PostgresTestContainer {

	@Autowired
	private ServicoRepositoryImpl servicoRepository;

	@Test
	void shouldSaveAndFindById() {
		Servico servico = criarServico("Troca de oleo", "Descricao completa", new BigDecimal("150.00"),
				CategoriaServico.PREVENTIVA);
		Servico salvo = servicoRepository.salvar(servico);

		assertNotNull(salvo.getId());

		Optional<Servico> encontrado = servicoRepository.buscarPorId(salvo.getId(), false);
		assertTrue(encontrado.isPresent());
		assertEquals("Troca de oleo", encontrado.get().getNome());
		assertEquals(new BigDecimal("150.00"), encontrado.get().getValor());
		assertTrue(encontrado.get().isAtivo());
	}

	@Test
	void shouldReturnEmptyForInactiveWithoutFlag() {
		Servico servico = criarServico("Balanceamento", "Balanceamento de rodas", new BigDecimal("80.00"), null);
		Servico salvo = servicoRepository.salvar(servico);

		salvo.removerLogicamente();
		servicoRepository.salvar(salvo);

		PaginaResultado<Servico> resultado = servicoRepository.listar(0, 10, null, null, false);
		assertTrue(resultado.itens().stream().noneMatch(s -> s.getId().equals(salvo.getId())));
	}

	@Test
	void shouldListWithPagination() {
		servicoRepository.salvar(criarServico("Servico A", "Descricao A", new BigDecimal("100.00"), null));
		servicoRepository.salvar(criarServico("Servico B", "Descricao B", new BigDecimal("200.00"), null));
		servicoRepository.salvar(criarServico("Servico C", "Descricao C", new BigDecimal("300.00"), null));

		PaginaResultado<Servico> pagina = servicoRepository.listar(0, 2, null, null, false);

		assertEquals(3, pagina.totalElementos());
		assertEquals(2, pagina.itens().size());
	}

	@Test
	void shouldDetectDuplicateName() {
		Servico servico = criarServico("Alinhamento", "Alinhamento de rodas", new BigDecimal("90.00"), null);
		Servico salvo = servicoRepository.salvar(servico);

		assertTrue(servicoRepository.existeNomeAtivo("Alinhamento", null));
		assertFalse(servicoRepository.existeNomeAtivo("Alinhamento", salvo.getId()));
		assertFalse(servicoRepository.existeNomeAtivo("Outro servico", null));
	}

	@Test
	void shouldRemoveLogically() {
		Servico servico = criarServico("Troca de correia", "Substituicao da correia dentada", new BigDecimal("450.00"),
				CategoriaServico.MECANICA);
		Servico salvo = servicoRepository.salvar(servico);

		assertTrue(salvo.isAtivo());

		salvo.removerLogicamente();
		servicoRepository.salvar(salvo);

		Optional<Servico> encontrado = servicoRepository.buscarPorId(salvo.getId(), true);
		assertTrue(encontrado.isPresent());
		assertFalse(encontrado.get().isAtivo());
		assertNotNull(encontrado.get().getDataRemocao());
	}

	@Test
	void shouldFilterByPartialName() {
		servicoRepository.salvar(criarServico("Troca de oleo", "Substituicao do oleo", new BigDecimal("100.00"),
				CategoriaServico.PREVENTIVA));
		servicoRepository.salvar(criarServico("Alinhamento", "Alinhamento de rodas", new BigDecimal("80.00"),
				CategoriaServico.MECANICA));

		PaginaResultado<Servico> resultado = servicoRepository.listar(0, 10, "oleo", null, false);

		assertEquals(1, resultado.totalElementos());
		assertEquals("Troca de oleo", resultado.itens().get(0).getNome());
	}

	@Test
	void shouldFilterByCategoria() {
		servicoRepository
			.salvar(criarServico("Servico mecanico", "Reparo", new BigDecimal("200.00"), CategoriaServico.MECANICA));
		servicoRepository.salvar(
				criarServico("Servico eletrico", "Diagnostico", new BigDecimal("150.00"), CategoriaServico.ELETRICA));

		PaginaResultado<Servico> resultado = servicoRepository.listar(0, 10, null, CategoriaServico.MECANICA, false);

		assertEquals(1, resultado.totalElementos());
		assertEquals(CategoriaServico.MECANICA, resultado.itens().get(0).getCategoria());
	}

	@Test
	void shouldIncludeInactivesWhenFlagOn() {
		Servico ativo = servicoRepository.salvar(criarServico("Ativo", "Servico ativo", new BigDecimal("50.00"), null));
		Servico inativo = servicoRepository
			.salvar(criarServico("Inativo", "Servico a remover", new BigDecimal("75.00"), null));
		inativo.removerLogicamente();
		servicoRepository.salvar(inativo);

		PaginaResultado<Servico> comFlag = servicoRepository.listar(0, 10, null, null, true);
		PaginaResultado<Servico> semFlag = servicoRepository.listar(0, 10, null, null, false);

		assertEquals(2, comFlag.totalElementos());
		assertEquals(1, semFlag.totalElementos());
		assertEquals(ativo.getId(), semFlag.itens().get(0).getId());
	}

	@Test
	void shouldListByCategoriaIncludingInactivesWhenFlagOn() {
		Servico ativo = servicoRepository
			.salvar(criarServico("Mecanica ativo", "X", new BigDecimal("100.00"), CategoriaServico.MECANICA));
		Servico inativo = servicoRepository
			.salvar(criarServico("Mecanica inativo", "Y", new BigDecimal("100.00"), CategoriaServico.MECANICA));
		inativo.removerLogicamente();
		servicoRepository.salvar(inativo);

		assertEquals(1, servicoRepository.listarPorCategoria(CategoriaServico.MECANICA, false).size());
		assertEquals(2, servicoRepository.listarPorCategoria(CategoriaServico.MECANICA, true).size());
		assertTrue(servicoRepository.listarPorCategoria(CategoriaServico.MECANICA, false)
			.stream()
			.allMatch(s -> s.getId().equals(ativo.getId())));
	}

	@Test
	void shouldAllowReusingNameAfterSoftDelete() {
		Servico original = servicoRepository.salvar(criarServico("Servico unico", "X", new BigDecimal("100.00"), null));
		original.removerLogicamente();
		servicoRepository.salvar(original);

		assertFalse(servicoRepository.existeNomeAtivo("Servico unico", null));

		Servico novo = servicoRepository.salvar(criarServico("Servico unico", "Y", new BigDecimal("120.00"), null));
		assertNotNull(novo.getId());
	}

	@Test
	void shouldReturnEmptyForInactiveBuscarPorIdWithoutFlag() {
		Servico salvo = servicoRepository.salvar(criarServico("Para remover", "X", new BigDecimal("100.00"), null));
		salvo.removerLogicamente();
		servicoRepository.salvar(salvo);

		assertTrue(servicoRepository.buscarPorId(salvo.getId(), false).isEmpty());
		assertTrue(servicoRepository.buscarPorId(salvo.getId(), true).isPresent());
	}

	private Servico criarServico(String nome, String descricao, BigDecimal valor, CategoriaServico categoria) {
		return new Servico(null, nome, descricao, valor, categoria, null, null, null);
	}

}
