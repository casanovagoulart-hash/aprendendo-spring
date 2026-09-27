package com.casanova.aprendendospring.controller.dtos;

import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private List<EnderecoDTO> enderecos;
    private List<TelefoneDTO> telefones;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.enderecos = usuario.getEnderecos().stream()
                .map(EnderecoDTO::new)
                .toList();
        this.telefones = usuario.getTelefones().stream()
                .map(TelefoneDTO::new)
                .toList();
    }
}