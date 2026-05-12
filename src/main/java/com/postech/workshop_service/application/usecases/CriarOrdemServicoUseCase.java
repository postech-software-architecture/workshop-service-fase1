package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso responsavel pela recepcao do veiculo e abertura da OS sem itens.
 *
 * <p>
 * A OS nasce com status RECEBIDO e sem composicao tecnica. Itens, diagnostico e orcamento
 * sao incluidos em etapas posteriores via use cases dedicados.
 * </p>
 */
@Service
public class CriarOrdemServicoUseCase {

	private final ClienteRepository clienteRepository;

	private final VeiculoRepository veiculoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	public CriarOrdemServicoUseCase(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository,
			OrdemServicoRepository ordemServicoRepository) {
		this.clienteRepository = clienteRepository;
		this.veiculoRepository = veiculoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
	}

	@Transactional
	public ResultadoCriacaoOrdemServico executar(CriarOrdemServicoRequest request) {
		try {
			String clienteDocumento = request.getClienteDocumento();
			String veiculoPlaca = request.getVeiculoPlaca();
			String veiculoMarca = request.getVeiculo() != null ? request.getVeiculo().getMarca() : null;
			String veiculoModelo = request.getVeiculo() != null ? request.getVeiculo().getModelo() : null;
			Integer veiculoAno = request.getVeiculo() != null ? request.getVeiculo().getAno() : null;
			String observacoes = request.getObservacoes();

			Cliente cliente = resolverCliente(clienteDocumento);
			Veiculo veiculo = resolverVeiculo(veiculoPlaca, veiculoMarca, veiculoModelo, veiculoAno, cliente);

			String numero = ordemServicoRepository.gerarProximoNumero(LocalDate.now().getYear());
			OrdemServico os = new OrdemServico(null, cliente.getId(), veiculo.getId(), numero, observacoes, List.of());
			OrdemServico osSalva = ordemServicoRepository.salvar(os);

			return new ResultadoCriacaoOrdemServico(osSalva, null, cliente, veiculo);
		}
		catch (RegraDeNegocioException | RecursoNaoEncontradoException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

	private Cliente resolverCliente(String documento) {
		String documentoLimpo = documento.replaceAll("[^0-9]", "");
		return clienteRepository.buscarPorDocumento(documentoLimpo, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException(
					"Cliente nao encontrado para o documento informado. Realize o cadastro antes de abrir uma OS."));
	}

	private Veiculo resolverVeiculo(String placaRaw, String marca, String modelo, Integer ano, Cliente cliente) {
		Placa placa;
		try {
			placa = new Placa(placaRaw);
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}

		return veiculoRepository.buscarPorPlaca(placa.getValor(), false).map(v -> {
			if (!v.getClientesVinculados().contains(cliente.getId())) {
				throw new RegraDeNegocioException(
						"O veiculo informado nao pertence ao cliente. Verifique a placa ou o cliente.");
			}
			return v;
		}).orElseGet(() -> cadastrarNovoVeiculo(placa, marca, modelo, ano, cliente));
	}

	private Veiculo cadastrarNovoVeiculo(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
		if (marca == null || modelo == null || ano == null) {
			throw new RegraDeNegocioException(
					"Veiculo nao encontrado pela placa informada. Informe marca, modelo e ano para cadastra-lo.");
		}
		Veiculo novo = new Veiculo(placa.getValor(), marca, modelo, ano, null, null, List.of(cliente.getId()));
		return veiculoRepository.salvar(novo);
	}

}
