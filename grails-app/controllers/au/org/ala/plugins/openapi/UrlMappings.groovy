package au.org.ala.plugins.openapi

class UrlMappings {

    static mappings = {
        "/openapi/$action?/$id?(.$format)?"(controller: "openApi")
    }
}
