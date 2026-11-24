package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.*;
import br.com.gmhsupport.dto.response.*;
import br.com.gmhsupport.model.DocumentoTecnico;
import br.com.gmhsupport.model.StatusCadastro;
import br.com.gmhsupport.model.StatusRepasse;
import br.com.gmhsupport.services.AdminAuthService;
import br.com.gmhsupport.services.AdminService;
import br.com.gmhsupport.services.RepasseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Administrador")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminAuthService authService;
    private final AdminService service;
    private final RepasseService repasseService;

    public AdminController(AdminAuthService authService, AdminService service, RepasseService repasseService) {
        this.authService = authService;
        this.service = service;
        this.repasseService = repasseService;
    }

    // ===== Login em 2 etapas (públicos) =====

    @Operation(description = "Etapa 1: e-mail e senha. Devolve um token temporário (5 min) e, no primeiro acesso, o segredo do Authenticator")
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> login(@RequestBody @Valid AdminLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.etapaSenha(dto));
    }

    @Operation(description = "Etapa 2: código de 6 dígitos do Google Authenticator. Devolve o token de acesso (ROLE_ADMIN)")
    @PostMapping("/login/verificar")
    public ResponseEntity<TokenResponseDTO> verificar(@RequestBody @Valid AdminVerificarRequestDTO dto) {
        return ResponseEntity.ok(authService.etapaCodigo(dto));
    }

    // ===== Resumo =====

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Números do topo: técnicos pendentes, repasses a pagar")
    @GetMapping("/resumo")
    public ResponseEntity<AdminResumoResponseDTO> resumo(Authentication auth) {
        return ResponseEntity.ok(service.resumo(auth.getName()));
    }

    // ===== Técnicos =====

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Lista técnicos. status = pendente | aprovado | recusado (vazio = todos)")
    @GetMapping("/tecnicos")
    public ResponseEntity<List<TecnicoAdminResponseDTO>> tecnicos(@RequestParam(required = false) StatusCadastro status) {
        return ResponseEntity.ok(service.listarTecnicos(status));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/tecnicos/{id}")
    public ResponseEntity<TecnicoAdminResponseDTO> tecnico(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarTecnico(id));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Baixa a certidão de antecedentes criminais do técnico")
    @GetMapping("/tecnicos/{id}/documento")
    public ResponseEntity<byte[]> documento(@PathVariable Long id) {
        DocumentoTecnico doc = service.documento(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getTipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(doc.getNomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(doc.getConteudo());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/tecnicos/{id}/aprovar")
    public ResponseEntity<TecnicoAdminResponseDTO> aprovar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.aprovar(id, auth.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/tecnicos/{id}/recusar")
    public ResponseEntity<TecnicoAdminResponseDTO> recusar(@PathVariable Long id,
                                                           @RequestBody @Valid RecusarTecnicoRequestDTO dto,
                                                           Authentication auth) {
        return ResponseEntity.ok(service.recusar(id, dto.motivo(), auth.getName()));
    }

    // ===== Repasses =====

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Pagamentos com repasse ao técnico. status = liberado (a pagar) | pago")
    @GetMapping("/repasses")
    public ResponseEntity<List<RepasseResponseDTO>> repasses(@RequestParam(defaultValue = "liberado") StatusRepasse status) {
        return ResponseEntity.ok(repasseService.listar(status));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Marca como pago tudo o que estava liberado para o técnico (depois de fazer o Pix)")
    @PostMapping("/repasses/tecnico/{tecnicoId}/pagar")
    public ResponseEntity<RepassePagoResponseDTO> pagarRepasse(@PathVariable Long tecnicoId,
                                                               @RequestBody(required = false) @Valid PagarRepasseRequestDTO dto,
                                                               Authentication auth) {
        return ResponseEntity.ok(repasseService.pagarTudoDoTecnico(tecnicoId,
                dto != null ? dto.observacao() : null, auth.getName()));
    }

    // ===== Preços =====

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Todos os problemas (subcategorias) com preço, inclusive os desativados")
    @GetMapping("/subcategorias")
    public ResponseEntity<List<SubcategoriaAdminResponseDTO>> subcategorias() {
        return ResponseEntity.ok(service.listarSubcategorias());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/subcategorias")
    public ResponseEntity<SubcategoriaAdminResponseDTO> criarSubcategoria(@RequestBody @Valid SubcategoriaAdminRequestDTO dto) {
        return new ResponseEntity<>(service.criarSubcategoria(dto), HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/subcategorias/{id}")
    public ResponseEntity<SubcategoriaAdminResponseDTO> atualizarSubcategoria(@PathVariable Long id,
                                                                              @RequestBody @Valid SubcategoriaAdminRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarSubcategoria(id, dto));
    }
}
