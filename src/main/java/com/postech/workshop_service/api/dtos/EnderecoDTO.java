package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de endereço")
public class EnderecoDTO {

	@Schema(example = "Avenida Paulista")
	private String logradouro;

	@Schema(example = "1000")
	private String numero;

	@Schema(example = "Apto 101")
	private String complemento;

	@Schema(example = "Bela Vista")
	private String bairro;

	@Schema(example = "São Paulo")
	private String cidade;

	@Schema(example = "SP")
	private String estado;

	@Schema(example = "01310-100")
	private String cep;

}
