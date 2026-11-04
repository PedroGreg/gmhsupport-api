package br.com.gmhsupport.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entidade encontrada com sucesso, retornará a entidade"),
        @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content),
        @ApiResponse(responseCode = "404", description = "Não foi encontrado nenhuma entidade com esse ID para atualizar, verificar ID", content = @Content)
})
public @interface ApiResponseFindById {

}
