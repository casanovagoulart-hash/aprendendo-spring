package com.casanova.aprendendospring.controller.dtos;

import lombok.*;

import java.util.List;

/**
 * DTO de ENTRADA usado exclusivamente no cadastro de um novo usuário.
 *
 * Diferente de mapear @RequestBody direto para a entidade Usuario, este DTO
 * expõe apenas os campos que o cliente tem permissão de enviar. Não existe
 * campo "id" aqui de propósito: o ID é gerado pelo banco, nunca deve vir
 * do cliente (evita que alguém tente sobrescrever outro registro existente
 * enviando um id arbitrário - esse tipo de falha é chamado de "mass assignment").
 */
@Getter
@Setter
@ToString(exclude = "senha") // evita que a senha apareça em logs que usem toString()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCreateDTO {
    private String nome;
    private String email;
    private String senha; // chega em texto puro aqui; o hash acontece no Service, nunca no Controller/DTO
    private List<EnderecoCreateDTO> enderecos;
    private List<TelefoneCreateDTO> telefones;
}