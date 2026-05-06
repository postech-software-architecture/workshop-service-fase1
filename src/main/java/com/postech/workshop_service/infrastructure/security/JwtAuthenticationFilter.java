package com.postech.workshop_service.infrastructure.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro responsavel por validar o JWT e popular o contexto autenticado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenService jwtTokenService;

	private final DetalhesUsuarioServiceImpl detalhesUsuarioService;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService, DetalhesUsuarioServiceImpl detalhesUsuarioService) {
		this.jwtTokenService = jwtTokenService;
		this.detalhesUsuarioService = detalhesUsuarioService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);
		try {
			UUID usuarioId = jwtTokenService.extrairUsuarioId(token);
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				UsuarioAutenticadoPrincipal principal = detalhesUsuarioService.carregarPorId(usuarioId);
				if (jwtTokenService.validarAccessToken(token, principal.getId()) && principal.isEnabled()
						&& principal.isAccountNonLocked()) {
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
							principal, null, principal.getAuthorities());
					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				}
				else {
					SecurityContextHolder.clearContext();
				}
			}
		}
		catch (JwtException | IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

}
