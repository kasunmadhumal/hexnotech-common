package com.hexnotech.commons.annotation.web;

import java.lang.annotation.*;

/**
 * Declares the API version for a Spring MVC controller or handler method.
 * <p>
 * This is a documentation / routing-convention annotation. Combine it with a
 * {@code RequestMappingHandlerMapping} customisation or simply use it as a
 * readable marker alongside {@code @RequestMapping("/api/v1/...")}.
 *
 * <pre>{@code
 * @ApiVersion("v1")
 * @RestController
 * @RequestMapping("/api/v1/users")
 * public class UserController { ... }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * The API version string, e.g. {@code "v1"}, {@code "v2"}.
     */
    String value();

    /**
     * Mark the version as deprecated so consumers know to migrate.
     */
    boolean deprecated() default false;
}
