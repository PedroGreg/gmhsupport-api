package br.com.gmhsupport.controller;

import br.com.gmhsupport.config.*;
import br.com.gmhsupport.dto.request.ChamadoCreateRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoFinalizarRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoMensagemRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoRequestDTO;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.dto.response.HistoricoChamadoResponseDTO;
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

    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Criar um novo chamado já com o pagamento (formaPagamento: pix, credito ou debito; cartaoId obrigatório no cartão). Valor = valor base da categoria + acréscimo da prioridade (baixa 0%, media 20%, alta 50%, urgente 100%)")
    @ApiResponseCreate
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

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar chamado por id")
    @ApiResponseFindById
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> findById(@PathVariable Long id, Authentication authentication) {
        ChamadoResponseDTO findChamado = service.findByIdComAcesso(id, authentication.getName());
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(findChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Buscar todos chamados")
    @ApiResponseFindAll
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

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar chamado com o id informado")
    @ApiResponseFindById
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> update(
            @RequestBody @Valid ChamadoRequestDTO dto,
            @PathVariable Long id
    ) {
        ChamadoResponseDTO updateChamado = service.update(id, dto);
        EntityModel<ChamadoResponseDTO> entityChamado = EntityModel.of(updateChamado);
        return new ResponseEntity<>(entityChamado, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Deletar chamado")
    @ApiResponseDelete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Cancelar chamado")
    @ApiResponseFindById
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> cancelarChamado(
            @PathVariable Long id,
            Authentication authentication
    ){
        ChamadoResponseDTO chamadoCancelado = service.cancelarChamado(authentication.getName(), id);
        EntityModel<ChamadoResponseDTO> entityModel = EntityModel.of(chamadoCancelado);
        return new ResponseEntity<>(entityModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USUARIO') or hasAuthority('ROLE_TECNICO')")
    @Operation(description = "Adicionar mensagem/evolução ao histórico do chamado")
    @ApiResponseCreate
    @PostMapping("/{id}/mensagem")
    public ResponseEntity<EntityModel<HistoricoChamadoResponseDTO>> adicionarMensagem(
            @PathVariable Long id,
            @RequestBody @Valid ChamadoMensagemRequestDTO dto,
            Authentication authentication
    ){
        HistoricoChamadoResponseDTO historicoDTO = service.adicionarMensagem(id, authentication.getName(), dto);
        EntityModel<HistoricoChamadoResponseDTO> entityModel = EntityModel.of(historicoDTO);
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_TECNICO') or hasAuthority('ROLE_ADMIN')")
    @Operation(description = "Atualizar status para aguardando usuario")
    @ApiResponseUpdate
    @PostMapping("/{id}/aguardar")
    public ResponseEntity<EntityModel<ChamadoResponseDTO>> aguardandoUsuario(
            @PathVariable Long id,
            Authentication authentication
    ){
        ChamadoResponseDTO chamadoAtualizado = service.aguardandoUsuario(id, authentication.getName());
        EntityModel<ChamadoResponseDTO> entityModel = EntityModel.of(chamadoAtualizado);
        return new ResponseEntity<>(entityModel, HttpStatus.OK);
    }
    // ======== //
    @PreAuthorize("hasAuthority('ROLE_USUARIO')")
    @Operation(description = "Buscar meus chamados")
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todos os seus chamados, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todos os chamados sem tecnicos atribuidos, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todos os chamados com técnicos atribuidos, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Retornada uma lista com todos os chamados atribuidos para o técnico logado, se estiver vazia retornará apenas os dados do pageable")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Entidade atualizada com sucesso, retornará a própria entidade atualizada")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
    @ApiResponse(responseCode = "404", description = "Não foi encontrado nenhuma entidade com esse ID para atualizar, verificar ID", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Entidade atualizada com sucesso, retornará a própria entidade atualizada")
    @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
    @ApiResponse(responseCode = "404", description = "Não foi encontrado nenhuma entidade com esse ID para atualizar, verificar ID", content = @Content)
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