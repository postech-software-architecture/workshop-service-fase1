package com.postech.workshop_service.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private LocalDateTime timestamp;

	private int status;

	private String error;

	private String message;

	private String path;

	private List<FieldErrorDetail> fieldErrors;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FieldErrorDetail {

		private String field;

		private String message;

	}

}
