package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Caso de uso responsavel por atualizar os dados de um servico existente no catalogo.
 */
@Service
public class AtualizarServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public AtualizarServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Executa a atualizacao de um servico existente no catalogo.
	 * @param id identificador do servico.
	 * @param nome novo nome do servico.
	 * @param descricao nova descricao do servico.
	 * @param valor novo valor cobrado pelo servico.
	 * @param tempoEstimadoMinutos novo tempo estimado de execucao em minutos.
	 * @param categoria nova categoria do servico.
	 * @param nivelComplexidade novo nivel de complexidade do servico.
	 * @param garantiaDias nova quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas novas observacoes tecnicas opcionais.
	 * @return servico atualizado.
	 */
	@Transactional
	public Servico executar(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas) {
		Servico servico = servicoRepository.buscarPorId(id)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado com o ID informado."));

		try {
			if (servicoRepository.existeNomeAtivo(nome, id)) {
				throw new RegraDeNegocioException("Ja existe um servico ativo cadastrado com o nome informado.");
			}

			servico.atualizarDados(nome, descricao, valor, tempoEstimadoMinutos, categoria, nivelComplexidade,
					garantiaDias, observacoesTecnicas);

			return servicoRepository.salvar(servico);
		}
		catch (RegraDeNegocioException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
	}

}
