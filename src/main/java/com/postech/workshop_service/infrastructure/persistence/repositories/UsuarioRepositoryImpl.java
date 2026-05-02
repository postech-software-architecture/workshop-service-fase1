package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.UsuarioMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA do repositorio de usuarios autenticaveis.
 */
@Component
@Transactional
public class UsuarioRepositoryImpl implements UsuarioRepository {

	private final JpaUsuarioRepository jpaUsuarioRepository;

	private final UsuarioMapper usuarioMapper;

	private final EntityManager entityManager;

	public UsuarioRepositoryImpl(JpaUsuarioRepository jpaUsuarioRepository, UsuarioMapper usuarioMapper,
			EntityManager entityManager) {
		this.jpaUsuarioRepository = jpaUsuarioRepository;
		this.usuarioMapper = usuarioMapper;
		this.entityManager = entityManager;
	}

	@Override
	public Usuario salvar(Usuario usuario) {
		UsuarioJpaEntity entity = usuario.getId() != null
				? jpaUsuarioRepository.findById(usuario.getId()).map(existing -> {
					usuarioMapper.updateEntityFromDomain(usuario, existing);
					return existing;
				}).orElseGet(() -> usuarioMapper.toEntity(usuario)) : usuarioMapper.toEntity(usuario);

		entity.setCliente(obterClienteReferencia(usuario.getClienteId()));
		UsuarioJpaEntity salvo = jpaUsuarioRepository.save(entity);
		return usuarioMapper.toDomain(salvo);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorUsernameOuEmail(String identificador) {
		return jpaUsuarioRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identificador, identificador)
			.map(usuarioMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorId(UUID id) {
		return jpaUsuarioRepository.findById(id).map(usuarioMapper::toDomain);
	}

	private ClienteJpaEntity obterClienteReferencia(UUID clienteId) {
		if (clienteId == null) {
			return null;
		}
		return entityManager.getReference(ClienteJpaEntity.class, clienteId);
	}

}
