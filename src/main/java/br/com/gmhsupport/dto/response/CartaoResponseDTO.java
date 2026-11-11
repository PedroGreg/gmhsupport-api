package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Cartao;
import br.com.gmhsupport.model.TipoCartao;

public record CartaoResponseDTO(
        Long id,
        String apelido,
        TipoCartao tipo,
        String bandeira,
        String ultimosDigitos,
        String nomeTitular,
        Integer mesValidade,
        Integer anoValidade,
        Boolean principal
) {
    public CartaoResponseDTO(Cartao cartao) {
        this(
                cartao.getId(),
                cartao.getApelido(),
                cartao.getTipo(),
                cartao.getBandeira(),
                cartao.getUltimosDigitos(),
                cartao.getNomeTitular(),
                cartao.getMesValidade(),
                cartao.getAnoValidade(),
                cartao.getPrincipal()
        );
    }
}
