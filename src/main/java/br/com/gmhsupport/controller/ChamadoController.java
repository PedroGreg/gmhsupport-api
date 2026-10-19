package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.ChamadoCreateRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoFinalizarRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoRequestDTO;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.services.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
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

@Tag(name = "Chamados")
@RestController
@RequestMapping("/chamado")
@SecurityRequirement(name = "bearerAuth")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @Operation(description = "Criar um novo chamado para o usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Chamado criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acesso negado / Token inválido", content = @Content)
    @PostMapping("")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> create(
            @RequestBody @Valid ChamadoCreateRequestDTO dto,
            Authentication authentication
    ) {
        String emailUsuarioLogado = authentication.getName();
        LoggerFactory.getLogger(ChamadoController.class).info(emailUsuarioLogado);
        ChamadoResponseDTO createChamado = service.create(dto, emailUsuarioLogado);
        EntityModel<ChamadoResponseDTO> entityModel = EntityModel.of(createChamado);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @Operation(description = "Buscar chamado por id")
    @ApiResponse(responseCode = "200", description = "Chamado encontrado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Chamado com o id informado não encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> findById(@PathVariable Long id) {
        ChamadoResponseDTO findChamado = service.findById(id);
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(findChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }

    @Operation(description = "Buscar todos chamados")
    @ApiResponse(responseCode = "200", description = "Lista com todos os chamados cadastrados, caso nenhum, retornará apenas o pageable")
    @GetMapping()
    public ResponseEntity<PagedModel<ChamadoResponseDTO>> findAll(@ParameterObject @PageableDefault() Pageable pageable) {
        Page<ChamadoResponseDTO> pageChamado = service.findAll(pageable);
        PagedModel<ChamadoResponseDTO> pagedChamado = PagedModel.of(
                pageChamado.stream().toList(),
                new PagedModel.PageMetadata(
                        pageChamado.getSize(),
                        pageChamado.getNumber(),
                        pageChamado.getTotalElements(),
                        pageChamado.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedChamado, HttpStatus.OK);
    }

    @Operation(description = "Atualizar chamado com o id informado")
    @ApiResponse(responseCode = "200", description = "Chamado com o id informado atualizado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Chamado com o id informado não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> update(
            @RequestBody @Valid ChamadoRequestDTO dto,
            @PathVariable Long id
    ) {
        ChamadoResponseDTO updateChamado = service.update(id, dto);
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(updateChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }

    @Operation(description = "Deletar chamado")
    @ApiResponse(responseCode = "204", description = "Chamado com o id informado deletado")
    @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content)
    @ApiResponse(responseCode = "404", description = "Chamado com o id informado não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    // ======== //
    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Buscar meus chamados")
    @ApiResponse(responseCode = "200", description = "Lista com todos os chamados cadastrados desse usuário")
    @GetMapping("/meus")
    public ResponseEntity<PagedModel<ChamadoResponseDTO>> findMeusChamados(
            @ParameterObject @PageableDefault() Pageable pageable,
            Authentication authentication
    ){
        Page<ChamadoResponseDTO> pageChamado = service.findMeusChamados(authentication.getName(), pageable);
        PagedModel<ChamadoResponseDTO> pagedChamado = PagedModel.of(
                pageChamado.stream().toList(),
                new PagedModel.PageMetadata(
                        pageChamado.getSize(),
                        pageChamado.getNumber(),
                        pageChamado.getTotalElements(),
                        pageChamado.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedChamado, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar chamados sem técnicos")
    @ApiResponse(responseCode = "200", description = "Lista com todos os chamados cadastrados sem técnico")
    @GetMapping("/fila")
    public ResponseEntity<PagedModel<ChamadoResponseDTO>> findChamadosAbertos(
            @ParameterObject @PageableDefault() Pageable pageable
    ){
        Page<ChamadoResponseDTO> pageChamado = service.findChamadosAbertos(pageable);
        PagedModel<ChamadoResponseDTO> pagedModel = PagedModel.of(
                pageChamado.stream().toList(),
                new PagedModel.PageMetadata(
                        pageChamado.getSize(),
                        pageChamado.getNumber(),
                        pageChamado.getTotalElements(),
                        pageChamado.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos chamados atribuidos")
    @ApiResponse(responseCode = "200", description = "Lista com todos os chamados atribuidos desse técnico")
    @GetMapping("/atribuidos/todos")
    public ResponseEntity<PagedModel<ChamadoResponseDTO>> findChamadosAtribuidos(
            @ParameterObject @PageableDefault() Pageable pageable
    ){
        Page<ChamadoResponseDTO> pageChamado = service.findChamadosAtribuidos(pageable);
        PagedModel<ChamadoResponseDTO> pagedModel = PagedModel.of(
                pageChamado.stream().toList(),
                new PagedModel.PageMetadata(
                        pageChamado.getSize(),
                        pageChamado.getNumber(),
                        pageChamado.getTotalElements(),
                        pageChamado.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar chamados atribuidos por técnico")
    @ApiResponse(responseCode = "200", description = "Lista com todos os chamados atribuidos desse técnico")
    @GetMapping("/atribuidos")
    public ResponseEntity<PagedModel<ChamadoResponseDTO>> findChamadosAtribuidos(
            @ParameterObject @PageableDefault() Pageable pageable,
            Authentication authentication
    ){
        Page<ChamadoResponseDTO> pageChamado = service.findChamadosAtribuidos(authentication.getName(), pageable);
        PagedModel<ChamadoResponseDTO> pagedModel = PagedModel.of(
                pageChamado.stream().toList(),
                new PagedModel.PageMetadata(
                        pageChamado.getSize(),
                        pageChamado.getNumber(),
                        pageChamado.getTotalElements(),
                        pageChamado.getTotalPages()
                )
        );
        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atribuir chamado ao técnico")
    @ApiResponse(responseCode = "200", description = "Chamado atribuido ao técnico")
    @ApiResponse(responseCode = "404", description = "Chamado ou Técnico não encontrado")
    @PostMapping("/atribuidos/{id}/assumir")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> assumirChamado(
            @PathVariable Long id,
            Authentication authentication
    ){
        ChamadoResponseDTO atribuidoChamado = service.assumirChamado(id, authentication.getName());
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(atribuidoChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Finalizar chamado")
    @ApiResponse(responseCode = "200", description = "Chamado finalizado com sucesso")
    @PostMapping("/atribuidos/{id}/finalizar")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> finalizarChamado(
            @PathVariable Long id,
            @RequestBody @Valid ChamadoFinalizarRequestDTO dto,
            Authentication authentication
    ) {
        ChamadoResponseDTO finalizadoChamado = service.finalizarChamado(id, authentication.getName(), dto);
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(finalizadoChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }
}