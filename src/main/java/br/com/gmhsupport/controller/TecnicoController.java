package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.TecnicoRequestDTO;
import br.com.gmhsupport.dto.response.TecnicoEstatisticasResponseDTO;
import br.com.gmhsupport.dto.response.TecnicoResponseDTO;
import br.com.gmhsupport.dto.response.UsuarioResponseDTO;
import br.com.gmhsupport.services.TecnicoService;
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

@Tag(name = "Tecnicos")
@RestController
@RequestMapping("/tecnico")
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Criar um novo tecnico")
    @ApiResponseCreate
    @PostMapping("")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> create(@RequestBody @Valid TecnicoRequestDTO dto){
        TecnicoResponseDTO createTecnico = service.create(dto);
        EntityModel<TecnicoResponseDTO> entityModel = EntityModel.of(createTecnico);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar perfil do tecnico logado")
    @ApiResponse()
    @GetMapping("/me")
    public ResponseEntity<TecnicoResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(service.findByEmailExato(authentication.getName()));
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar estatísticas do perfil do técnico logado")
    @ApiResponse
    @GetMapping("/me/estatisticas")
    public ResponseEntity<TecnicoEstatisticasResponseDTO> obterEstatisticas(Authentication authentication) {
        TecnicoEstatisticasResponseDTO estatisticas = service.obterEstatisticas(authentication.getName());
        return ResponseEntity.ok(estatisticas);
    }


    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos tecnicos")
    @ApiResponseFindAll
    @GetMapping()
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findAll(@ParameterObject @PageableDefault() Pageable pageable){
        Page<TecnicoResponseDTO> pageTecnico = service.findAll(pageable);
        PagedModel<TecnicoResponseDTO> pagedTecnico = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedTecnico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar tecnico por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> findById(@PathVariable Long id){
        TecnicoResponseDTO findTecnico = service.findById(id);
        EntityModel<TecnicoResponseDTO> entityTecnico = EntityModel.of(findTecnico);
        return new ResponseEntity<>(entityTecnico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar tecnicos por nome")
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todas as entidades salvas por nome, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
    @GetMapping("/search")
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findByName(
            @RequestParam @Valid @DefaultValue(value = "") String nome,
            @ParameterObject @PageableDefault Pageable pageable
    ){
        Page<TecnicoResponseDTO> pageTecnico = service.findByNome(nome, pageable);
        PagedModel<TecnicoResponseDTO> pagedModel = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar tecnicos por email")
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todas as entidades salvas por nome, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
    @GetMapping("/search-email")
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findByEmail(@RequestParam @Valid @DefaultValue(value = "") String email, @ParameterObject @PageableDefault Pageable pageable){
        Page<TecnicoResponseDTO> pageTecnico = service.findByEmail(email, pageable);
        PagedModel<TecnicoResponseDTO> pagedModel = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar tecnico")
    @ApiResponseUpdate
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> update(@RequestBody @Valid TecnicoRequestDTO dto, @PathVariable Long id){
        TecnicoResponseDTO updateTecnico = service.update(dto, id);
        EntityModel<TecnicoResponseDTO> entityTecnico = EntityModel.of(updateTecnico);
        return new ResponseEntity<>(entityTecnico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar tecnico")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}