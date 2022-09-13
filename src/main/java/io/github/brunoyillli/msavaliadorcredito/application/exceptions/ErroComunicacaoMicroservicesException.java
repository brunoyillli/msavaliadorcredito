package io.github.brunoyillli.msavaliadorcredito.application.exceptions;

import lombok.Getter;

public class ErroComunicacaoMicroservicesException extends Exception{

	private static final long serialVersionUID = 4313218628495377058L;
	
	@Getter
	private Integer status;
	
	public ErroComunicacaoMicroservicesException(String message, Integer status) {
		super(message);
		this.status = status;
	}
	
}
