package io.github.brunoyillli.msavaliadorcredito.application;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.github.brunoyillli.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.brunoyillli.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.brunoyillli.msavaliadorcredito.infra.clients.ClienteResourceClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

	private final ClienteResourceClient clienteResourceClient;
	private final CartoesResourceClient cartoesResourceClient;

	public SituacaoCliente obterSituacaoCliente(String cpf) {

		ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
		ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesResourceClient.getCartoesByCliente(cpf);

		return SituacaoCliente
				.builder()
				.cliente(dadosClienteResponse.getBody())
				.cartoes(cartoesResponse.getBody())
				.build();
	}

}
