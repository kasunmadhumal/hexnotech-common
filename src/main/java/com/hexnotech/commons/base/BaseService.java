package com.hexnotech.commons.base;

import java.util.List;
import java.util.Optional;

/**
 * Generic base service interface that defines the standard CRUD contract.
 * <p>
 * Service classes can implement this interface for consistency across all
 * Hexnotech backend services. {@code T} is the domain/response DTO type,
 * {@code ID} is the primary key type (usually {@code Long}).
 *
 * <pre>{@code
 * @Service
 * public class BookingService implements BaseService<BookingDto, Long> {
 *     ...
 * }
 * }</pre>
 *
 * @param <T>  The DTO / response type
 * @param <ID> The primary key type
 */
public interface BaseService<T, ID> {

    /**
     * Find a single record by its primary key.
     *
     * @param id the primary key
     * @return an {@link Optional} containing the record, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Retrieve all records.
     *
     * @return list of all records
     */
    List<T> findAll();

    /**
     * Persist a new record.
     *
     * @param entity the entity / DTO to create
     * @return the persisted record
     */
    T create(T entity);

    /**
     * Update an existing record identified by {@code id}.
     *
     * @param id     the primary key of the record to update
     * @param entity the updated values
     * @return the updated record
     */
    T update(ID id, T entity);

    /**
     * Delete a record by its primary key.
     *
     * @param id the primary key
     */
    void deleteById(ID id);
}
