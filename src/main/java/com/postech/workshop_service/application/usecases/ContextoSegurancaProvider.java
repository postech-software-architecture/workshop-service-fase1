package com.postech.workshop_service.application.usecases;

import java.util.Optional;

public interface ContextoSegurancaProvider {

	Optional<IdentidadeAutenticada> identidadeAtual();

}
