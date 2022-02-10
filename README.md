# OpenAPI Plugin for Grails

This is a pre-release plugin for adding OpenAPI documentation to APIs.

This pre-release version is heavily based on the OpenAPI JAX-RS implementation.  Because of this it imports and relies
on some existing JAX-RS API annotations, such as `@Path`, `@Produces` and `@Consumes`.  Eventually this plugin should provide alternatives for all these and drop the dependency on the JAX-RS API.

## Using

Add the plugin to your `build.gradle`:

`compile 'au.org.ala.plugins:openapi:0.1.0-SNAPSHOT`

Configure the static content using your `application.yml`.  An example is presented below:

```yaml
openapi:
  title: UserDetails REST services
  description: REST services for interacting with the user details webapp
  terms: https://www.ala.org.au/terms
  contact:
    name: Support
    email: support@ala.org.au
  license:
    name: Mozilla Public License 1.1
    url: https://www.mozilla.org/en-US/MPL/1.1/
  version: '@info.app.version@'
  cachetimeoutms: 0
```

`cachetimeoutms` defaults to indefinite caching (a value of -1).  It can be set to 0 to disable caching or using a positive value to set the cache timeout in MS.

Then, take a Grails action or controller that should be included in an OpenAPI definition and add some annotations:

### @Operation

Only actions annotated with the OpenAPI `@Operation` will be included in the OpenAPI spec.
The more information you can provide in the method signature (ala JAX-RS) the less duplicated information will need to 
be included in the `@Operation` annotation.  For example, an action with a return type means that the return type can be
used to fill in parts of the `@Operation` and then doesn't duplicate the existing code because the code is the 
documentation.  Additionally, request parameters can added as the action parameters and marked with @Parameter 
annotations or similar, which means the `@Operation` doesn't need information about parameters added.

#### Example

```groovy
@Operation(
        method = "POST",
        tags = "users",
        operationId = "getUserDetailsFromIdList",
        summary = "Get User Details by id list",
        description = "Get a list of user details for a list of user ids",
        requestBody = @RequestBody(
                description = "The list of user ids to request and whether to include extended properties",
                required = true,
                content = @Content(
                        mediaType = 'application/json',
                        schema = @Schema(implementation = GetUserDetailsFromIdListRequest)
                )
        ),
        responses = [
        @ApiResponse(
                description = "User Details",
                responseCode = "200",
                content = [
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GetUserDetailsFromIdListResponse)
                )
                ]
        )
        ]
)
```

### @Path

`@Path` can be added to either a controller or action.  Added to a controller it adds a prefix to the URL path for all actions in a controller.  On an action it will define the canonical path for that action.

If ommitted, the grails link generator will be used to generate the path to the action.

#### TODO

Provide a way for URL Mappings to be used and a way to disambiguate when the default path generated by the link generator is not desired.

### @Consumes / @Produces

Indicates the Media Types that an action will consume as a request body or produce as a response body.

*NOTE:* In these early WIP versions a `@Produces` annotation may be required for an operation to appear.

#### TODO
 - May also be applied to the Controller?
 - Use `static responseFormats` as Controller level `@Produces`

## Schema Generation

Schema generation may be controlled in a number of ways.  The simplest is to provide an `implementation` attribute on
a `@Schema` annotation.  The generated Schema from an implementation class can be controlled by the use of Jackson based
annotations.  

eg, the following @Schema will generate a schema for the UserDetails class and any additional non-primitive schemas for
its properties.

```groovy
@Schema(implementation = UserDetails)
```

### Notes

On thing to note for classes generated by the Groovy compiler is that they have a `metaClass` attribute which the
generator will find and will create a large amount of unnecessary types in the Schema.  To remedy this, add a Jackson
`@JsonIgnoreProperties('metaClass')` annotation to the type.

```groovy
@JsonIgnoreProperties('metaClass')
static class GetUserDetailsFromIdListRequest {
  // ...
}
```
### Todo

 - Investigate support for common schema / types
 - Investigate support for code using `request.JSON` or `render([some: 'result'])`


## Further work

### Documenting Security

TODO: Adding Bearer token auth?  Probably should be configurable globably and referenced by `@Operation`.

### Refs

eg add an ApiResponse, Params, Schema, etc that can be referenced from multiple `@Operation`s.  