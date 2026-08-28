package com.hexnotech.commons.base;

import com.hexnotech.commons.annotation.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Abstract base entity providing standard audit fields for all JPA entities.
 * <p>
 * Extend this class instead of duplicating id, createdAt, updatedAt in every entity:
 *
 * <pre>{@code
 * @Auditable
 * @Entity
 * @Table(name = "bookings")
 * public class Booking extends BaseEntity {
 *     private String reference;
 * }
 * }</pre>
 *
 * Fields provided:
 * <ul>
 *   <li>{@code id}        — auto-generated Long primary key (SEQUENCE strategy)</li>
 *   <li>{@code createdAt} — set once on persist</li>
 *   <li>{@code updatedAt} — updated on every merge</li>
 * </ul>
 */
@Auditable
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
