package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Abre uma OS recebida com servicos e pecas iniciais, sem antecipar o orcamento. */
@Service
public class CriarOrdemServicoComItensUseCase {

	private final ClienteRepository clienteRepository;

	private final VeiculoRepository veiculoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final PrepararItemComposicaoService prepararItemService;

	public CriarOrdemServicoComItensUseCase(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository,
			OrdemServicoRepository ordemServicoRepository, PrepararItemComposicaoService prepararItemService) {
		this.clienteRepository = clienteRepository;
		this.veiculoRepository = veiculoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.prepararItemService = prepararItemService;
	}

	@Transactional
	public ResultadoCriacaoOrdemServico executar(DadosCriacaoOrdemServicoComItens dados) {
		boolean semServicos = dados.servicos() == null || dados.servicos().isEmpty();
		boolean semPecas = dados.pecas() == null || dados.pecas().isEmpty();
		if (semServicos && semPecas) {
			throw new RegraDeNegocioException("Informe pelo menos um servico ou uma peca para a abertura da OS.");
		}
		Cliente cliente = resolverCliente(dados.clienteDocumento());
		Veiculo veiculo = resolverVeiculo(dados, cliente);
		List<ItemComposicaoTecnica> itens = new ArrayList<>();
		List<ItemServicoSolicitado> servicos = dados.servicos() == null ? List.of() : dados.servicos();
		servicos.forEach(servico -> itens
			.add(prepararItemService.prepararServico(servico.servicoId(), BigDecimal.valueOf(servico.quantidade()))));
		List<PrepararItemComposicaoService.PecaPreparada> pecas = dados.pecas() == null ? List.of()
				: dados.pecas()
					.stream()
					.map(peca -> prepararItemService.prepararPeca(peca.pecaId(), peca.quantidade()))
					.toList();
		pecas.forEach(peca -> itens.add(peca.item()));

		String numero = ordemServicoRepository.gerarProximoNumero(LocalDate.now().getYear());
		OrdemServico ordem = new OrdemServico(null, cliente.getId(), veiculo.getId(), numero, dados.observacoes(),
				itens);
		OrdemServico salva = ordemServicoRepository.salvar(ordem);
		pecas.forEach(peca -> prepararItemService.reservarPeca(peca, salva));
		return new ResultadoCriacaoOrdemServico(salva, null, cliente, veiculo);
	}

	private Cliente resolverCliente(String documento) {
		String normalizado = documento.replaceAll("[^0-9]", "");
		return clienteRepository.buscarPorDocumento(normalizado, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException(
					"Cliente nao encontrado para o documento informado. Realize o cadastro antes de abrir uma OS."));
	}

	private Veiculo resolverVeiculo(DadosCriacaoOrdemServicoComItens dados, Cliente cliente) {
		Placa placa;
		try {
			placa = new Placa(dados.veiculoPlaca());
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
		return veiculoRepository.buscarPorPlaca(placa.getValor(), false).map(veiculo -> {
			if (!veiculo.getClientesVinculados().contains(cliente.getId())) {
				throw new RegraDeNegocioException(
						"O veiculo informado nao pertence ao cliente. Verifique a placa ou o cliente.");
			}
			return veiculo;
		}).orElseGet(() -> cadastrarVeiculo(dados, placa, cliente));
	}

	private Veiculo cadastrarVeiculo(DadosCriacaoOrdemServicoComItens dados, Placa placa, Cliente cliente) {
		if (dados.veiculoMarca() == null || dados.veiculoModelo() == null || dados.veiculoAno() == null) {
			throw new RegraDeNegocioException(
					"Veiculo nao encontrado pela placa informada. Informe marca, modelo e ano para cadastra-lo.");
		}
		Veiculo veiculo = new Veiculo(placa.getValor(), dados.veiculoMarca(), dados.veiculoModelo(), dados.veiculoAno(),
				null, null, List.of(cliente.getId()));
		return veiculoRepository.salvar(veiculo);
	}

}
