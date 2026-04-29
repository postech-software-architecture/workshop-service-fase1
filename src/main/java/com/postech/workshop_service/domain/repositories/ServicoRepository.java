package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para o agregado de servicos.
 */
public interface ServicoRepository {

	/**
	 * Persiste um servico no repositorio.
	 * @param servico agregado a ser persistido.
	 * @return agregado persistido.
	 */
	Servico salvar(Servico servico);

	/**
	 * Busca um servico pelo seu identificador tecnico.
	 * @param id identificador do servico.
	 * @return servico encontrado, se existir.
	 */
	Optional<Servico> buscarPorId(UUID id);

	/**
	 * Lista servicos com filtros opcionais e paginacao.
	 * @param pagina pagina solicitada.
	 * @param tamanho tamanho da pagina.
	 * @param nome nome filtrado opcional.
	 * @param categoria categoria filtrada opcional.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return resultado paginado de servicos.
	 */
	PaginaResultado<Servico> listar(int pagina, int tamanho, String nome, CategoriaServico categoria,
			boolean incluirInativos);

	/**
	 * Lista todos os servicos de uma categoria.
	 * @param categoria categoria filtrada.
	 * @param incluirInativos indica se servicos inativos devem ser considerados.
	 * @return lista de servicos da categoria.
	 */
	List<Servico> listarPorCategoria(CategoriaServico categoria, boolean incluirInativos);

	/**
	 * Verifica se ja existe servico ativo com o mesmo nome, excluindo opcionalmente um
	 * registro especifico (util para validacao em atualizacoes).
	 * @param nome nome do servico a verificar.
	 * @param idExcluir identificador do servico a ser ignorado na verificacao, quando
	 * aplicavel.
	 * @return verdadeiro quando ja existir servico ativo com o nome informado.
	 */
	boolean existeNomeAtivo(String nome, UUID idExcluir);

}
