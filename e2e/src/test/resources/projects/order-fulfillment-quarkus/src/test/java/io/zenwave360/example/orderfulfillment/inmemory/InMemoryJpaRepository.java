package io.zenwave360.example.orderfulfillment.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory store used by generated repository fakes.
 */
public abstract class InMemoryJpaRepository<T, ID> {

    protected final Map<ID, T> entities = new LinkedHashMap<>();

    public T save(T entity) {
        var id = idOf(entity);
        if (id == null) {
            id = newId();
            setId(entity, id);
        }
        entities.put(id, entity);
        return entity;
    }

    public List<T> saveAll(List<T> values) {
        var saved = new ArrayList<T>();
        for (var value : values) {
            saved.add(save(value));
        }
        return saved;
    }

    public T findById(ID id) {
        return entities.get(id);
    }

    public void deleteById(ID id) {
        entities.remove(id);
    }

    public void delete(T entity) {
        var id = idOf(entity);
        if (id != null) {
            entities.remove(id);
        }
    }

    public List<T> findAll() {
        return new ArrayList<>(entities.values());
    }

    public List<T> findAll(int page, int size) {
        var all = findAll();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    public void deleteAll() {
        entities.clear();
    }

    public boolean containsEntity(T entity) {
        return entities.containsValue(entity);
    }

    protected abstract ID idOf(T entity);

    protected abstract void setId(T entity, ID id);

    protected abstract ID newId();
}
