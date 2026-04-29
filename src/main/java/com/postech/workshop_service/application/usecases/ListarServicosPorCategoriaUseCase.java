package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso responsavel por listar todos os servicos de uma categoria especifica.
 */
@Service
public class ListarServicosPorCategoriaUseCase {

	private final ServicoRepository servicoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param servicoRepository repositorio de servicos.
	 */
	public ListarServicosPorCategoriaUseCase(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	/**
	 * Lista todos os servicos pertencentes a uma categoria.
	 * @param categoria categoria dos servicos a listar.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return lista de servicos da categoria informada.
	 */
	public List<Servico> executar(CategoriaServico categoria, boolean incluirInativos) {
		return servicoRepository.listarPorCategoria(categoria, incluirInativos);
	}

}
