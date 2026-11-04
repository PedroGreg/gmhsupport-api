package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.response.HistoricoChamadoResponseDTO;
import br.com.gmhsupport.services.ChamadoService;
import br.com.gmhsupport.services.HistoricoChamadoService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Historicos de Chamados")
@RestController
@RequestMapping("/historico-chamado")
@SecurityRequirement(name = "bearerAuth")
public class HistoricoChamadoController {

    private final HistoricoChamadoService service;
    private final ChamadoService chamadoService;

    public HistoricoChamadoController(HistoricoChamadoService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Criar um novo histórico de chamado")
    @ApiResponseCreate
    @PostMapping("")
    public ResponseEntity<EntityModel<HistoricoChamadoResponseDTO>> create(
            @RequestBody @Valid HistoricoChamadoRequestDTO dto
    ) {
        HistoricoChamadoResponseDTO createHistorico = service.create(dto);
        EntityModel<HistoricoChamadoResponseDTO> entityModel = EntityModel.of(createHistorico);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos os históricos de chamados")
    @ApiResponseFindAll
    @GetMapping()
    public ResponseEntity<PagedModel<HistoricoChamadoResponseDTO>> findAll(
            @ParameterObject @PageableDefault() Pageable pageable
    ) {
        Page<HistoricoChamadoResponseDTO> pageHistorico = service.findAll(pageable);
        PagedModel<HistoricoChamadoResponseDTO> pagedHistorico = PagedModel.of(
                pageHistorico.stream().toList(),
                new PagedModel.PageMetadata(
                        pageHistorico.getSize(),
                        pageHistorico.getNumber(),
                        pageHistorico.getTotalElements(),
                        pageHistorico.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedHistorico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar histórico de chamado por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<HistoricoChamadoResponseDTO>> findById(
            @PathVariable Long id
    ) {
        HistoricoChamadoResponseDTO findHistorico = service.findById(id);
        EntityModel<HistoricoChamadoResponseDTO> entityHistorico = EntityModel.of(findHistorico);
        return new ResponseEntity<>(entityHistorico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar historico chamado pelo id do chamado")
    @ApiResponseFindById
    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<PagedModel<HistoricoChamadoResponseDTO>> findByChamado(
            @PathVariable Long chamadoId,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        chamadoService.validarAcesso(chamadoId, authentication.getName());
        Page<HistoricoChamadoResponseDTO> pageHistorico = service.findByChamado(chamadoId, pageable);
        PagedModel<HistoricoChamadoResponseDTO> pagedModel = PagedModel.of(
                pageHistorico.stream().toList(),
                new PagedModel.PageMetadata(pageHistorico.getSize(), pageHistorico.getNumber(), pageHistorico.getTotalElements(), pageHistorico.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar histórico de chamado")
    @ApiResponseUpdate
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<HistoricoChamadoResponseDTO>> update(
            @RequestBody @Valid HistoricoChamadoRequestDTO dto,
            @PathVariable Long id
    ) {
        HistoricoChamadoResponseDTO updateHistorico = service.update(id, dto);
        EntityModel<HistoricoChamadoResponseDTO> entityHistorico = EntityModel.of(updateHistorico);
        return new ResponseEntity<>(entityHistorico, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar histórico de chamado")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}