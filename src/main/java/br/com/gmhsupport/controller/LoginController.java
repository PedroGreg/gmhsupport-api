package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.LoginRequestDTO;
import br.com.gmhsupport.services.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Target;

@Tag(name = "Login")
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service){
        this.service = service;
    }
    @Operation(description = "Fazer login")
    @ApiResponse(responseCode = "200", description = "Se o login for efetuado corretamente retornará um LoginResponse")
    @ApiResponse(responseCode = "401", description = "Senha e/ou email incorretos.", content = @Content)
    @PostMapping()
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request){
        ResponseEntity<?> response = service.onLogin(request);
        return response;
    }
}