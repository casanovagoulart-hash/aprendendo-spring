package com.casanova.aprendendospring.controller.dtos;

import com.casanova.aprendendospring.infrastructure.entity.Endereco;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoDTO {
    private Long id;
    private String rua;
    private Integer numero;
    private String bairro;
    private String cidade;
    private String cep;

    public EnderecoDTO(Endereco endereco) {
        this.id = endereco.getId();
        this.rua = endereco.getRua();
        this.numero = endereco.getNumero();
        this.bairro = endereco.getBairro();
        this.cidade = endereco.getCidade();
        this.cep = endereco.getCep();
    }
}