package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Endereco;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClienteRepositoryImplIT extends PostgresTestContainer {

    @Autowired
    private ClienteRepositoryImpl repository;

    @Test
    void shouldSaveUpdateAndFindCliente() {
        // Create
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente(
            id, 
            "Teste Repo", 
            new Documento("98765432100"), 
            "repo@test.com", 
            "11999999999"
        );
        repository.salvar(cliente);

        // Update with address (new)
        cliente.atualizarDados(
            "Novo Nome", "novo@test.com", "11888888888",
            new Endereco("Rua T", "1", null, "B", "C", "SP", "01234567"),
            null, "Obs"
        );
        repository.salvar(cliente);

        Optional<Cliente> found = repository.buscarPorId(id);
        assertTrue(found.isPresent());
        assertEquals("Novo Nome", found.get().getNome());
        assertEquals("01234567", found.get().getEndereco().getCep());

        // Update existing address
        cliente.atualizarDados(
            "Novo Nome", "novo@test.com", "11888888888",
            new Endereco("Rua U", "2", null, "B", "C", "SP", "01234567"),
            null, "Obs"
        );
        repository.salvar(cliente);
        
        found = repository.buscarPorId(id);
        assertEquals("Rua U", found.get().getEndereco().getLogradouro());

        // Remove address
        cliente.atualizarDados(
            "Novo Nome", "novo@test.com", "11888888888",
            null,
            null, "Obs"
        );
        repository.salvar(cliente);
        
        found = repository.buscarPorId(id);
        assertNull(found.get().getEndereco());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Cliente> found = repository.buscarPorId(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnEmptyByDocumentWhenNotFound() {
        Optional<Cliente> found = repository.buscarPorDocumento("000");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDelete() {
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente(id, "D", new Documento("98765432100"), "d@d.com", "1");
        repository.salvar(cliente);
        repository.remover(id);
        assertTrue(repository.buscarPorId(id).isEmpty());
    }

    @Test
    void shouldExistByDocument() {
        String doc = "12345678909";
        Cliente cliente = new Cliente(UUID.randomUUID(), "E", new Documento(doc), "e@e.com", "1");
        repository.salvar(cliente);
        assertTrue(repository.existePorDocumento(doc));
    }
}
