package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Caso de uso responsável por cadastrar um novo serviço no catálogo.
 */
@Service
public class CriarServicoUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injeção de dependências.
	 * @param servicoRepository repositório de serviços.
	 */
	public CriarServicoUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Executa o cadastro de um novo serviço no catálogo.
	 * @param nome nome do serviço.
	 * @param descricao descrição do serviço.
	 * @param valor valor cobrado pelo serviço.
	 * @param categoria categoria do serviço.
	 * @param nivelComplexidade nível de complexidade do serviço.
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observações técnicas opcionais.
	 * @return serviço persistido.
	 */
	@Transactional
	public Servico executar(String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		try {
			if (servicoRepository.existeNomeAtivo(nome, null)) {
				throw new RegraDeNegocioException("Já existe um serviço ativo cadastrado com o nome informado.");
			}

			Servico servico = new Servico(null, nome, descricao, valor, categoria, nivelComplexidade, garantiaDias,
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
