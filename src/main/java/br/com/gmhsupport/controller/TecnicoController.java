package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.TecnicoRequestDTO;
import br.com.gmhsupport.dto.response.TecnicoResponseDTO;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tecnicos")
@RestController
@RequestMapping("/tecnico")
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service){
        this.service = service;
    }

    @Operation(description = "Criar um novo tecnico")
    @ApiResponse(responseCode = "201", description = "Tecnico criado, retornará o próprio tecnico criado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @PostMapping("")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> create(@RequestBody @Valid TecnicoRequestDTO dto){
        TecnicoResponseDTO createTecnico = service.create(dto);
        EntityModel<TecnicoResponseDTO> entityModel = EntityModel.of(createTecnico);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @Operation(description = "Buscar todos tecnicos")
    @ApiResponse(responseCode = "200", description = "Lista com todos os tecnicos cadastrados, caso nenhum, retornará apenas o pageable")
    @GetMapping()
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findAll(@ParameterObject @PageableDefault() Pageable pageable){
        Page<TecnicoResponseDTO> pageTecnico = service.findAll(pageable);
        PagedModel<TecnicoResponseDTO> pagedTecnico = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedTecnico, HttpStatus.OK);
    }

    @Operation(description = "Buscar tecnico por id")
    @ApiResponse(responseCode = "200", description = "Tecnico encontrado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tecnico com o id informado não encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> findById(@PathVariable Long id){
        TecnicoResponseDTO findTecnico = service.findById(id);
        EntityModel<TecnicoResponseDTO> entityTecnico = EntityModel.of(findTecnico);
        return new ResponseEntity<>(entityTecnico, HttpStatus.OK);
    }

    @Operation(description = "Buscar tecnicos por nome")
    @ApiResponse(responseCode = "200", description = "Lista com todos os tecnicos cadastrados com o nome informado, caso nenhum, retornará apenas o pageable")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tecnico com o id informado não encontrado", content = @Content)
    @GetMapping("/search")
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findByName(@RequestParam @Valid @DefaultValue(value = "") String nome, @ParameterObject @PageableDefault Pageable pageable){
        Page<TecnicoResponseDTO> pageTecnico = service.findByNome(nome, pageable);
        PagedModel<TecnicoResponseDTO> pagedModel = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @Operation(description = "Buscar tecnicos por email")
    @ApiResponse(responseCode = "200", description = "Lista com todos os tecnicos cadastrados com o email informado, caso nenhum, retornará apenas o pageable")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tecnico com o id informado não encontrado", content = @Content)
    @GetMapping("/search-email")
    public ResponseEntity<PagedModel<TecnicoResponseDTO>> findByEmail(@RequestParam @Valid @DefaultValue(value = "") String email, @ParameterObject @PageableDefault Pageable pageable){
        Page<TecnicoResponseDTO> pageTecnico = service.findByEmail(email, pageable);
        PagedModel<TecnicoResponseDTO> pagedModel = PagedModel.of(pageTecnico.stream().toList(),
                new PagedModel.PageMetadata(pageTecnico.getSize(), pageTecnico.getNumber(), pageTecnico.getTotalElements(), pageTecnico.getTotalPages()));
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @Operation(description = "Atualizar tecnico")
    @ApiResponse(responseCode = "200", description = "Tecnico com o id informado atualizado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tecnico com o id informado não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TecnicoResponseDTO>> update(@RequestBody @Valid TecnicoRequestDTO dto, @PathVariable Long id){
        TecnicoResponseDTO updateTecnico = service.update(dto, id);
        EntityModel<TecnicoResponseDTO> entityTecnico = EntityModel.of(updateTecnico);
        return new ResponseEntity<>(entityTecnico, HttpStatus.OK);
    }

    @Operation(description = "Deletar tecnico")
    @ApiResponse(responseCode = "204", description = "Tecnico com o id informado deletado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tecnico com o id informado não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}