package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.PagamentoRequestDTO;
import br.com.gmhsupport.dto.response.OrcamentoResponseDTO;
import br.com.gmhsupport.dto.response.PagamentoResponseDTO;
import br.com.gmhsupport.model.Prioridade;
import br.com.gmhsupport.model.TipoPagamento;
import br.com.gmhsupport.services.PagamentoService;
import br.com.gmhsupport.services.PrecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// tipo = "principal" (pago na abertura, padrão) ou "adicional" (valor extra pedido pelo técnico)
@Tag(name = "Pagamentos")
@RestController
@RequestMapping("/pagamento")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PagamentoService service;
    private final PrecoService precoService;

    public PagamentoController(PagamentoService service, PrecoService precoService) {
        this.service = service;
        this.precoService = precoService;
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Orçamento antes de abrir o chamado: serviço (subcategoria + acréscimo da prioridade) + deslocamento por km a partir do SENAC Jurubatuba")
    @ApiResponse(responseCode = "200", description = "Orçamento calculado (veja dentroDaArea)")
    @ApiResponse(responseCode = "404", description = "Subcategoria não encontrada", content = @Content)
    @GetMapping("/orcamento")
    public ResponseEntity<OrcamentoResponseDTO> orcamento(
            @RequestParam Long subcategoriaId,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return ResponseEntity.ok(precoService.orcar(subcategoriaId, prioridade, latitude, longitude));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar um pagamento do chamado (dono do chamado ou técnico responsável)")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "O chamado não possui pagamento desse tipo", content = @Content)
    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> buscarPorChamado(
            @PathVariable Long chamadoId,
            @RequestParam(defaultValue = "principal") TipoPagamento tipo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.buscarPorChamado(chamadoId, authentication.getName(), tipo)));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Todos os pagamentos do chamado (principal e adicional)")
    @GetMapping("/chamado/{chamadoId}/todos")
    public ResponseEntity<List<PagamentoResponseDTO>> listar(@PathVariable Long chamadoId, Authentication authentication) {
        return ResponseEntity.ok(service.listarPorChamado(chamadoId, authentication.getName()));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Trocar a forma de um pagamento ainda não pago (simulado). Pix: gera novo código (aguardando_pix). Crédito/Débito: usa um cartão salvo (cartaoId) e aprova na hora")
    @ApiResponse(responseCode = "200", description = "Pagamento processado")
    @ApiResponse(responseCode = "404", description = "O chamado não possui pagamento desse tipo", content = @Content)
    @ApiResponse(responseCode = "409", description = "Já pago, estornado ou chamado encerrado", content = @Content)
    @PostMapping("/chamado/{chamadoId}/pagar")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> pagar(
            @PathVariable Long chamadoId,
            @RequestParam(defaultValue = "principal") TipoPagamento tipo,
            @RequestBody @Valid PagamentoRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.pagar(chamadoId, authentication.getName(), dto, tipo)));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Confirmar o Pix (simula o aviso do banco de que o Pix foi recebido)")
    @ApiResponse(responseCode = "200", description = "Pix confirmado, pagamento concluído")
    @ApiResponse(responseCode = "409", description = "Pix não gerado ou já pago", content = @Content)
    @PostMapping("/chamado/{chamadoId}/confirmar-pix")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> confirmarPix(
            @PathVariable Long chamadoId,
            @RequestParam(defaultValue = "principal") TipoPagamento tipo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.confirmarPix(chamadoId, authentication.getName(), tipo)));
    }
}
