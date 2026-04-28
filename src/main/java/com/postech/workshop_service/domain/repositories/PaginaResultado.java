package com.postech.workshop_service.domain.repositories;

import java.util.List;

/**
 * Estrutura simples para retorno de resultados paginados.
 *
 * @param itens conteudo da pagina.
 * @param totalElementos total de registros encontrados.
 * @param totalPaginas total de paginas disponiveis.
 * @param pagina pagina atual.
 * @param tamanho tamanho solicitado.
 * @param <T> tipo dos itens retornados.
 */
public record PaginaResultado<T>(List<T> itens, long totalElementos, int totalPaginas, int pagina, int tamanho) {
}
