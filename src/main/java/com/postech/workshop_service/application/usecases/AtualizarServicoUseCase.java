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
 * Caso de uso responsável por atualizar os dados de um serviço existente no catálogo.
 */
@Service
public class AtualizarServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injeção de dependências.
	 * @param servicoRepository repositório de serviços.
	 */
	public AtualizarServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Executa a atualização de um serviço existente no catálogo.
	 * @param id identificador do serviço.
	 * @param nome novo nome do serviço.
	 * @param descricao nova descrição do serviço.
	 * @param valor novo valor cobrado pelo serviço.
	 * @param categoria nova categoria do serviço.
	 * @param nivelComplexidade novo nível de complexidade do serviço.
	 * @param garantiaDias nova quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas novas observações técnicas opcionais.
	 * @return serviço atualizado.
	 */
	@Transactional
	public Servico executar(UUID id, String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		Servico servico = servicoRepository.buscarPorId(id, true)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID informado."));

		try {
			if (servicoRepository.existeNomeAtivo(nome, id)) {
				throw new RegraDeNegocioException("Já existe um serviço ativo cadastrado com o nome informado.");
			}

			servico.atualizarDados(nome, descricao, valor, categoria, nivelComplexidade, garantiaDias,
					observacoesTecnicas);

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
