package br.com.gmhsupport.dto.response;

public record TokenResponseDTO(String token, String tipo, String nome, String role) {

    public TokenResponseDTO(String token, String nome, String role){
        this(token, "Bearer", nome, role);
    }
}
