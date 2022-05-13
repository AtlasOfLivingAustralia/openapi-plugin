package au.org.ala.plugins.openapi

class OpenApiUrlMappings {

    static mappings = {
        "/openapi/$action?/$id?(.$format)?"(controller: "openApi")
        name openapiDoc: "/openapi/openapi(.$format)?" {
            controller = 'openApi'
            action = "openapi"
        }
    }
}
