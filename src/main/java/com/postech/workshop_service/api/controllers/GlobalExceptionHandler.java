package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.ErrorResponse;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler centralizado para traducao de excecoes em respostas HTTP padronizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Trata erros de regra de negocio.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 400.
	 */
	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(RegraDeNegocioException ex,
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
	 * Trata erros estruturais de validacao no payload.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 422.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
			.getAllErrors()
			.stream()
			.map(error -> new ErrorResponse.FieldErrorDetail(((FieldError) error).getField(),
					error.getDefaultMessage()))
			.collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
			.error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
			.message("Erro de validacao estrutural no payload.")
			.path(request.getRequestURI())
			.fieldErrors(fieldErrors)
			.build();

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
	}

	/**
	 * Mantem compatibilidade com erros legados tratados como validacao estrutural.
	 * @param ex excecao capturada.
	 * @param request requisicao corrente.
	 * @return payload padronizado de erro HTTP 422.
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
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
