package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * EntityManager implementation of the Order repository port.
 */
@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {

    private final EntityManager entityManager;

    @Inject
    public OrderRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Order save(Order entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public List<Order> saveAll(List<Order> entities) {
        var saved = new ArrayList<Order>();
        for (var entity : entities) {
            saved.add(save(entity));
        }
        return saved;
    }

    @Override
    public Order findById(Long id) {
        return entityManager.find(Order.class, id);
    }

    @Override
    public void deleteById(Long id) {
        var found = findById(id);
        if (found != null) {
            delete(found);
        }
    }

    @Override
    public void delete(Order entity) {
        var managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    @Override
    public List<Order> findAll() {
        return entityManager.createQuery("from Order", Order.class).getResultList();
    }

    @Override
    public List<Order> findAll(int page, int size) {
        return entityManager
                .createQuery("from Order", Order.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public java.util.Optional<Order> findByOrderNumber(String orderNumber) {
        return entityManager
                .createQuery("from Order where orderNumber = :orderNumber", Order.class)
                .setParameter("orderNumber", orderNumber)
                .getResultStream()
                .findFirst();
    }
}
