package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.AdicionalAceitarRequestDTO;
import br.com.gmhsupport.dto.request.AdicionalDecisaoRequestDTO;
import br.com.gmhsupport.dto.request.AdicionalRequestDTO;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.services.AdicionalService;
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

@Tag(name = "Adicional do chamado")
@RestController
@RequestMapping("/chamado")
@SecurityRequirement(name = "bearerAuth")
public class AdicionalController {

    private final AdicionalService service;

    public AdicionalController(AdicionalService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO')")
    @Operation(description = "Técnico pede um valor adicional (1 por chamado). O chamado vai para 'Aguardando aprovação do adicional'")
    @ApiResponse(responseCode = "200", description = "Pedido registrado")
    @ApiResponse(responseCode = "409", description = "Status não permite ou já houve pedido", content = @Content)
    @PostMapping("/atribuidos/{id}/adicional")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> solicitar(@PathVariable Long id,
                                                                     @RequestBody @Valid AdicionalRequestDTO dto,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(EntityModel.of(service.solicitar(id, authentication.getName(), dto)));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Cliente aceita o adicional e paga (cartão aprova na hora; Pix fica aguardando a confirmação)")
    @ApiResponse(responseCode = "200", description = "Adicional aceito")
    @ApiResponse(responseCode = "409", description = "Pedido já respondido", content = @Content)
    @PostMapping("/{id}/adicional/aceitar")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> aceitar(@PathVariable Long id,
                                                                   @RequestBody @Valid AdicionalAceitarRequestDTO dto,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(EntityModel.of(service.aceitar(id, authentication.getName(), dto)));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Cliente recusa o adicional. O técnico decide depois se continua ou cancela")
    @ApiResponse(responseCode = "200", description = "Adicional recusado")
    @PostMapping("/{id}/adicional/recusar")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> recusar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(EntityModel.of(service.recusar(id, authentication.getName())));
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO')")
    @Operation(description = "Depois da recusa: 'continuar' pelo valor original ou 'cancelar' (encerra e estorna tudo ao cliente)")
    @ApiResponse(responseCode = "200", description = "Decisão registrada")
    @PostMapping("/atribuidos/{id}/adicional/decisao")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> decidir(@PathVariable Long id,
                                                                   @RequestBody @Valid AdicionalDecisaoRequestDTO dto,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(EntityModel.of(service.decidir(id, authentication.getName(), dto)));
    }
}
