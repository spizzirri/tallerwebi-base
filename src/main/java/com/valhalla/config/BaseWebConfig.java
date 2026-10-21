package com.valhalla.config;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.standard.serializer.StandardJavaScriptSerializer;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Shared MVC base between production and tests: only the concrete context
 * differs.
 */
@Configuration
@EnableWebMvc
@Import({ SecurityConfig.class, ValidationConfig.class })
// com.valhalla.domain is deliberately absent. Nothing in it is a component, and scanning it
// would let the domain grow @Service classes again without anyone noticing the coupling.
@ComponentScan({ "com.valhalla.presentation", "com.valhalla.infrastructure" })
public abstract class BaseWebConfig implements WebMvcConfigurer {

  // Spring + Thymeleaf need this
  @Autowired
  private ApplicationContext applicationContext;

  // Dev-only live reload: active when `mvn jetty:run` serves the templates in
  // place (the directory exists at the repo root). In the packaged WAR (Docker)
  // that path does not exist and the endpoint answers 404 instead.
  private static boolean isLiveReload() {
    return Files.exists(Path.of("src/main/webapp/WEB-INF/templates"));
  }

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**").addResourceLocations("/resources/css/");
    registry.addResourceHandler("/js/**").addResourceLocations("/resources/core/js/");
    registry.addResourceHandler("/images/**").addResourceLocations("/resources/images/");
    // El manifest va como /manifest.json y no como site.webmanifest: Spring no tiene la
    // extensión .webmanifest en su mime.types, así que esa ruta se sirve como
    // application/octet-stream y el navegador descarta el manifest en vez de usarlo. Solo
    // este path resuelve contra /resources/, que tampoco lo expone entero.
    registry.addResourceHandler("/manifest.json").addResourceLocations("/resources/");
  }

  /**
   * Origen absoluto de la request actual, para las etiquetas de compartir. El
   * <code>@{...}</code> de Thymeleaf devuelve una URL relativa en un GET directo, y una
   * <code>og:image</code> relativa no la baja ningún scraper. Thymeleaf 3.1 ya no expone
   * <code>#request</code> en las expresiones, así que el prefijo se arma acá y el template
   * lo lee como <code>${@siteOrigin.base()}</code>. Sale de la request y no de una
   * constante: detrás de un proxy el host público no es adivinable desde el código.
   */
  @Bean
  @Scope(WebApplicationContext.SCOPE_REQUEST)
  public SiteOrigin siteOrigin(HttpServletRequest request) {
    return new SiteOrigin(
      request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
    );
  }

  /**
   * Origen absoluto de una request. Sin proxy de scope: solo lo lee el template.
   *
   * @param base scheme, host y puerto de la request, con el separador {@code ://} ya puesto
   */
  public record SiteOrigin(String base) {}

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (isLiveReload()) {
      registry
        .addInterceptor(new DevReloadInterceptor(devReloadController()))
        .addPathPatterns("/**");
    }
  }

  // No HiddenHttpMethodFilter bean on purpose: a Filter bean is never installed in this plain Spring
  // MVC WAR. MyServletInitializer registers the filter with the servlet container instead.
  @Bean
  public DevReloadController devReloadController() {
    return new DevReloadController();
  }

  // https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html
  // Spring + Thymeleaf
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    // SpringResourceTemplateResolver automatically integrates with Spring's own
    // resource resolution infrastructure, which is highly recommended.
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setApplicationContext(this.applicationContext);
    templateResolver.setPrefix("/WEB-INF/templates/");
    templateResolver.setSuffix(".html");
    // HTML is the default value, added here for the sake of clarity.
    templateResolver.setTemplateMode(TemplateMode.HTML);
    // Template cache is off so template edits appear on refresh (F5) without
    // restarting the server. This is the dev-friendly default for a taller.
    templateResolver.setCacheable(false);
    // Thymeleaf reads templates as ISO-8859-1 by default: without this, any
    // non-ASCII byte in a template comment or text would render double-encoded.
    templateResolver.setCharacterEncoding("UTF-8");
    return templateResolver;
  }

  // Spring + Thymeleaf
  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    // Replace the default dialect with one whose JavaScript serializer never
    // delegates to Jackson. Jackson switches on automatically because
    // jackson-databind is on the classpath (the REST API needs it), but its
    // serializer flushes the response writer on every inlined /*[[...]]*/
    // expression, committing the response mid-render. Spring Security then
    // cannot create the session to store the CSRF token and the page is sent
    // truncated ("Response is committed"). Thymeleaf's own serializer buffers
    // until render end, which is how this behaved before jackson-databind.
    SpringStandardDialect springStandardDialect = new SpringStandardDialect();
    springStandardDialect.setJavaScriptSerializer(new StandardJavaScriptSerializer(false));
    // setDialect() clears every dialect, so it must run before addDialect("sec", ...).
    templateEngine.setDialect(springStandardDialect);
    templateEngine.setTemplateResolver(templateResolver());
    templateEngine.setEnableSpringELCompiler(true);
    templateEngine.addDialect("sec", new SpringSecurityDialect());
    return templateEngine;
  }

  // Spring + Thymeleaf
  // Configure Thymeleaf View Resolver
  @Bean
  public ThymeleafViewResolver viewResolver() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    // ThymeleafViewResolver answers text/html;charset=iso-8859-1 unless told
    // otherwise. The browser honors the header over the <meta charset> tag, so
    // pages rendered without this sent the page as Latin-1 and the browser
    // built every form body in Latin-1 too: Jetty decodes form content as
    // UTF-8, and the first accented value a user submitted blew up with 400
    // "Unable to parse form content: Invalid UTF-8".
    viewResolver.setCharacterEncoding("UTF-8");
    return viewResolver;
  }
}
