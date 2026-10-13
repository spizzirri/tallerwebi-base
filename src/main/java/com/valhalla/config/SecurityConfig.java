package com.valhalla.config;

import com.valhalla.infrastructure.security.CustomAuthenticationFailureHandler;
import com.valhalla.infrastructure.security.CustomAuthenticationSuccessHandler;
import com.valhalla.infrastructure.security.HomeOnAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private CustomAuthenticationSuccessHandler successHandler;

  @Autowired
  private CustomAuthenticationFailureHandler failureHandler;

  @Autowired
  private HomeOnAccessDeniedHandler homeOnAccessDeniedHandler;

  // 1. Cadena para Usuarios Comunes (/auth/**) [AUT-01]

  @Bean
  @Order(1)
  public SecurityFilterChain authFilterChain(HttpSecurity http, SessionRegistry sessionRegistry)
    throws Exception {
    http
      .securityMatcher("/auth/**")
      .authorizeHttpRequests(auth ->
        // "Público" acá no es "para cualquiera": es abierto a quien NO sea admin. Nadie tiene que
        // estar adentro para registrarse ni para recuperar contraseña, así que un anónimo tiene que
        // poder entrar a /auth/register y a /auth/forgot-password; y un USER tiene que poder usar
        // toda la superficie. Lo único que no corresponde es un ADMIN, que ya está en su lugar: el
        // navbar de un admin ofrece /admin/logout, no /auth/logout.
        //
        // isAnonymous() hace falta además del permitAll() plano: permitAll() autoriza también a un
        // ADMIN, que es justo lo que esta regla viene a negar.
        auth
          .anyRequest()
          .access(new WebExpressionAuthorizationManager("isAnonymous() or hasRole('USER')"))
      )
      .exceptionHandling(ex -> ex.accessDeniedHandler(this.homeOnAccessDeniedHandler))
      .formLogin(form ->
        form
          .loginPage("/auth/login")
          .loginProcessingUrl("/auth/validate-login")
          .usernameParameter("username")
          .passwordParameter("password")
          .successHandler(successHandler)
          .failureHandler(failureHandler)
          .permitAll()
      )
      .logout(logout ->
        logout
          .logoutUrl("/auth/logout")
          .logoutSuccessUrl("/auth/login?logout=true")
          .invalidateHttpSession(true)
          .deleteCookies("JSESSIONID")
          .permitAll()
      )
      .sessionManagement(session ->
        session.maximumSessions(1).sessionRegistry(sessionRegistry).maxSessionsPreventsLogin(false)
      );

    return http.build();
  }

  // 2. Cadena para Administradores

  @Bean
  @Order(2)
  public SecurityFilterChain adminFilterChain(HttpSecurity http, SessionRegistry sessionRegistry)
    throws Exception {
    http
      .securityMatcher("/admin/**")
      .authorizeHttpRequests(auth ->
        // /admin/** es de ADMIN, sin excepción: /admin/home estaba en authenticated() y dejaba
        // entrar a un USER común al home del admin. Solo el par login + validate-login queda
        // abierto, porque es el punto de entrada: sin él no hay forma de obtener una sesión de
        // admin. El controller de /admin/login ya manda al que llega con sesión a su home.
        auth
          .requestMatchers("/admin/login", "/admin/validate-login")
          .permitAll()
          .requestMatchers("/admin/**")
          .hasRole("ADMIN")
      )
      .exceptionHandling(ex -> ex.accessDeniedHandler(this.homeOnAccessDeniedHandler))
      .formLogin(form ->
        form
          .loginPage("/admin/login")
          .loginProcessingUrl("/admin/validate-login")
          .usernameParameter("email")
          .passwordParameter("password")
          .successHandler(successHandler)
          .failureUrl("/admin/login?error=true")
          .permitAll()
      )
      .logout(logout ->
        logout
          .logoutUrl("/admin/logout")
          // Mirrors the /auth chain above: the flag is what lets the login view confirm the
          // session ended, instead of the user landing on a form that says nothing about it.
          .logoutSuccessUrl("/admin/login?logout=true")
          .invalidateHttpSession(true)
          .deleteCookies("JSESSIONID")
      )
      .sessionManagement(session ->
        session.maximumSessions(1).sessionRegistry(sessionRegistry).maxSessionsPreventsLogin(false)
      );

    return http.build();
  }

  // 3. Cadena Default para el resto del sitio

  @Bean
  @Order(3)
  public SecurityFilterChain defaultFilterChain(HttpSecurity http, SessionRegistry sessionRegistry)
    throws Exception {
    http
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**", "/explore")
          .permitAll()
          // [PPV, decision de producto] El feed /plans/public es para usuarios con sesion: cae en
          // el anyRequest().authenticated() de abajo, junto al resto de /plans/**, asi que un
          // anonimo que llegue a /plans/public aterriza en /auth/login.
          // [PVP] En cambio la vista publica de un plan compartido es visible sin sesion. El
          // allowlist es solo para GET: el POST de /plans/{id}/clone [CLO] sigue exigiendo estar
          // autenticado.
          .requestMatchers(org.springframework.http.HttpMethod.GET, "/plans/*/public")
          .permitAll()
          .requestMatchers("/api/**")
          .authenticated()
          .requestMatchers("/", "/share/**", "/reload/**")
          .permitAll()
          .requestMatchers("/css/**", "/js/**", "/images/**", "/manifest.json")
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .formLogin(form ->
        form
          .loginPage("/auth/login")
          .loginProcessingUrl("/auth/validate-login")
          .usernameParameter("username")
          .passwordParameter("password")
          .successHandler(successHandler)
          .failureHandler(failureHandler)
          .permitAll()
      )
      .exceptionHandling(ex ->
        ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/auth/login"))
      )
      .sessionManagement(session ->
        session.maximumSessions(1).sessionRegistry(sessionRegistry).maxSessionsPreventsLogin(false)
      );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }
}
