package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Caso de uso responsavel por cadastrar um novo servico no catalogo.
 */
@Service
public class CriarServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public CriarServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Executa o cadastro de um novo servico no catalogo.
	 * @param nome nome do servico.
	 * @param descricao descricao do servico.
	 * @param valor valor cobrado pelo servico.
	 * @param tempoEstimadoMinutos tempo estimado de execucao em minutos.
	 * @param categoria categoria do servico.
	 * @param nivelComplexidade nivel de complexidade do servico.
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observacoes tecnicas opcionais.
	 * @return servico persistido.
	 */
	@Transactional
	public Servico executar(String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas) {
		try {
			if (servicoRepository.existeNomeAtivo(nome, null)) {
				throw new RegraDeNegocioException("Ja existe um servico ativo cadastrado com o nome informado.");
			}

			Servico servico = new Servico(null, nome, descricao, valor, tempoEstimadoMinutos, categoria,
					nivelComplexidade, garantiaDias, observacoesTecnicas);

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
