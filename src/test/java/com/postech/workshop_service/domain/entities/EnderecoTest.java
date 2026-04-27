package com.postech.workshop_service.domain.entities;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EnderecoTest {

    @Test
    void shouldCreateEnderecoWithId() {
        UUID id = UUID.randomUUID();
        Endereco endereco = new Endereco(
            id, "Rua A", "123", "Apt 1", "Bairro X", "São Paulo", "SP", "01234-567"
        );

        assertEquals(id, endereco.getId());
        assertEquals("Rua A", endereco.getLogradouro());
        assertEquals("01234567", endereco.getCep());
    }

    @Test
    void shouldGenerateIdWhenNull() {
        Endereco endereco = new Endereco(
            null, "Rua A", "123", null, "Bairro X", "São Paulo", "SP", "01234567"
        );

        assertNotNull(endereco.getId());
    }

    @Test
    void shouldThrowExceptionWhenLogradouroIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Endereco(null, "", "123", null, "Bairro X", "São Paulo", "SP", "01234567")
        );
    }

    @Test
    void shouldThrowExceptionWhenCidadeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Endereco(null, "Rua A", "123", null, "Bairro X", "", "SP", "01234567")
        );
    }

    @Test
    void shouldThrowExceptionWhenEstadoIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Endereco(null, "Rua A", "123", null, "Bairro X", "São Paulo", " ", "01234567")
        );
    }

    @Test
    void shouldThrowExceptionWhenCepIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Endereco(null, "Rua A", "123", null, "Bairro X", "São Paulo", "SP", "123")
        );
    }
}
