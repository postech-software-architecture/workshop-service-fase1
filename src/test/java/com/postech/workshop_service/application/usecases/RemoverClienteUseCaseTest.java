package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoverClienteUseCaseTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private RemoverClienteUseCase removerClienteUseCase;

    @Test
    void shouldRemoveCliente() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.buscarPorId(id)).thenReturn(Optional.of(mock(Cliente.class)));

        removerClienteUseCase.executar(id);

        verify(clienteRepository).remover(id);
    }

    @Test
    void shouldThrowExceptionWhenClienteNotFound() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            removerClienteUseCase.executar(id)
        );
    }
}
