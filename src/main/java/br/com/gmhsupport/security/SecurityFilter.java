package br.com.gmhsupport.security;

import br.com.gmhsupport.repository.AdministradorRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import br.com.gmhsupport.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService service;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final AdministradorRepository administradorRepository;

    public SecurityFilter(TokenService service, UsuarioRepository usuarioRepository, TecnicoRepository tecnicoRepository,
                          AdministradorRepository administradorRepository){
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.administradorRepository = administradorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recuperarToken(request);
        if(token != null){
            var jwt = service.decodificar(token);
            if (jwt != null && jwt.getSubject() != null){
                String email = jwt.getSubject();
                String role = jwt.getClaim("role").asString();
                String autoridade = null;

                if ("ROLE_ADMIN".equals(role)) {
                    // Só vale se o admin existir e não estiver bloqueado
                    var admin = administradorRepository.findByEmailIgnoreCase(email);
                    if (admin.isPresent() && !admin.get().isBloqueado()) autoridade = "ROLE_ADMIN";
                } else {
                    var usuario = usuarioRepository.findByEmailIgnoreCase(email);
                    if (usuario.isPresent()) {
                        autoridade = "ROLE_USUARIO";
                    } else {
                        var tecnico = tecnicoRepository.findByEmailIgnoreCase(email);
                        if (tecnico.isPresent()) {
                            // Lido do banco a cada requisição: quando o admin aprova, o mesmo token já passa a valer
                            autoridade = tecnico.get().isAprovado() ? "ROLE_TECNICO" : "ROLE_TECNICO_PENDENTE";
                        }
                    }
                }

                if (autoridade != null) {
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority(autoridade));
                    var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }else {
            return authHeader.replace("Bearer ", "");
        }
    }
}
