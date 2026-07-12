package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.FiltrosOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso responsavel pela listagem paginada de ordens de servico para usuarios
 * administrativos.
 */
@Service
public class ListarOrdensServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 */
	public ListarOrdensServicoUseCase(OrdemServicoRepository ordemServicoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
	}

	/**
	 * Lista ordens de servico aplicando filtros opcionais.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param status status opcional.
	 * @param idCliente cliente opcional.
	 * @param dataInicio data minima (inclusiva) opcional.
	 * @param dataFim data maxima (exclusiva) opcional.
	 * @return resultado paginado.
	 */
	public PaginaResultado<OrdemServico> executar(int pagina, int tamanho, StatusOrdemServico status, UUID idCliente,
			LocalDateTime dataInicio, LocalDateTime dataFim) {
		FiltrosOrdemServico filtros = FiltrosOrdemServico.listagem(status, idCliente, dataInicio, dataFim);
		return ordemServicoRepository.listar(pagina, tamanho, filtros);
	}

	/**
	 * Lista a fila de trabalho: exclui ordens encerradas (FINALIZADA, ENTREGUE,
	 * CANCELADA) e ordena por prioridade de status e, dentro do mesmo status, por
	 * antiguidade.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @return resultado paginado no modo fila de trabalho.
	 */
	public PaginaResultado<OrdemServico> executarFilaTrabalho(int pagina, int tamanho) {
		return ordemServicoRepository.listar(pagina, tamanho, FiltrosOrdemServico.filaTrabalho(null));
	}

}
