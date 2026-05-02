package com.postech.workshop_service.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Resposta da identidade autenticada corrente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAutenticadoResponse {

	private UUID id;

	private String username;

	private List<String> roles;

}
