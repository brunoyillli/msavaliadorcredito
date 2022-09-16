package io.github.brunoyillli.msavaliadorcredito.application;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.brunoyillli.msavaliadorcredito.application.exceptions.DadosClienteNotFoundException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.ErroComunicacaoMicroservicesException;
import io.github.brunoyillli.msavaliadorcredito.application.exceptions.ErroSolicitacaoCartaoException;
import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosAvaliacao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.ProtocoloSolicitacaoCartao;
import io.github.brunoyillli.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.brunoyillli.msavaliadorcredito.domain.model.SituacaoCliente;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("avaliacoes-credito")
@RequiredArgsConstructor
public class AvaliadorCreditoController {

	private final AvaliadorCreditoService avaliadorCreditoService;

	@GetMapping
	public String status() {
		return "ok";
	}

	@GetMapping(value = "situacao-cliente", params = "cpf")
	public ResponseEntity consultaSituacaoCliente(@RequestParam("cpf") String cpf) {
		try {
			SituacaoCliente situacaoCliente = avaliadorCreditoService.obterSituacaoCliente(cpf);
			return ResponseEntity.ok(situacaoCliente);
		} catch (DadosClienteNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (ErroComunicacaoMicroservicesException e) {
			return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity realizarAvalicao(@RequestBody DadosAvaliacao dados) {
		try {
			RetornoAvaliacaoCliente realizarAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dados.getCpf(),
					dados.getRenda());
			return ResponseEntity.ok(realizarAvaliacaoCliente);
		} catch (DadosClienteNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (ErroComunicacaoMicroservicesException e) {
			return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
		}
	}
	
	@PostMapping("solicitacoes-cartao")
	public ResponseEntity solicitarCartao(@RequestBody DadosSolicitacaoEmissaoCartao dados) {
		try {
			ProtocoloSolicitacaoCartao protocoloSolicitacaoCartao = avaliadorCreditoService.solicitarEmissaoCartao(dados);
			return ResponseEntity.ok(protocoloSolicitacaoCartao);
		}catch (ErroSolicitacaoCartaoException e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}
}
