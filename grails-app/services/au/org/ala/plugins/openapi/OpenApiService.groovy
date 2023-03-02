package au.org.ala.plugins.openapi

import grails.core.GrailsApplication
import grails.web.mapping.LinkGenerator
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder
import io.swagger.v3.oas.integration.OpenApiConfigurationException
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import java.lang.reflect.Modifier

class OpenApiService {

    @Value('${openapi.cachetimeoutms:-1}')
    final long CACHE_TIMEOUT_MS = -1

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    LinkGenerator linkGenerator

    OpenAPI getOpenApi() {
        OpenAPI oas = new OpenAPI()
        Info info = new Info()
                .title(grailsApplication.config.getProperty("openapi.title"))
                .description(grailsApplication.config.getProperty("openapi.description"))
                .termsOfService(grailsApplication.config.getProperty("openapi.terms"))
                .contact(new Contact()
                        .name(grailsApplication.config.getProperty("openapi.contact.name"))
                        .email(grailsApplication.config.getProperty("openapi.contact.email")))
                .license(new License()
                        .name(grailsApplication.config.getProperty("openapi.license.name"))
                        .url(grailsApplication.config.getProperty("openapi.license.url")))
                .version(grailsApplication.config.getProperty("openapi.version"))

        oas.info(info)
        def servers = grailsApplication.config.getProperty("openapi.server-urls", List<String>, [])
        if (!servers) servers = [grailsApplication.config.getProperty("grails.serverURL")]
        for (server in servers) {
            oas.addServersItem(new Server().url(server))
        }

        oas.setComponents(new Components())

        if (grailsApplication.config.getProperty('security.oidc.enabled', Boolean, false)) {
            def discoveryUri = grailsApplication.config.getProperty('security.oidc.discovery-uri') ?: grailsApplication.config.getProperty('security.oidc.discoveryUri')
            SecurityScheme oidcScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.OPENIDCONNECT)
                    .name('openIdConnect')
                    .openIdConnectUrl(discoveryUri)

            oas.components.addSecuritySchemes('openIdConnect', oidcScheme)

            // Allow client to disable separate oauth2 flow
            if (grailsApplication.config.getProperty('openapi.components.security.oauth2.enabled', Boolean, true)) {

                SecurityScheme oauthScheme = new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().tap {
                            def scopes = new Scopes()
                            def authzUrl = grailsApplication.config.getProperty('openapi.components.security.oauth2.authorizationUrl') ?: grailsApplication.config.getProperty('openapi.components.security.oauth2.authorization-url')
                            def tokenUrl = grailsApplication.config.getProperty('openapi.components.security.oauth2.tokenUrl') ?: grailsApplication.config.getProperty('openapi.components.security.oauth2.token-url')
                            def refreshUrl = grailsApplication.config.getProperty('openapi.components.security.oauth2.refreshUrl') ?: grailsApplication.config.getProperty('openapi.components.security.oauth2.refresh-url')
                            grailsApplication.config.getProperty('openapi.components.security.oauth2.scopes', Map, [:]).each { scope ->
                                scopes.addString(scope.key, scope.value)
                            }
                            if (grailsApplication.config.getProperty('openapi.components.security.oauth2.client-credentials-flow-enabled', Boolean, true)) {
                                it.clientCredentials(
                                        new OAuthFlow()
                                                .authorizationUrl(authzUrl)
                                                .tokenUrl(tokenUrl)
                                                .refreshUrl(refreshUrl)
                                                .scopes(scopes)
                                )
                            }
                            if (grailsApplication.config.getProperty('openapi.components.security.oauth2.password-flow-enabled', Boolean, false)) {
                                it.password(
                                        new OAuthFlow()
                                                .authorizationUrl(authzUrl)
                                                .tokenUrl(tokenUrl)
                                                .refreshUrl(refreshUrl)
                                                .scopes(scopes)
                                )
                            }
                            if (grailsApplication.config.getProperty('openapi.components.security.oauth2.implicit-flow-enabled', Boolean, false)) {
                                it.implicit(
                                        new OAuthFlow()
                                                .authorizationUrl(authzUrl)
                                                .tokenUrl(tokenUrl)
                                                .refreshUrl(refreshUrl)
                                                .scopes(scopes)
                                )
                            }
                            if (grailsApplication.config.getProperty('openapi.components.security.oauth2.authcode-flow-enabled', Boolean, true)) {
                                it.authorizationCode(
                                        new OAuthFlow()
                                                .authorizationUrl(authzUrl)
                                                .tokenUrl(tokenUrl)
                                                .refreshUrl(refreshUrl)
                                                .scopes(scopes)
                                )
                            }
                        })
                oas.components.addSecuritySchemes('oauth', oauthScheme)
            }
        }
        if (grailsApplication.config.getProperty('security.cas.enabled', Boolean, false)) {
            SecurityScheme sessionCookieScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name('jsessionid')
            oas.components.addSecuritySchemes('sessionCookie', sessionCookieScheme)
        }
        if (grailsApplication.config.getProperty('security.jwt.enabled', Boolean, false)) {
            SecurityScheme jwtScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .bearerFormat('JWT')

            oas.components.addSecuritySchemes('jwt', jwtScheme)
        }
        if (grailsApplication.config.getProperty('security.jwt.fallbackToLegacy', Boolean, false)) {
            SecurityScheme apiKeyScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(grailsApplication.config.getProperty('security.apikey.header.override') ?: 'apiKey')

            oas.components.addSecuritySchemes('apikey', apiKeyScheme)
        }

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .cacheTTL(CACHE_TIMEOUT_MS)
                .readAllResources(false)
                .readerClass(GrailsOpenApiReader.name)
                .resourceClasses(getResources())

        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.ast')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.ast.stmt')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.control')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.control.customizers')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.reflection')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.reflection')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.runtime.callsite')
        ModelConverters.instance.addPackageToSkip('org.codehaus.groovy.syntax')
        ModelConverters.instance.addPackageToSkip('jdk.internal.reflect')
        ModelConverters.instance.addPackageToSkip('groovy.lang')

        try {

//            def context = new JaxrsOpenApiContextBuilder()
            def context = new GenericOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)

            def reader = new GrailsOpenApiReader()
            reader.grailsApplication = this.grailsApplication
            reader.linkGenerator = linkGenerator
            reader.configuration = oasConfig

            context.setOpenApiReader(reader)

            OpenAPI openAPI = context.read()

            return openAPI
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e)
        }

    }

    String openApiJsonString(boolean pretty = false) {
        return (pretty ? Json.pretty() : Json.mapper().writer()).writeValueAsString(openApi)
    }

    String openApiYamlString(boolean pretty = false) {
        return (pretty ? Yaml.pretty() : Yaml.mapper().writer()).writeValueAsString(openApi)
    }

    private Set<String> getResources() {
        def classes = grailsApplication.getArtefacts("Controller")*.clazz
        classes.findAll {
            it.methods.any {
                Modifier.isPublic(it.modifiers) && it.getAnnotation(Operation)
            }
        }*.name.toSet()
    }
}
