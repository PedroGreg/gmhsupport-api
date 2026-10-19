package br.com.gmhsupport.controller;

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

    @Operation(description = "Criar um novo status de chamado")
    @ApiResponse(responseCode = "201", description = "Status de chamado criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @PostMapping("")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> create(
            @RequestBody @Valid StatusChamadoRequestDTO dto
    ) {
        StatusChamadoResponseDTO createStatus = service.create(dto);
        EntityModel<StatusChamadoResponseDTO> entityModel = EntityModel.of(createStatus);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @Operation(description = "Buscar todos os status de chamado")
    @ApiResponse(responseCode = "200", description = "Lista com todos os status cadastrados, caso nenhum, retornará apenas o pageable")
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

    @Operation(description = "Buscar status de chamado por id")
    @ApiResponse(responseCode = "200", description = "Status de chamado encontrado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Status de chamado com o id informado não encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> findById(
            @PathVariable Long id
    ) {
        StatusChamadoResponseDTO findStatus = service.findById(id);
        EntityModel<StatusChamadoResponseDTO> entityStatus = EntityModel.of(findStatus);
        return new ResponseEntity<>(entityStatus, HttpStatus.OK);
    }

    @Operation(description = "Atualizar status de chamado")
    @ApiResponse(responseCode = "200", description = "Status de chamado com o id informado atualizado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Status de chamado com o id informado não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<StatusChamadoResponseDTO>> update(
            @RequestBody @Valid StatusChamadoRequestDTO dto,
            @PathVariable Long id
    ) {
        StatusChamadoResponseDTO updateStatus = service.update(id, dto);
        EntityModel<StatusChamadoResponseDTO> entityStatus = EntityModel.of(updateStatus);
        return new ResponseEntity<>(entityStatus, HttpStatus.OK);
    }

    @Operation(description = "Deletar status de chamado")
    @ApiResponse(responseCode = "204", description = "Status de chamado com o id informado deletado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Status de chamado com o id informado não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}