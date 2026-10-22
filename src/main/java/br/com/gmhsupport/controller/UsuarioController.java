package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.UsuarioAlterarSenhaRequestDTO;
import br.com.gmhsupport.dto.request.UsuarioRequestDTO;
import br.com.gmhsupport.dto.request.UsuarioUpdateMeRequestDTO;
import br.com.gmhsupport.dto.response.UsuarioResponseDTO;
import br.com.gmhsupport.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(description = "Criar um novo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario criado, retornará o próprio usuario criado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @PostMapping("")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> create(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO createUser = service.create(dto);
        EntityModel<UsuarioResponseDTO> entityModel = EntityModel.of(createUser);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(service.findByEmailExato(authentication.getName()));
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Atualizar dados do perfil do usuário logado (sem alterar senha)")
    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso")
    @PutMapping("/me")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> updateMe(
            @RequestBody @Valid UsuarioUpdateMeRequestDTO dto,
            Authentication authentication
    ) {
        UsuarioResponseDTO responseDTO = service.updateMe(authentication.getName(), dto);
        EntityModel<UsuarioResponseDTO> entityModel = EntityModel.of(responseDTO);
        return ResponseEntity.ok(entityModel);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Alterar senha do usuário logado")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Senha atual incorreta")
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestBody @Valid UsuarioAlterarSenhaRequestDTO dto,
            Authentication authentication
    ) {
        service.alterarSenha(authentication.getName(), dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos usuarios")
    @ApiResponse(responseCode = "200", description = "Lista com todos os usuarios cadastrados, caso nenhum, retornará apenas o pageable")
    @GetMapping()
    public ResponseEntity<PagedModel<UsuarioResponseDTO>> findAll(@ParameterObject @PageableDefault() Pageable pageable) {
        Page<UsuarioResponseDTO> pageUser = service.findAll(pageable);
        PagedModel<UsuarioResponseDTO> pagedUser = PagedModel.of(pageUser.stream().toList(),
                new PagedModel.PageMetadata(pageUser.getSize(), pageUser.getNumber(), pageUser.getTotalElements(), pageUser.getTotalPages()));
        return new ResponseEntity<>(pagedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar usuario por id")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario com o id informado não encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> findById(@PathVariable Long id) {
        UsuarioResponseDTO findUser = service.findById(id);
        EntityModel<UsuarioResponseDTO> entityUser = EntityModel.of(findUser);
        return new ResponseEntity<>(entityUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar usuarios por nome")
    @ApiResponse(responseCode = "200", description = "Lista com todos os usuarios cadastrados com o nome informado, caso nenhum, retornará apenas o pageable")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario com o id informado não encontrado", content = @Content)
    @GetMapping("/search")
    public ResponseEntity<PagedModel<UsuarioResponseDTO>> findByName(@RequestParam @Valid @DefaultValue(value = "") String nome, @ParameterObject @PageableDefault Pageable pageable) {
        Page<UsuarioResponseDTO> pageUser = service.findByNome(nome, pageable);
        PagedModel<UsuarioResponseDTO> pagedModel = PagedModel.of(pageUser.stream().toList(),
                new PagedModel.PageMetadata(pageUser.getSize(), pageUser.getNumber(), pageUser.getTotalElements(), pageUser.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar usuarios por email")
    @ApiResponse(responseCode = "200", description = "Lista com todos os usuarios cadastrados com o email informado, caso nenhum, retornará apenas o pageable")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario com o id informado não encontrado", content = @Content)
    @GetMapping("/search-email")
    public ResponseEntity<PagedModel<UsuarioResponseDTO>> findByEmail(@RequestParam @Valid @DefaultValue(value = "") String email, @ParameterObject @PageableDefault Pageable pageable) {
        Page<UsuarioResponseDTO> pageUser = service.findByEmail(email, pageable);
        PagedModel<UsuarioResponseDTO> pagedModel = PagedModel.of(pageUser.stream().toList(),
                new PagedModel.PageMetadata(pageUser.getSize(), pageUser.getNumber(), pageUser.getTotalElements(), pageUser.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar usuario")
    @ApiResponse(responseCode = "200", description = "Usuario com o id informado atualizado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario com o id informado não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> update(@RequestBody @Valid UsuarioRequestDTO dto, @PathVariable Long id) {
        UsuarioResponseDTO updateUser = service.update(dto, id);
        EntityModel<UsuarioResponseDTO> entityUser = EntityModel.of(updateUser);
        return new ResponseEntity<>(entityUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar usuario")
    @ApiResponse(responseCode = "204", description = "Usuario com o id informado deletado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario com o id informado não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}