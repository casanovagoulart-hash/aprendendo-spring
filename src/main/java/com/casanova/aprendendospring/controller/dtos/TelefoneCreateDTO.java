package com.casanova.aprendendospring.controller.dtos;

import lombok.*;

/**
 * DTO de ENTRADA para um telefone, usado dentro de UsuarioCreateDTO.
 * Sem campo "id" pelo mesmo motivo dos demais DTOs de entrada.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefoneCreateDTO {
    private String numero;
}