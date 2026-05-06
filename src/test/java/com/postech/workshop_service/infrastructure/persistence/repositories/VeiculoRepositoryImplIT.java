package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class VeiculoRepositoryImplIT extends PostgresTestContainer {

	@Autowired
	private VeiculoRepositoryImpl veiculoRepository;

	@Autowired
	private ClienteRepositoryImpl clienteRepository;

	@Test
	void shouldSaveUpdateAndFindVeiculo() {
		UUID clienteId = criarCliente("Cliente Repo", "98765432100");
		UUID segundoClienteId = criarCliente("Cliente Repo 2", "11144477735");

		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, "Prata", "Obs", List.of(clienteId));
		Veiculo salvo = veiculoRepository.salvar(veiculo);

		salvo.atualizarDados("ABC1234", "Ford", "Focus", 2019, "Azul", "Atualizado");
		salvo.vincularCliente(segundoClienteId);
		veiculoRepository.salvar(salvo);

		Optional<Veiculo> encontrado = veiculoRepository.buscarPorId(salvo.getId(), false);
		assertTrue(encontrado.isPresent());
		assertEquals("ABC1234", encontrado.get().getPlaca().getValor());
		assertEquals(2, encontrado.get().getClientesVinculados().size());
	}

	@Test
	void shouldListFilterAndRespectSoftDelete() {
		UUID clienteId = criarCliente("Cliente Lista", "12345678909");
		UUID outroClienteId = criarCliente("Outro Cliente", "52998224725");

		Veiculo veiculoAtivo = veiculoRepository
			.salvar(new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId)));
		Veiculo veiculoInativo = veiculoRepository
			.salvar(new Veiculo("ABC1234", "Ford", "Focus", 2018, null, null, List.of(outroClienteId)));
		veiculoInativo.removerLogicamente();
		veiculoRepository.salvar(veiculoInativo);

		PaginaResultado<Veiculo> ativos = veiculoRepository.listar(0, 10, null, null, false);
		assertEquals(1, ativos.totalElementos());

		PaginaResultado<Veiculo> todos = veiculoRepository.listar(0, 10, null, null, true);
		assertEquals(2, todos.totalElementos());

		List<Veiculo> porCliente = veiculoRepository.listarPorCliente(clienteId, false);
		assertEquals(1, porCliente.size());
		assertEquals(veiculoAtivo.getId(), porCliente.get(0).getId());
	}

	@Test
	void shouldReusePlateAfterLogicalRemoval() {
		UUID clienteId = criarCliente("Cliente Reuso", "86288366757");

		Veiculo original = veiculoRepository
			.salvar(new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId)));
		assertTrue(veiculoRepository.existePlacaAtiva("BRA1D23", null));

		original.removerLogicamente();
		veiculoRepository.salvar(original);

		assertFalse(veiculoRepository.existePlacaAtiva("BRA1D23", null));
	}

	private UUID criarCliente(String nome, String documento) {
		Cliente cliente = new Cliente(UUID.randomUUID(), nome, new Documento(documento),
				nome.toLowerCase().replace(" ", ".") + "@teste.com", null);
		return clienteRepository.salvar(cliente).getId();
	}

}
