package com.tallerwebi.preparacion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ThymeleafPreProcessor {

    private static class CustomLinkBuilder extends StandardLinkBuilder {
        @Override
        protected String computeContextPath(IExpressionContext context, String base, Map<String, Object> parameters) {
            return ""; // No añadir ningún path de contexto.
        }
    }

    private final TemplateEngine templateEngine;
    private final String outputDir = "src/test/web/resources/generated/";
    private final String templateDir = "src/main/webapp/WEB-INF/views/thymeleaf/";

    public ThymeleafPreProcessor() {
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(new StringTemplateResolver());
        this.templateEngine.setLinkBuilder(new CustomLinkBuilder());
    }

    public void process(String templateName, Map<String, Object> contextData) throws IOException {
        String templatePath = Paths.get(templateDir, templateName + ".html").toString();
        String content = new String(Files.readAllBytes(Paths.get(templatePath)));

        Context context = new Context();
        if (contextData != null) {
            context.setVariables(contextData);
        }

        // Procesar la plantilla con Thymeleaf (esto resuelve th:object, th:if, etc.)
        String renderedHtml = templateEngine.process(content, context);

        // AHORA: Usar Jsoup para manipular el HTML generado por Thymeleaf
        Document doc = Jsoup.parse(renderedHtml);

        // Obtener la ruta raíz del proyecto dinámicamente
        String projectRoot = System.getProperty("user.dir") + "/";
        String jsBasePath = projectRoot + "src/main/webapp/resources/core/js/";
        String cssBasePath = projectRoot + "src/main/webapp/resources/core/css/";

        // Manipular atributos th:href, th:src, th:action
        Elements elementsWithThAttributes = doc.select("[th:href], [th:src], [th:action], [th:field]");

        for (Element element : elementsWithThAttributes) {
            // th:href
            if (element.hasAttr("th:href")) {
                String thHref = element.attr("th:href");
                String cleanHref = thHref.replaceFirst("^@\\{([^}]+)\\}$", "$1"); // Eliminar @{}
                element.attr("href", cleanHref);
                element.removeAttr("th:href");
            }

            // th:src
            if (element.hasAttr("th:src")) {
                String thSrc = element.attr("th:src");
                String cleanSrc = thSrc.replaceFirst("^@\\{([^}]+)\\}$", "$1"); // Eliminar @{}
                element.attr("src", cleanSrc);
                element.removeAttr("th:src");
            }

            // th:action (manejar conflicto con action="#")
            if (element.hasAttr("th:action")) {
                String thAction = element.attr("th:action");
                String cleanAction = thAction.replaceFirst("^@\\{([^}]+)\\}$", "$1"); // Eliminar @{}
                
                // Si ya tiene un action="#" (placeholder), lo eliminamos
                if (element.hasAttr("action") && element.attr("action").equals("#")) {
                    element.removeAttr("action");
                }
                element.attr("action", cleanAction);
                element.removeAttr("th:action");
            }

            // th:field (Thymeleaf ya lo procesa, pero si queda algo, lo limpiamos)
            if (element.hasAttr("th:field")) {
                // th:field="*{email}" -> name="email" value="test@tallerwebi.com"
                // Jsoup no evalúa OGNL, así que esto es para limpiar si Thymeleaf no lo hizo
                // o si queremos un valor específico para el test.
                // Por ahora, solo lo removemos si no es necesario.
                // element.removeAttr("th:field"); // Descomentar si th:field causa problemas
            }
        }

        // Manipular rutas JS relativas a absolutas
        Elements scriptElements = doc.select("script[src]");
        for (Element script : scriptElements) {
            String src = script.attr("src");
            if (src.startsWith("js/")) { // Si la ruta es relativa a "js/"
                script.attr("src", "file://" + jsBasePath + src.substring(3)); // Reemplazar con la ruta absoluta
            }
            else if (src.startsWith("/js/")) { // Si la ruta es relativa a "js/"
                script.attr("src", "file://" + jsBasePath + src.substring(4)); // Reemplazar con la ruta absoluta
            }
        }

        Elements linkElements = doc.select("link[rel=stylesheet][href]");
        for (Element link : linkElements) {
            String href = link.attr("href");
            if (href.startsWith("css/")) { // Si la ruta es relativa a css/
                link.attr("href", "file://" + cssBasePath + href.substring(4)); // Reemplazar con la ruta absoluta
            }
            else if (href.startsWith("/css/")) { // Si la ruta es relativa a /css/
                link.attr("href", "file://" + cssBasePath + href.substring(5)); // Reemplazar con la ruta absoluta
            }
        }

        renderedHtml = doc.html(); // Obtener el HTML modificado por Jsoup

        try (FileWriter writer = new FileWriter(Paths.get(outputDir, templateName + ".html").toString())) {
            writer.write(renderedHtml);
        }
        System.out.println(" -> Generado: " + outputDir + templateName + ".html");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("ERROR: Se requieren 2 argumentos: <templateName> <jsonDataPath>");
            System.exit(1);
        }

        String templateName = args[0];
        String jsonDataPath = args[1];

        System.out.println("Iniciando pre-procesamiento para '" + templateName + "' con datos de '" + jsonDataPath + "'");

        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonDataPath)));
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> contextData = gson.fromJson(jsonContent, type);

        ThymeleafPreProcessor processor = new ThymeleafPreProcessor();
        processor.process(templateName, contextData);

        System.out.println("Pre-procesamiento completado.");
    }
}
