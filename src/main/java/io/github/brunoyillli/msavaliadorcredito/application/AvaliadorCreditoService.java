package io.github.brunoyillli.msavaliadorcredito.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException.FeignClientException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.DadosClienteNotFoundException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.ErroComunicacaoMicroservicesException;
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

	public SituacaoCliente obterSituacaoCliente(String cpf)
			throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
		try {
			ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
			ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesResourceClient.getCartoesByCliente(cpf);

			return SituacaoCliente.builder().cliente(dadosClienteResponse.getBody()).cartoes(cartoesResponse.getBody())
					.build();
		} catch (FeignClientException e) {
			int status = e.status();
			if (HttpStatus.NOT_FOUND.value() == status) {
				throw new DadosClienteNotFoundException();
			}
			throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
		}
	}

}
