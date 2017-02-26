package edu.teco.smartlambda.rest.response;

import lombok.Getter;

public class ExceptionResponse {
	@Getter
	private final String message;
	
	public ExceptionResponse(final String message) {
		this.message = message;
	}
}
