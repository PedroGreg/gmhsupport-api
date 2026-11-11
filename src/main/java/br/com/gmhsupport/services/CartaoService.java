package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.CartaoRequestDTO;
import br.com.gmhsupport.dto.response.CartaoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Cartao;
import br.com.gmhsupport.model.Usuario;
import br.com.gmhsupport.repository.CartaoRepository;
import br.com.gmhsupport.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class CartaoService {

    private static final Logger logger = LoggerFactory.getLogger(CartaoService.class);
    private static final int LIMITE_CARTOES = 10;

    private final CartaoRepository cartaoRepository;
    private final UsuarioRepository usuarioRepository;

    public CartaoService(CartaoRepository cartaoRepository, UsuarioRepository usuarioRepository) {
        this.cartaoRepository = cartaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CartaoResponseDTO> meusCartoes(String email) {
        Usuario usuario = buscarUsuario(email);
        return cartaoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(usuario.getId())
                .stream().map(CartaoResponseDTO::new).toList();
    }

    @Transactional
    public CartaoResponseDTO cadastrar(String email, CartaoRequestDTO dto) {
        Usuario usuario = buscarUsuario(email);
        logger.info("Cadastrando cartão final {} para o usuário {}", dto.ultimosDigitos(), usuario.getId());

        if (YearMonth.of(dto.anoValidade(), dto.mesValidade()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("Este cartão está vencido.");
        }
        long quantidade = cartaoRepository.countByUsuarioId(usuario.getId());
        if (quantidade >= LIMITE_CARTOES) {
            throw new IllegalStateException("Limite de " + LIMITE_CARTOES + " cartões atingido. Remova um para cadastrar outro.");
        }

        // O primeiro cartão (ou um marcado como principal) vira o principal
        boolean principal = quantidade == 0 || Boolean.TRUE.equals(dto.principal());
        if (principal) desmarcarPrincipal(usuario.getId());

        Cartao cartao = new Cartao();
        cartao.setUsuario(usuario);
        cartao.setApelido(dto.apelido() != null && !dto.apelido().isBlank() ? dto.apelido().trim() : null);
        cartao.setTipo(dto.tipo());
        cartao.setBandeira(dto.bandeira());
        cartao.setUltimosDigitos(dto.ultimosDigitos());
        cartao.setNomeTitular(dto.nomeTitular().trim().toUpperCase());
        cartao.setMesValidade(dto.mesValidade());
        cartao.setAnoValidade(dto.anoValidade());
        cartao.setPrincipal(principal);

        return new CartaoResponseDTO(cartaoRepository.save(cartao));
    }

    @Transactional
    public CartaoResponseDTO tornarPrincipal(String email, Long id) {
        Usuario usuario = buscarUsuario(email);
        Cartao cartao = buscarDoUsuario(id, usuario.getId());
        desmarcarPrincipal(usuario.getId());
        cartao.setPrincipal(true);
        return new CartaoResponseDTO(cartaoRepository.save(cartao));
    }

    @Transactional
    public void remover(String email, Long id) {
        Usuario usuario = buscarUsuario(email);
        Cartao cartao = buscarDoUsuario(id, usuario.getId());
        boolean eraPrincipal = Boolean.TRUE.equals(cartao.getPrincipal());
        cartaoRepository.delete(cartao);
        cartaoRepository.flush();

        // Se removeu o principal, o mais novo que sobrou vira principal
        if (eraPrincipal) {
            cartaoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(usuario.getId()).stream().findFirst()
                    .ifPresent(c -> {
                        c.setPrincipal(true);
                        cartaoRepository.save(c);
                    });
        }
    }

    // Usado pelo pagamento: garante que o cartão é do usuário e não está vencido
    @Transactional(readOnly = true)
    public Cartao buscarParaPagamento(Long id, Long usuarioId) {
        Cartao cartao = buscarDoUsuario(id, usuarioId);
        if (YearMonth.of(cartao.getAnoValidade(), cartao.getMesValidade()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("O cartão final " + cartao.getUltimosDigitos() + " está vencido.");
        }
        return cartao;
    }

    private void desmarcarPrincipal(Long usuarioId) {
        cartaoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(usuarioId).forEach(c -> {
            if (Boolean.TRUE.equals(c.getPrincipal())) {
                c.setPrincipal(false);
                cartaoRepository.save(c);
            }
        });
    }

    private Cartao buscarDoUsuario(Long id, Long usuarioId) {
        return cartaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new CustomNotFoundException("Cartão não encontrado."));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado: " + email));
    }
}
