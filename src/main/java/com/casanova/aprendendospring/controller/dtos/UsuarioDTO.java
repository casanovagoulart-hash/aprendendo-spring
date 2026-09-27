/* DTO: Data Transfer Object: é um padrão de software utilizado para transferir dados entre diferentes camadas ou
subsistemas de uma aplicação. Ele é amplamente empregado em arquiteturas distribuídas, como sistemas baseados em
APIs REST, gRPC ou sistemas de mensageria, para encapsular e transportar informações de forma eficiente e organizada.
O DTO é uma classe simples e anêmica, ou seja, contém apenas atributos e métodos básicos, como getters e setters,
sem lógica de negócio. Sua principal função é garantir que os dados sejam transferidos no formato necessário
para o consumo por outras camadas ou serviços, sem expor diretamente as entidades do domínio.*/

package com.casanova.aprendendospring.controller.dtos;

import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import lombok.*;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private String email;
    private String senha;

    // Construtor que recebe a entidade
    public UsuarioDTO(Usuario usuario) {
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
    }
}
