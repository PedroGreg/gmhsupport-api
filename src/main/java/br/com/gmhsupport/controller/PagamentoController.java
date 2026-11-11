package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.PagamentoRequestDTO;
import br.com.gmhsupport.dto.response.PagamentoResponseDTO;
import br.com.gmhsupport.services.PagamentoService;
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

@Tag(name = "Pagamentos")
@RestController
@RequestMapping("/pagamento")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar o pagamento de um chamado (dono do chamado ou técnico responsável)")
    @ApiResponse(responseCode = "200", description = "Cobrança encontrada")
    @ApiResponse(responseCode = "404", description = "O chamado não possui cobrança", content = @Content)
    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> buscarPorChamado(
            @PathVariable Long chamadoId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.buscarPorChamado(chamadoId, authentication.getName())));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Trocar a forma de um pagamento ainda não pago (simulado). Pix: gera novo código (aguardando_pix). Crédito/Débito: usa um cartão salvo (cartaoId) e aprova na hora")
    @ApiResponse(responseCode = "200", description = "Pagamento processado")
    @ApiResponse(responseCode = "404", description = "O chamado não possui cobrança", content = @Content)
    @ApiResponse(responseCode = "409", description = "Chamado já pago", content = @Content)
    @PostMapping("/chamado/{chamadoId}/pagar")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> pagar(
            @PathVariable Long chamadoId,
            @RequestBody @Valid PagamentoRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.pagar(chamadoId, authentication.getName(), dto)));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Confirmar o Pix (simula o aviso do banco de que o Pix foi recebido)")
    @ApiResponse(responseCode = "200", description = "Pix confirmado, pagamento concluído")
    @ApiResponse(responseCode = "409", description = "Pix não gerado ou chamado já pago", content = @Content)
    @PostMapping("/chamado/{chamadoId}/confirmar-pix")
    public ResponseEntity<EntityModel<PagamentoResponseDTO>> confirmarPix(
            @PathVariable Long chamadoId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(EntityModel.of(service.confirmarPix(chamadoId, authentication.getName())));
    }
}
