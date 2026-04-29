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
		Servico servico = criarServico("Troca de oleo", "Descricao completa", new BigDecimal("150.00"), 60,
				CategoriaServico.PREVENTIVA);
		Servico salvo = servicoRepository.salvar(servico);

		assertNotNull(salvo.getId());

		Optional<Servico> encontrado = servicoRepository.buscarPorId(salvo.getId());
		assertTrue(encontrado.isPresent());
		assertEquals("Troca de oleo", encontrado.get().getNome());
		assertEquals(new BigDecimal("150.00"), encontrado.get().getValor());
		assertTrue(encontrado.get().isAtivo());
	}

	@Test
	void shouldReturnEmptyForInactiveWithoutFlag() {
		Servico servico = criarServico("Balanceamento", "Balanceamento de rodas", new BigDecimal("80.00"), 30, null);
		Servico salvo = servicoRepository.salvar(servico);

		salvo.removerLogicamente();
		servicoRepository.salvar(salvo);

		PaginaResultado<Servico> resultado = servicoRepository.listar(0, 10, null, null, false);
		assertTrue(resultado.itens().stream().noneMatch(s -> s.getId().equals(salvo.getId())));
	}

	@Test
	void shouldListWithPagination() {
		servicoRepository.salvar(criarServico("Servico A", "Descricao A", new BigDecimal("100.00"), 60, null));
		servicoRepository.salvar(criarServico("Servico B", "Descricao B", new BigDecimal("200.00"), 90, null));
		servicoRepository.salvar(criarServico("Servico C", "Descricao C", new BigDecimal("300.00"), 120, null));

		PaginaResultado<Servico> pagina = servicoRepository.listar(0, 2, null, null, false);

		assertEquals(3, pagina.totalElementos());
		assertEquals(2, pagina.itens().size());
	}

	@Test
	void shouldDetectDuplicateName() {
		Servico servico = criarServico("Alinhamento", "Alinhamento de rodas", new BigDecimal("90.00"), 30, null);
		Servico salvo = servicoRepository.salvar(servico);

		assertTrue(servicoRepository.existeNomeAtivo("Alinhamento", null));
		assertFalse(servicoRepository.existeNomeAtivo("Alinhamento", salvo.getId()));
		assertFalse(servicoRepository.existeNomeAtivo("Outro servico", null));
	}

	@Test
	void shouldRemoveLogically() {
		Servico servico = criarServico("Troca de correia", "Substituicao da correia dentada", new BigDecimal("450.00"),
				180, CategoriaServico.MECANICA);
		Servico salvo = servicoRepository.salvar(servico);

		assertTrue(salvo.isAtivo());

		salvo.removerLogicamente();
		servicoRepository.salvar(salvo);

		Optional<Servico> encontrado = servicoRepository.buscarPorId(salvo.getId());
		assertTrue(encontrado.isPresent());
		assertFalse(encontrado.get().isAtivo());
		assertNotNull(encontrado.get().getDataRemocao());
	}

	private Servico criarServico(String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria) {
		return new Servico(null, nome, descricao, valor, tempoEstimadoMinutos, categoria, null, null, null);
	}

}
