package com.casanova.aprendendospring.infrastructure.repository.impl;

import com.casanova.aprendendospring.infrastructure.entity.Usuario;
import org.springframework.stereotype.Repository;

/**
 * Implementação concreta de CrudRepositoryImpl para a entidade Usuario.
 *
 * Por que essa classe precisa existir?
 * Como CrudRepositoryImpl é genérica e abstrata, o Spring não consegue
 * instanciá-la sozinho (não saberia qual "T" usar). Esta subclasse
 * "fecha" os tipos genéricos (Usuario, Long) e informa ao construtor
 * da superclasse qual é a {@code Class<T>} concreta - isso é o que permite
 * o uso de entityManager.find(domainClass, id) lá na superclasse.
 *
 * {@code @Repository} marca essa classe como um componente gerenciado pelo Spring
 * (um {@code @Component} especializado para a camada de acesso a dados), permitindo
 * que ela seja injetada em outros lugares via {@code @Autowired} ou injeção de construtor.
 */
@Repository
public class UsuarioCrudRepositoryImplManual extends CrudRepositoryImpl<Usuario, Long> {

    public UsuarioCrudRepositoryImplManual() {
        // Aqui passamos explicitamente Usuario.class para a superclasse,
        // resolvendo o problema de "type erasure" citado no comentário
        // da classe pai: em tempo de execução, o Java não sabe mais que
        // T = Usuario, então precisamos informar isso manualmente.
        super(Usuario.class);
    }
}
