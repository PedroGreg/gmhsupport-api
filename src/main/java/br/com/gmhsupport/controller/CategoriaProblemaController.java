package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.CategoriaProblemaRequestDTO;
import br.com.gmhsupport.dto.response.CategoriaProblemaResponseDTO;
import br.com.gmhsupport.services.CategoriaProblemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categoria Problema")
@RestController
@RequestMapping("/categoriaProblema")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaProblemaController {

    private final CategoriaProblemaService service;

    public CategoriaProblemaController(CategoriaProblemaService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Criar um novo categoriaProblema")
    @ApiResponseCreate
    @PostMapping("")
    public ResponseEntity<EntityModel<CategoriaProblemaResponseDTO>> create(
            @RequestBody @Valid CategoriaProblemaRequestDTO dto
    ){
        CategoriaProblemaResponseDTO createCategoriaProblema = service.create(dto);
        EntityModel<CategoriaProblemaResponseDTO> entityModel = EntityModel.of(createCategoriaProblema);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos categoriaProblema")
    @ApiResponseFindAll
    @GetMapping()
    public ResponseEntity<PagedModel<CategoriaProblemaResponseDTO>> findAll(
            @ParameterObject @PageableDefault() Pageable pageable
    ){
        Page<CategoriaProblemaResponseDTO> pageCategoriaProblema = service.findAll(pageable);
        PagedModel<CategoriaProblemaResponseDTO> pagedCategoriaProblema = PagedModel.of(pageCategoriaProblema.stream().toList(),
                new PagedModel.PageMetadata(pageCategoriaProblema.getSize(), pageCategoriaProblema.getNumber(), pageCategoriaProblema.getTotalElements(), pageCategoriaProblema.getTotalPages()));
        return new ResponseEntity<>(pagedCategoriaProblema, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar categoriaProblema por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CategoriaProblemaResponseDTO>> findById(
            @PathVariable Long id
    ){
        CategoriaProblemaResponseDTO findCategoriaProblema = service.findById(id);
        EntityModel<CategoriaProblemaResponseDTO> entityCategoriaProblema = EntityModel.of(findCategoriaProblema);
        return new ResponseEntity<>(entityCategoriaProblema, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar categoriaProblema")
    @ApiResponseUpdate
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CategoriaProblemaResponseDTO>> update(
            @RequestBody @Valid CategoriaProblemaRequestDTO dto,
            @PathVariable Long id){
        CategoriaProblemaResponseDTO updateCategoriaProblema = service.update(id, dto);
        EntityModel<CategoriaProblemaResponseDTO> entityCategoriaProblema = EntityModel.of(updateCategoriaProblema);
        return new ResponseEntity<>(entityCategoriaProblema, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar categoriaProblema")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
