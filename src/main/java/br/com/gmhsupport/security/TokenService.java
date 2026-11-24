package br.com.gmhsupport.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    private static final String EMISSOR = "gmhsupport-api";
    // Token "meio-login" do admin (senha certa, falta o código). Emissor diferente:
    // o SecurityFilter não aceita este token para nada além de /admin/login/verificar.
    private static final String EMISSOR_2FA = "gmhsupport-api-2fa";

    @Value("${api.security.token.secret:Aoba1223}")
    private String secret;

    public String gerarToken(String email, String role){
        try{
            return JWT.create()
                    .withIssuer(EMISSOR)
                    .withSubject(email)
                    .withClaim("role", role)
                    .withExpiresAt(Instant.now().plus(8, ChronoUnit.HOURS))
                    .sign(algoritmo());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    // Devolve o e-mail (subject) ou null se o token for inválido/vencido
    public String validarToken(String token){
        DecodedJWT jwt = decodificar(token);
        return jwt != null ? jwt.getSubject() : null;
    }

    // Token completo decodificado (para ler o "role") ou null se inválido/vencido
    public DecodedJWT decodificar(String token){
        try {
            return JWT.require(algoritmo()).withIssuer(EMISSOR).build().verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    // ----- Admin: etapa 1 do login (vale 5 minutos) -----
    public String gerarTokenTemporarioAdmin(String email){
        return JWT.create()
                .withIssuer(EMISSOR_2FA)
                .withSubject(email)
                .withClaim("etapa", "codigo")
                .withExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .sign(algoritmo());
    }

    public String validarTokenTemporarioAdmin(String token){
        try {
            return JWT.require(algoritmo()).withIssuer(EMISSOR_2FA).withClaim("etapa", "codigo")
                    .build().verify(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private Algorithm algoritmo(){
        return Algorithm.HMAC256(secret);
    }
}
