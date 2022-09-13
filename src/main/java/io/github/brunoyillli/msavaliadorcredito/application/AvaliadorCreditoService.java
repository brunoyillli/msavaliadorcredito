package io.github.brunoyillli.msavaliadorcredito.application;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.brunoyillli.msavaliadorcredito.infra.clients.ClienteResourceClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

	private final ClienteResourceClient clienteResourceClient;
	
	public SituacaoCliente obterSituacaoCliente(String cpf) {
		ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
		return SituacaoCliente.builder().cliente(dadosClienteResponse.getBody()).build();
	}

}
