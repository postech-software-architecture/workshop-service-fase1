package com.postech.workshop_service.application.usecases;

/**
 * Dados de entrada para a abertura de uma ordem de servico.
 *
 * <p>
 * Objeto de aplicacao que isola o use case do DTO de transporte da camada de API. A
 * camada {@code api} converte o request HTTP para este record antes de invocar o use
 * case.
 * </p>
 *
 * @param clienteDocumento CPF ou CNPJ do cliente (com ou sem mascara).
 * @param veiculoPlaca placa do veiculo informada na recepcao.
 * @param veiculoMarca marca do veiculo para cadastro, quando a placa nao existir.
 * @param veiculoModelo modelo do veiculo para cadastro, quando a placa nao existir.
 * @param veiculoAno ano do veiculo para cadastro, quando a placa nao existir.
 * @param observacoes observacoes do atendente sobre o veiculo ou relato do cliente.
 */
public record DadosCriacaoOrdemServico(String clienteDocumento, String veiculoPlaca, String veiculoMarca,
		String veiculoModelo, Integer veiculoAno, String observacoes) {
}
