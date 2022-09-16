package io.github.brunoyillli.msavaliadorcredito.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException.FeignClientException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.DadosClienteNotFoundException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.ErroComunicacaoMicroservicesException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.ErroSolicitacaoCartaoException;
import io.github.brunoyillli.msavaliadorcredito.domain.model.Cartao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.CartaoAprovado;
import io.github.brunoyillli.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.ProtocoloSolicitacaoCartao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.brunoyillli.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.brunoyillli.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.brunoyillli.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

	private final ClienteResourceClient clienteResourceClient;
	private final CartoesResourceClient cartoesResourceClient;
	private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

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

	public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda)
			throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
		try {
			ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
			ResponseEntity<List<Cartao>> cartoesResponse = cartoesResourceClient.getCartoesRendaAteh(renda);

			List<Cartao> cartoes = cartoesResponse.getBody();
			List<CartaoAprovado> listaCartoesAprovados = cartoes.stream().map(cartao -> {
				DadosCliente dadosCliente = dadosClienteResponse.getBody();
				BigDecimal limiteBasico = cartao.getLimiteBasico();
				BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());

				BigDecimal fator = idadeBD.divide(BigDecimal.valueOf(10));
				BigDecimal limiteAprovado = fator.multiply(limiteBasico);

				CartaoAprovado aprovado = new CartaoAprovado();
				aprovado.setCartao(cartao.getNome());
				aprovado.setBandeira(cartao.getBandeira());
				aprovado.setLimiteAprovado(limiteAprovado);
				return aprovado;
			}).collect(Collectors.toList());
			return new RetornoAvaliacaoCliente(listaCartoesAprovados);
		} catch (FeignClientException e) {
			int status = e.status();
			if (HttpStatus.NOT_FOUND.value() == status) {
				throw new DadosClienteNotFoundException();
			}
			throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
		}
	}
	
	public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaoCartao dados) {
		try {
			emissaoCartaoPublisher.solicitarCartao(dados);
			String protocolo = UUID.randomUUID().toString();
			return new ProtocoloSolicitacaoCartao(protocolo);
		}catch (Exception e) {
			throw new ErroSolicitacaoCartaoException(e.getMessage());
		}
	}
}
