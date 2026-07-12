package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso responsavel pela listagem paginada de veiculos.
 */
@Service
public class ListarVeiculosUseCase {

	private final VeiculoRepository veiculoRepository;

	private final ClienteRepository clienteRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param veiculoRepository repositorio de veiculos.
	 * @param clienteRepository repositorio de clientes.
	 */
	public ListarVeiculosUseCase(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
		this.veiculoRepository = veiculoRepository;
		this.clienteRepository = clienteRepository;
	}

	/**
	 * Lista veiculos com filtros opcionais.
	 * @param pagina pagina desejada.
	 * @param tamanho tamanho solicitado.
	 * @param placaRaw placa opcional para filtro.
	 * @param clienteId cliente opcional para filtro.
	 * @param incluirInativos indica se veiculos inativos devem ser considerados.
	 * @return resultado paginado de veiculos.
	 */
	public PaginaResultado<Veiculo> executar(int pagina, int tamanho, String placaRaw, UUID clienteId,
			boolean incluirInativos) {
		if (clienteId != null && clienteRepository.buscarPorId(clienteId, false).isEmpty()) {
			throw new RecursoNaoEncontradoException("Cliente não encontrado com o ID informado.");
		}
		String placaNormalizada = placaRaw != null && !placaRaw.isBlank() ? Placa.normalizar(placaRaw) : null;
		return veiculoRepository.listar(pagina, tamanho, placaNormalizada, clienteId, incluirInativos);
	}

}
