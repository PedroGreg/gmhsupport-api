package br.com.gmhsupport.controller;

import br.com.gmhsupport.dto.request.LoginRequestDTO;
import br.com.gmhsupport.services.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service){
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request){
        ResponseEntity<?> response = service.onLogin(request);
        return response;
    }
}