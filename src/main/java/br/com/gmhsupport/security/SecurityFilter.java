package br.com.gmhsupport.security;

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

    public SecurityFilter(TokenService service, UsuarioRepository usuarioRepository, TecnicoRepository tecnicoRepository){
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recuperarToken(request);
        if(token != null){
            var email = service.validarToken(token);
            if (email != null){
                var usuario = usuarioRepository.findByEmailIgnoreCase(email);
                if(usuario != null && usuario.isPresent()){
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USUARIO"));
                    var authentication = new UsernamePasswordAuthenticationToken(usuario.get().getEmail(),null,authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else{
                    var tecnico = tecnicoRepository.findByEmailIgnoreCase(email);
                    if (tecnico != null && tecnico.isPresent()){
                        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_TECNICO"));
                        var authentication = new UsernamePasswordAuthenticationToken(tecnico.get().getEmail(), null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
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
