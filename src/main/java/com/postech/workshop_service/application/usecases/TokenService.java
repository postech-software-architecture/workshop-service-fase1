package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Usuario;

import java.time.LocalDateTime;

public interface TokenService {

	String gerarAccessToken(Usuario usuario);

	String gerarRefreshToken();

	long getExpiracaoAccessSegundos();

	LocalDateTime calcularExpiracaoRefreshToken();

}
