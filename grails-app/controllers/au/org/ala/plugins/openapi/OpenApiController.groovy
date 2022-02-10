package au.org.ala.plugins.openapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

class OpenApiController {

    static namespace = 'v1'
    static allowedMethods = [getDocuments: "GET"]

    OpenApiService openApiService

    @Value("classpath*:**/webjars/swagger-ui/**/index.html")
    Resource[] swaggerUiResources

    def index() {
        redirect uri: "/webjars/swagger-ui${getSwaggerUiFile()}?url=${g.createLink(controller: 'openApi', action: 'openapi')}"
    }

    def openapi() {

        def pretty = params.boolean('pretty', false)
        withFormat {
            json { render(contentType: 'application/json', text: openApiService.openApiJsonString(pretty)) }
            '*' { render(contentType: 'application/yaml', text: openApiService.openApiYamlString(pretty)) }
        }
    }

    protected String getSwaggerUiFile() {
        try {
            (swaggerUiResources.getAt(0) as Resource).getURI().toString().split("/webjars/swagger-ui")[1]
        } catch (Exception e) {
            throw new Exception("Unable to find swagger ui.. Please make sure that you have added swagger ui dependency eg:-\n compile 'org.webjars:swagger-ui:4.5.0' \nin your build.gradle file", e)
        }
    }

}
