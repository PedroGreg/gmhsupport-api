package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.AvaliacaoRequestDTO;
import br.com.gmhsupport.dto.response.AvaliacaoResponseDTO;
import br.com.gmhsupport.model.Avaliacao;
import br.com.gmhsupport.services.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@Tag(name = "Avaliacoes")
@RestController
@RequestMapping("/avaliacao")
@SecurityRequirement(name = "bearerAuth")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Criar avaliação para um chamado finalizado")
    @ApiResponseCreate
    @PostMapping
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> create(
            @RequestBody @Valid AvaliacaoRequestDTO dto,
            Authentication authentication
    ) {
        AvaliacaoResponseDTO responseDTO = service.create(authentication.getName(), dto);
        EntityModel<AvaliacaoResponseDTO> entityModel = EntityModel.of(responseDTO);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar avaliação por ID do chamado")
    @ApiResponseFindById
    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> buscarPorChamadoId(
            @PathVariable Long chamadoId
    ) {
        AvaliacaoResponseDTO responseDTO = service.buscarPorChamadoId(chamadoId);
        EntityModel<AvaliacaoResponseDTO> entityModel = EntityModel.of(responseDTO);
        return ResponseEntity.ok(entityModel);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos avaliacoes")
    @ApiResponseFindAll
    @GetMapping()
    public ResponseEntity<PagedModel<AvaliacaoResponseDTO>> findAll(
            @ParameterObject @PageableDefault() Pageable pageable
    ){
        Page<AvaliacaoResponseDTO> pageAvaliacao = service.findAll(pageable);
        PagedModel<AvaliacaoResponseDTO> pagedAvaliacao = PagedModel.of(pageAvaliacao.stream().toList(),
                new PagedModel.PageMetadata(pageAvaliacao.getSize(), pageAvaliacao.getNumber(), pageAvaliacao.getTotalElements(), pageAvaliacao.getTotalPages()));
        return new ResponseEntity<>(pagedAvaliacao, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar avaliacao por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> findById(
            @PathVariable Long id
    ){
        AvaliacaoResponseDTO findAvaliacao = service.findById(id);
        EntityModel<AvaliacaoResponseDTO> entityAvaliacao = EntityModel.of(findAvaliacao);
        return new ResponseEntity<>(entityAvaliacao, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar avaliacao")
    @ApiResponseUpdate
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> update(
            @RequestBody @Valid AvaliacaoRequestDTO dto,
            @PathVariable Long id){
        AvaliacaoResponseDTO updateAvaliacao = service.update(id, dto);
        EntityModel<AvaliacaoResponseDTO> entityAvaliacao = EntityModel.of(updateAvaliacao);
        return new ResponseEntity<>(entityAvaliacao, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar avaliacao")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
