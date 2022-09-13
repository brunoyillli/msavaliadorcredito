package io.github.brunoyillli.msavaliadorcredito.application.exceptions;

public class DadosClienteNotFoundException extends Exception{

	private static final long serialVersionUID = -3155431898178192622L;

	public DadosClienteNotFoundException() {
		super("Dados do Cliente não encontrados para o CPF informado.");
	}
	
}
