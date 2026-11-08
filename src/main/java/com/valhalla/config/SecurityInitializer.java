package com.valhalla.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Registers {@code springSecurityFilterChain} (a DelegatingFilterProxy) with the
 * servlet container. The proxy resolves the Spring bean at runtime from the
 * servlet context, so no config classes are given. Without this initializer the
 * security filters (CSRF, authentication) never execute and {@code _csrf} is
 * null in Thymeleaf templates.
 */
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
  // No constructor args: the proxy finds springSecurityFilterChain in the
  // servlet WebApplicationContext (registered via SecurityConfig there).
  // onStartup es final aca: el ForwardedHeaderFilter se registra en MyServletInitializer.
}
