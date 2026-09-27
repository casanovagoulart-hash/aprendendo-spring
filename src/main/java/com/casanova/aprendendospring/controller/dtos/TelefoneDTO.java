package com.casanova.aprendendospring.controller.dtos;

import com.casanova.aprendendospring.infrastructure.entity.Telefone;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefoneDTO {
    private Long id;
    private String numero;

    public TelefoneDTO(Telefone telefone) {
        this.id = telefone.getId();
        this.numero = telefone.getNumero();
    }
}