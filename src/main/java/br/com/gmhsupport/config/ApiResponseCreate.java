package br.com.gmhsupport.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entidade criada com sucesso, retornará a própria com o ID do banco"),
        @ApiResponse(responseCode = "400", description = "Houve algum erro nos dados enviados na requisição", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado, necessário estar logado e enviar o token de acesso, deverá vir no Header (Authorization: Bearer ..token..) e deverá ter a permissão correta", content = @Content)
})
public @interface ApiResponseCreate { ;
}
