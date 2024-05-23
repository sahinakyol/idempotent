package org.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as idempotent, meaning they can be safely called multiple times
 * without changing the result beyond the initial application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Idempotent {
    /**
     * Time-to-live (TTL) for the idempotency key in milliseconds.
     * Default is 20000 milliseconds.
     *
     * @return the TTL in milliseconds.
     */
    long ttl() default 20000;
}