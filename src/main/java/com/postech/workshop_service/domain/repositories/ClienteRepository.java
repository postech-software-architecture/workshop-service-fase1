package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

	Cliente salvar(Cliente cliente);

	Optional<Cliente> buscarPorId(UUID id, boolean incluirInativos);

	Optional<Cliente> buscarPorDocumento(String documento, boolean incluirInativos);

	List<Cliente> listar(int pagina, int tamanho, boolean incluirInativos);

	long contarTodos();

	void remover(UUID id);

	boolean existePorDocumento(String documento);

}
