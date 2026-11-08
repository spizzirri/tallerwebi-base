package com.valhalla;

import com.valhalla.config.JpaConfig;
import com.valhalla.config.SecurityConfig;
import com.valhalla.config.SpringWebConfig;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.util.EnumSet;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  private static final String HIDDEN_HTTP_METHOD_FILTER = "hiddenHttpMethodFilter";
  private static final String FORWARDED_HEADER_FILTER = "forwardedHeaderFilter";

  // services and data sources
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[0];
  }

  // controller, view resolver, handler mapping
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class<?>[] { SpringWebConfig.class, JpaConfig.class, SecurityConfig.class };
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    registerForwardedHeaderFilter(servletContext);
    super.onStartup(servletContext);
    registerHiddenHttpMethodFilter(servletContext);
    // Track session create/destroy so Spring Security's SessionRegistry can
    // enforce maximumSessions(1) (AC-13). Without this listener, expired or
    // logged-out sessions are never removed from the registry.
    servletContext.addListener(new HttpSessionEventPublisher());
  }

  /**
   * El equivalente de {@code server.forward-headers-strategy=native} de Boot para este WAR
   * sin Boot. Detras de un proxy o tunel (cloudflared/ngrok) el request real es
   * {@code http://localhost}: sin esto, {@code getScheme()}/{@code getServerName()} mentirian a
   * todo el que arme una URL absoluta (el bean siteOrigin de las etiquetas OG y los redirects
   * de Security). El filtro reescribe el request desde {@code X-Forwarded-Proto}/{@code Host};
   * se registra antes que todo lo demas para correr primero en la cadena.
   */
  private void registerForwardedHeaderFilter(ServletContext servletContext) {
    if (servletContext.getFilterRegistration(FORWARDED_HEADER_FILTER) != null) {
      return;
    }
    servletContext
      .addFilter(FORWARDED_HEADER_FILTER, new ForwardedHeaderFilter())
      .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
  }

  /**
   * Installs the HiddenHttpMethodFilter in the servlet container. A {@code @Bean} Filter is
   * invisible here: Boot's auto-registration of Filter beans belongs to Spring Boot, and this WAR
   * is bootstrapped by {@link AbstractAnnotationConfigDispatcherServletInitializer}. Without this,
   * {@code _method} was ignored at runtime while MockMvc, which picks Filter beans up from the
   * context, pretended it worked.
   */
  private void registerHiddenHttpMethodFilter(ServletContext servletContext) {
    if (servletContext.getFilterRegistration(HIDDEN_HTTP_METHOD_FILTER) != null) {
      return;
    }
    servletContext
      .addFilter(HIDDEN_HTTP_METHOD_FILTER, new HiddenHttpMethodFilter())
      .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false);
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }
}
