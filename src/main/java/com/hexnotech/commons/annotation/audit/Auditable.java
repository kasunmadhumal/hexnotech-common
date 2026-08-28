package com.hexnotech.commons.annotation.audit;

import java.lang.annotation.*;

/**
 * Marks a JPA entity for automatic audit field population.
 * <p>
 * Apply this annotation alongside {@code @MappedSuperclass} or on any entity that
 * extends {@link com.hexnotech.commons.base.BaseEntity}. The {@code createdAt},
 * {@code updatedAt}, and optionally {@code createdBy} / {@code updatedBy} fields
 * are automatically managed by JPA lifecycle callbacks defined in {@code BaseEntity}.
 *
 * <pre>{@code
 * @Auditable
 * @Entity
 * public class Order extends BaseEntity { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Human-readable label for this audited entity (used in logs / audit trails).
     * Defaults to the simple class name if left empty.
     */
    String label() default "";
}
