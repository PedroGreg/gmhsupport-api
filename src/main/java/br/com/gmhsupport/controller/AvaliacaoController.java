package br.com.gmhsupport.controller;

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

    @Operation(description = "Criar um novo avaliacao")
    @ApiResponse(responseCode = "201", description = "Avaliacao criado, retornará o próprio avaliacao criado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @PostMapping("")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> create(
            @RequestBody @Valid AvaliacaoRequestDTO dto
    ){
        AvaliacaoResponseDTO createAvaliacao = service.create(dto);
        EntityModel<AvaliacaoResponseDTO> entityModel = EntityModel.of(createAvaliacao);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @Operation(description = "Buscar todos avaliacoes")
    @ApiResponse(responseCode = "200", description = "Lista com todos os avaliacoes cadastrados, caso nenhum, retornará apenas o pageable")
    @GetMapping()
    public ResponseEntity<PagedModel<AvaliacaoResponseDTO>> findAll(
            @ParameterObject @PageableDefault() Pageable pageable
    ){
        Page<AvaliacaoResponseDTO> pageAvaliacao = service.findAll(pageable);
        PagedModel<AvaliacaoResponseDTO> pagedAvaliacao = PagedModel.of(pageAvaliacao.stream().toList(),
                new PagedModel.PageMetadata(pageAvaliacao.getSize(), pageAvaliacao.getNumber(), pageAvaliacao.getTotalElements(), pageAvaliacao.getTotalPages()));
        return new ResponseEntity<>(pagedAvaliacao, HttpStatus.OK);
    }

    @Operation(description = "Buscar avaliacao por id")
    @ApiResponse(responseCode = "200", description = "Avaliacao encontrado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Avaliacao com o id informado não encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> findById(
            @PathVariable Long id
    ){
        AvaliacaoResponseDTO findAvaliacao = service.findById(id);
        EntityModel<AvaliacaoResponseDTO> entityAvaliacao = EntityModel.of(findAvaliacao);
        return new ResponseEntity<>(entityAvaliacao, HttpStatus.OK);
    }

    @Operation(description = "Atualizar avaliacao")
    @ApiResponse(responseCode = "200", description = "Avaliacao com o id informado atualizado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Avaliacao com o id informado não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> update(
            @RequestBody @Valid AvaliacaoRequestDTO dto,
            @PathVariable Long id){
        AvaliacaoResponseDTO updateAvaliacao = service.update(id, dto);
        EntityModel<AvaliacaoResponseDTO> entityAvaliacao = EntityModel.of(updateAvaliacao);
        return new ResponseEntity<>(entityAvaliacao, HttpStatus.OK);
    }

    @Operation(description = "Deletar avaliacao")
    @ApiResponse(responseCode = "204", description = "Avaliacao com o id informado deletado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Avaliacao com o id informado não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
