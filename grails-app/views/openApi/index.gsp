<!-- HTML for static distribution bundle build -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Swagger UI</title>
    <asset:stylesheet src="webjars/swagger-ui/4.5.0/swagger-ui.css" />
    <asset:link rel="icon" href="webjars/swagger-ui/4.5.0/favicon-16x16.png" sizes="32x32" />
    <asset:link rel="icon" href="webjars/swagger-ui/4.5.0/favicon-32x32.png" sizes="16x16" />
    <style>
    html
    {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
    }

    *,
    *:before,
    *:after
    {
        box-sizing: inherit;
    }

    body
    {
        margin:0;
        background: #fafafa;
    }
    </style>
</head>

<body>
<div id="swagger-ui"></div>

<asset:javascript src="webjars/swagger-ui/4.5.0/swagger-ui-bundle.js" charset="UTF-8" />
<asset:javascript src="webjars/swagger-ui/4.5.0/swagger-ui-standalone-preset.js" charset="UTF-8" />
<script>
    window.onload = function() {
        // Begin Swagger UI call region
        const ui = SwaggerUIBundle({
            url: "${g.createLink(controller: 'openApi', action: 'openapi', format: 'json')}",
            dom_id: '#swagger-ui',
            deepLinking: true,
            presets: [
                SwaggerUIBundle.presets.apis,
                SwaggerUIStandalonePreset
            ],
            plugins: [
                SwaggerUIBundle.plugins.DownloadUrl
            ],
            layout: "StandaloneLayout"
        });
        // End Swagger UI call region

        window.ui = ui;
    };
</script>
</body>
</html>
