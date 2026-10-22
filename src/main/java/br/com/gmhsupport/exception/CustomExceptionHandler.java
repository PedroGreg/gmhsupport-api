package br.com.gmhsupport.exception;

import br.com.gmhsupport.model.ExceptionCustom;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<ExceptionCustom> customNotFoundException(WebRequest request, Exception e){
        ExceptionCustom response = new ExceptionCustom(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionCustom> regraDeNegocio(WebRequest request, IllegalStateException e) {
        ExceptionCustom response = new ExceptionCustom(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionCustom> dadoDuplicado(WebRequest request, DataIntegrityViolationException e) {
        String mensagem = "Registro duplicado ou violação de integridade dos dados.";
        if(e.getRootCause() != null && e.getRootCause().getMessage().contains("email")){
            mensagem = "Este e-mail já está cadastrado.";
        }
        ExceptionCustom response = new ExceptionCustom(
                new Date(),
                mensagem,
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
