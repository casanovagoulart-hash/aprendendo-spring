package com.casanova.aprendendospring.controller.dtos;

import lombok.*;

/**
 * DTO de ENTRADA para um endereço, usado dentro de UsuarioCreateDTO.
 * Sem campo "id" pelo mesmo motivo do UsuarioCreateDTO: quem cria o
 * registro é o banco, não o cliente da API.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoCreateDTO {
    private String rua;
    private Integer numero;
    private String bairro;
    private String cidade;
    private String cep;
}