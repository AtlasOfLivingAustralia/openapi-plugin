package au.org.ala.plugins.openapi

import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

/**
 * Annotation to mark the preferred path for an action in an OpenAPI definition.
 * May also be used for describing the location of path parameters in the path.
 * Path parameters may be described using the JAX-RS template format, ie
 * path parameters should be enclosed in curly braces and use the same name as
 * provided in the @Operation annotation: {}
 *
 * eg
 * @Path('/blog/{yyyy}/{mm}/{dd}')
 */
@Target(value=[TYPE,METHOD])
@Retention(value=RUNTIME)
@interface Path {
    String value = ''
}