package com.tallerwebi.config;

import com.tallerwebi.dominio.ServicioGoogleOAuth2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Configuración de seguridad para la aplicación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired
  private ServicioGoogleOAuth2 servicioGoogleOAuth2;

  /**
   * Configura las reglas de seguridad HTTP, incluyendo la protección CSRF,
   * las rutas públicas/privadas y la integración del flujo de inicio de sesión con OAuth2.
   *
   * @param http el objeto HttpSecurity utilizado para configurar la seguridad web
   * @throws Exception si ocurre un error en la configuración
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      // Protección CSRF omitida por fines pedagógicos para enfocarse en la integración de OAuth2 y Spring MVC. IMPORTANTE: Habilitar en entornos de producción.
      .csrf()
      .disable()
      // Configuramos qué rutas son públicas y cuáles requieren autenticación
      .authorizeRequests()
      .antMatchers("/login", "/registrarme", "/nuevo-usuario", "/css/**", "/js/**", "/home")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      // Configuramos el inicio de sesión con OAuth2/OpenID Connect (Google)
      .oauth2Login()
      .loginPage("/login")
      .defaultSuccessUrl("/home", true)
      .failureUrl("/login?error=true")
      // Configuramos nuestro servicio personalizado para procesar la información del usuario tras el login
      .userInfoEndpoint()
      .oidcUserService(servicioGoogleOAuth2);
  }

  /**
   * Crea y registra el repositorio de clientes OAuth2 en memoria, cargando las
   * credenciales de Google desde las propiedades del sistema.
   *
   * @param googleClientId     ID del cliente de Google obtenido desde las variables de entorno
   * @param googleClientSecret Secreto del cliente de Google obtenido desde las variables de entorno
   * @return una instancia de ClientRegistrationRepository configurada
   */
  @Bean
  public static ClientRegistrationRepository clientRegistrationRepository(
    @Value("${GOOGLE_CLIENT_ID:}") String googleClientId,
    @Value("${GOOGLE_CLIENT_SECRET:}") String googleClientSecret
  ) {
    return new InMemoryClientRegistrationRepository(
      googleClientRegistration(googleClientId, googleClientSecret)
    );
  }

  /**
   * Construye la configuración necesaria para interactuar con la API de autenticación de Google,
   * incluyendo URLs de autorización, intercambio de tokens y obtención de perfil de usuario.
   *
   * @param clientId     el ID del cliente de Google
   * @param clientSecret el secreto del cliente de Google
   * @return la configuración completa del cliente OAuth2 para Google
   * @throws IllegalArgumentException si el clientId está vacío o nulo
   */
  private static ClientRegistration googleClientRegistration(String clientId, String clientSecret) {
    LOGGER.debug("Google Client ID recibido: '{}'", clientId);
    if (clientId == null || clientId.isEmpty()) {
      throw new IllegalArgumentException("GOOGLE_CLIENT_ID is missing or empty!");
    }

    return ClientRegistration
      .withRegistrationId("google")
      .clientId(clientId)
      .clientSecret(clientSecret)
      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
      .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
      .scope("openid", "profile", "email")
      .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth?prompt=select_account")
      .tokenUri("https://www.googleapis.com/oauth2/v4/token")
      .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
      .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
      .userNameAttributeName("sub")
      .clientName("Google")
      .build();
  }
}
