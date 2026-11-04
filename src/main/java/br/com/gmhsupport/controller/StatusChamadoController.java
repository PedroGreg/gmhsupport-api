package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.StatusChamadoRequestDTO;
import br.com.gmhsupport.dto.response.StatusChamadoResponseDTO;
import br.com.gmhsupport.services.StatusChamadoService;
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

@Tag(name = "StatusChamados")
@RestController
@RequestMapping("/status-chamado")
@SecurityRequirement(name = "bearerAuth")
public class StatusChamadoController {

    private final StatusChamadoService service;

    public StatusChamadoController(StatusChamadoService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Criar um novo status de chamado")
    @ApiResponseCreate
    @PostMapping("")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> create(
            @RequestBody @Valid StatusChamadoRequestDTO dto
    ) {
        StatusChamadoResponseDTO createStatus = service.create(dto);
        EntityModel<StatusChamadoResponseDTO> entityModel = EntityModel.of(createStatus);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos os status de chamado")
    @ApiResponseFindAll
    @GetMapping()
    public ResponseEntity<PagedModel<StatusChamadoResponseDTO>> findAll(
            @ParameterObject @PageableDefault() Pageable pageable
    ) {
        Page<StatusChamadoResponseDTO> pageStatus = service.findAll(pageable);
        PagedModel<StatusChamadoResponseDTO> pagedStatus = PagedModel.of(
                pageStatus.stream().toList(),
                new PagedModel.PageMetadata(
                        pageStatus.getSize(),
                        pageStatus.getNumber(),
                        pageStatus.getTotalElements(),
                        pageStatus.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedStatus, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar status de chamado por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> findById(
            @PathVariable Long id
    ) {
        StatusChamadoResponseDTO findStatus = service.findById(id);
        EntityModel<StatusChamadoResponseDTO> entityStatus = EntityModel.of(findStatus);
        return new ResponseEntity<>(entityStatus, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar status de chamado")
    @ApiResponseUpdate
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> update(
            @RequestBody @Valid StatusChamadoRequestDTO dto,
            @PathVariable Long id
    ) {
        StatusChamadoResponseDTO updateStatus = service.update(id, dto);
        EntityModel<StatusChamadoResponseDTO> entityStatus = EntityModel.of(updateStatus);
        return new ResponseEntity<>(entityStatus, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar status de chamado")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}