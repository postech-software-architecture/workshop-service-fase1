package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Veiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para o agregado de veiculos.
 */
public interface VeiculoRepository {

    /**
     * Persiste um veiculo no repositorio.
     *
     * @param veiculo agregado a ser persistido.
     * @return agregado persistido.
     */
    Veiculo salvar(Veiculo veiculo);

    /**
     * Busca um veiculo pelo seu identificador tecnico.
     *
     * @param id identificador do veiculo.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return veiculo encontrado, se existir.
     */
    Optional<Veiculo> buscarPorId(UUID id, boolean incluirInativos);

    /**
     * Busca um veiculo pela placa normalizada.
     *
     * @param placaNormalizada placa normalizada.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return veiculo encontrado, se existir.
     */
    Optional<Veiculo> buscarPorPlaca(String placaNormalizada, boolean incluirInativos);

    /**
     * Lista veiculos com filtros opcionais e paginacao.
     *
     * @param pagina pagina solicitada.
     * @param tamanho tamanho da pagina.
     * @param placaNormalizada placa filtrada opcional.
     * @param clienteId cliente filtrado opcional.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return resultado paginado de veiculos.
     */
    PaginaResultado<Veiculo> listar(int pagina, int tamanho, String placaNormalizada, UUID clienteId, boolean incluirInativos);

    /**
     * Lista todos os veiculos vinculados a um cliente.
     *
     * @param clienteId identificador do cliente.
     * @param incluirInativos indica se veiculos inativos devem ser considerados.
     * @return lista de veiculos vinculados ao cliente.
     */
    List<Veiculo> listarPorCliente(UUID clienteId, boolean incluirInativos);

    /**
     * Verifica se existe placa ativa para outro cadastro.
     *
     * @param placaNormalizada placa normalizada.
     * @param veiculoIdIgnorado identificador do veiculo a ser ignorado, quando aplicavel.
     * @return verdadeiro quando ja existir cadastro ativo com a placa.
     */
    boolean existePlacaAtiva(String placaNormalizada, UUID veiculoIdIgnorado);
}
