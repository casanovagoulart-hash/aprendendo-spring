package com.casanova.aprendendospring.infrastructure.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação manual de CrudRepository usando EntityManager puro (JPA "cru"),
 * sem depender do Spring Data gerar a implementação automaticamente.
 *
 * OBJETIVO DIDÁTICO: mostrar o que o Spring Data faz "por baixo dos panos"
 * quando você simplesmente cria uma interface estendendo JpaRepository.
 *
 * <T>  -> tipo da entidade (ex: Usuario)
 * <ID> -> tipo do identificador da entidade (ex: Long)
 *
 * Como é genérica, ela sozinha não sabe qual entidade concreta manipular
 * em tempo de execução (o Java "apaga" o tipo genérico em runtime - isso
 * se chama "type erasure"). Por isso guardamos explicitamente a Class<T>
 * no construtor, para poder usar entityManager.find(domainClass, id).
 */
public abstract class CrudRepositoryImpl<T, ID> implements CrudRepository<T, ID> {

    // @PersistenceContext injeta o EntityManager gerenciado pelo Spring/JPA.
    // É através dele que conversamos diretamente com o banco (sem SQL manual).
    @PersistenceContext
    private EntityManager entityManager;

    // Guardamos a classe da entidade (ex: Usuario.class) porque em runtime
    // o Java não sabe mais qual é o "T" genérico - precisamos informar manualmente.
    private final Class<T> domainClass;

    protected CrudRepositoryImpl(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    @Override
    @Transactional // garante que a operação rode dentro de uma transação (commit/rollback automático)
    public <S extends T> S save(S entity) {
        // Extraímos o ID da entidade via reflection para decidir:
        // - se ainda não existe no banco -> INSERT (persist)
        // - se já existe -> UPDATE (merge)
        ID id = extractId(entity);
        if (id == null || entityManager.find(domainClass, id) == null) {
            entityManager.persist(entity); // equivale a um INSERT
            return entity;
        }
        return entityManager.merge(entity); // equivale a um UPDATE
    }

    @Override
    @Transactional
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        // Reaproveita o save() individual para cada item da coleção.
        // Em um cenário de produção real, isso poderia ser otimizado com
        // batch inserts, mas para fins didáticos a versão simples é mais clara.
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(save(entity));
        }
        return saved;
    }

    @Override
    public Optional<T> findById(ID id) {
        // entityManager.find() busca a entidade pela chave primária.
        // Optional.ofNullable evita retornar null diretamente, seguindo
        // a boa prática que o próprio Qodana estava cobrando nos avisos.
        return Optional.ofNullable(entityManager.find(domainClass, id));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        // Como não temos um EntityManager.findAll() pronto, usamos a Criteria API
        // (a forma "programática" e type-safe de montar queries em JPA, alternativa
        // a escrever JPQL como texto). Isso monta o equivalente a:
        // SELECT t FROM T t
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        query.select(query.from(domainClass));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        // Versão simples: busca um por um. Para grandes volumes, o ideal
        // seria uma única query com "WHERE id IN (:ids)".
        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        // Mesmo raciocínio do findAll(), mas contando registros:
        // SELECT COUNT(t) FROM T t
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        query.select(cb.count(query.from(domainClass)));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        // Primeiro busca a entidade gerenciada, depois remove.
        // JPA exige que a entidade esteja "managed" (gerenciada pelo
        // EntityManager) para poder ser removida com entityManager.remove().
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        // Se a entidade já está sendo gerenciada pelo EntityManager (contains),
        // remove direto. Caso contrário, precisa primeiro "reanexá-la" (merge)
        // antes de poder removê-la.
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<? extends ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        for (T entity : findAll()) {
            delete(entity);
        }
    }

    /**
     * Usa Reflection para encontrar, em tempo de execução, qual campo da
     * entidade está anotado com @Id (a chave primária) e devolve o valor dele.
     *
     * Isso é necessário porque, de forma genérica, não sabemos de antemão
     * qual atributo representa o ID de cada entidade (pode ser "id", "codigo",
     * etc). O Spring Data faz algo parecido internamente, de forma bem mais
     * sofisticada, através dos metadados de persistência (PersistentEntity).
     */
    @SuppressWarnings("unchecked")
    private ID extractId(T entity) {
        try {
            for (Field field : domainClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true); // permite ler campos privados
                    return (ID) field.get(entity);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Não foi possível extrair o ID da entidade", e);
        }
        return null; // nenhum campo anotado com @Id foi encontrado
    }
}