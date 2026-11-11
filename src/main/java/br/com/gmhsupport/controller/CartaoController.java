package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.CartaoRequestDTO;
import br.com.gmhsupport.dto.response.CartaoResponseDTO;
import br.com.gmhsupport.services.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cartões")
@RestController
@RequestMapping("/cartao")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_USUARIO')")
public class CartaoController {

    private final CartaoService service;

    public CartaoController(CartaoService service) {
        this.service = service;
    }

    @Operation(description = "Listar os cartões do usuário logado (principal primeiro)")
    @ApiResponse(responseCode = "200", description = "Lista de cartões (pode ser vazia)")
    @GetMapping
    public ResponseEntity<List<CartaoResponseDTO>> meusCartoes(Authentication authentication) {
        return ResponseEntity.ok(service.meusCartoes(authentication.getName()));
    }

    @Operation(description = "Cadastrar cartão. Envie só os 4 últimos dígitos: o número completo e o CVV nunca devem vir para a API")
    @ApiResponse(responseCode = "201", description = "Cartão cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    @ApiResponse(responseCode = "409", description = "Cartão vencido ou limite de cartões atingido", content = @Content)
    @PostMapping
    public ResponseEntity<CartaoResponseDTO> cadastrar(
            @RequestBody @Valid CartaoRequestDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(service.cadastrar(authentication.getName(), dto), HttpStatus.CREATED);
    }

    @Operation(description = "Marcar cartão como principal (vem selecionado ao abrir chamado)")
    @ApiResponse(responseCode = "200", description = "Cartão marcado como principal")
    @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    @PutMapping("/{id}/principal")
    public ResponseEntity<CartaoResponseDTO> tornarPrincipal(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.tornarPrincipal(authentication.getName(), id));
    }

    @Operation(description = "Remover cartão")
    @ApiResponse(responseCode = "204", description = "Cartão removido")
    @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, Authentication authentication) {
        service.remover(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
