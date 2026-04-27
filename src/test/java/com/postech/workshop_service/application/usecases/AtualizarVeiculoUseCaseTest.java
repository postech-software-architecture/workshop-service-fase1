package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarVeiculoUseCaseTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;

    @Test
    void shouldUpdateVeiculoWithoutChangingClientes() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        Veiculo veiculo = new Veiculo(veiculoId, new Placa("BRA1D23"), "Toyota", "Corolla", 2020, null,
                null, List.of(clienteId));

        when(veiculoRepository.buscarPorId(veiculoId, true)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.existePlacaAtiva("ABC1234", veiculoId)).thenReturn(false);
        when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo atualizado = atualizarVeiculoUseCase.executar(veiculoId, "ABC1234", "Ford", "Focus", 2019,
                "Azul", "Obs");

        assertEquals("ABC1234", atualizado.getPlaca().getValor());
        assertEquals("Ford", atualizado.getMarca());
        assertEquals("Focus", atualizado.getModelo());
        assertEquals(2019, atualizado.getAno());
        assertEquals("Azul", atualizado.getCor());
        assertEquals("Obs", atualizado.getObservacoes());
        assertEquals(List.of(clienteId), atualizado.getClientesVinculados().stream().toList());
    }

    @Test
    void shouldThrowWhenVeiculoDoesNotExist() {
        assertThrows(RecursoNaoEncontradoException.class, () -> atualizarVeiculoUseCase.executar(UUID.randomUUID(),
                "BRA1D23", "Toyota", "Corolla", 2020, null, null));
    }
}
