package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.ErrorResponse;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler centralizado para traducao de excecoes em respostas HTTP padronizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Trata violacoes de regra de negocio.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 422.
	 */
	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(RegraDeNegocioException ex,
			HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
			.error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
	}

	/**
	 * Trata recursos nao encontrados.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 404.
	 */
	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(RecursoNaoEncontradoException ex,
			HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.NOT_FOUND.value())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	/**
	 * Trata erros de Bean Validation no payload de entrada.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
			.collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.message("Erro de validação estrutural no payload.")
			.path(request.getRequestURI())
			.fieldErrors(fieldErrors)
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Trata argumentos invalidos vindos do dominio (ex.: invariante violada na construcao
	 * de um value object).
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
			HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Trata payloads JSON malformados, valores incompativeis com o tipo declarado e enums
	 * invalidos recebidos pelo Jackson.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.message("Payload inválido ou mal formado.")
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Trata path/query parameters com tipo invalido (ex.: UUID malformado).
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.message("Parâmetro '" + ex.getName() + "' tem formato inválido.")
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Trata requisicoes para rotas que nao correspondem a nenhum endpoint mapeado (ex.:
	 * GET /api/v1/servicos/ sem id, ou path inexistente).
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 404.
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
			HttpServletRequest request) {

		String message = String.format("Recurso não encontrado: %s /%s. Verifique a URL informada.",
				request.getMethod(), ex.getResourcePath());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.NOT_FOUND.value())
			.error(HttpStatus.NOT_FOUND.getReasonPhrase())
			.message(message)
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	/**
	 * Trata requisicoes com metodo HTTP nao suportado pelo endpoint.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 405.
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {

		String supported = ex.getSupportedHttpMethods() == null ? "" : ex.getSupportedHttpMethods().toString();
		String message = String.format("Método %s não suportado neste recurso. Métodos permitidos: %s.", ex.getMethod(),
				supported);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.METHOD_NOT_ALLOWED.value())
			.error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
			.message(message)
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
	}

	/**
	 * Trata requisicoes onde um query parameter obrigatorio nao foi informado.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException ex,
			HttpServletRequest request) {

		String message = String.format("Parâmetro obrigatório '%s' ausente na requisição.", ex.getParameterName());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.BAD_REQUEST.value())
			.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
			.message(message)
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Trata falhas de acesso ao banco de dados (conexao, timeout, etc.).
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 503.
	 */
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.SERVICE_UNAVAILABLE.value())
			.error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
			.message("Serviço de banco de dados temporariamente indisponível. Tente novamente em alguns instantes.")
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
	}

	/**
	 * Trata erros nao previstos da aplicacao.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 500.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
			.message("Ocorreu um erro interno no servidor.")
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

}
