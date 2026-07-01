package com.tallerwebi.config;

import com.tallerwebi.dominio.Usuario.ServicioUsuarioOAuth;
import com.tallerwebi.dominio.Usuario.Usuario;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private ServicioUsuarioOAuth servicioUsuarioOAuth;

  @Bean
  @Primary //si hay mas de un codificador , debe usar este por defecto
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public static ClientRegistrationRepository clientRegistrationRepository(
    @Value("${google.client.id}") String clientId,
    @Value("${google.client.secret}") String clientSecret,
    @Value("${app.base.url:http://localhost:8080/spring}") String appBaseUrl
  ) {
    ClientRegistration registration = ClientRegistration
      .withRegistrationId("google")
      .clientId(clientId)
      .clientSecret(clientSecret)
      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
      .redirectUri(appBaseUrl + "/login/oauth2/code/{registrationId}")
      .scope("openid", "profile", "email")
      .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
      .tokenUri("https://www.googleapis.com/oauth2/v4/token")
      .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
      .userNameAttributeName(IdTokenClaimNames.SUB)
      .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
      .clientName("Google")
      .build();
    return new InMemoryClientRegistrationRepository(registration);
  }

  @Override
  public void configure(WebSecurity web) { //excepciones de seguridad
    web.ignoring().antMatchers("/validar-login", "/registrarme");
    //al usar el web ignoring, estas url quedan fuera de las capas de filtro de seguridad, agiliza el login clasico
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf()
      .disable() /// Desactiva la protección CSRF (común en APIs o desarrollo)
      .authorizeRequests()
      .antMatchers("/**")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .formLogin()
      .disable() // Desactiva el formulario de login feo que trae Spring por defecto
      .oauth2Login()
      .loginPage("/login") // Si alguien quiere loguearse, esta es la vista
      .successHandler(oauth2SuccessHandler()) // <-- Qué pasa cuando se loguea bien con Google
      .and()
      .logout()
      .disable();
  }

  @Bean
  public AuthenticationSuccessHandler oauth2SuccessHandler() { //Qué pasa tras loguearse con Google
    return (
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
    ) -> {
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      //Cuando el usuario pone su patrón/contraseña en la ventana flotante de Google y acepta, Google te devuelve un objeto (OAuth2User).
      String email = oAuth2User.getAttribute("email");
      String nombre = oAuth2User.getAttribute("given_name");
      String apellido = oAuth2User.getAttribute("family_name");
      String fotoPerfil = oAuth2User.getAttribute("picture"); // <-- EXTRAEMOS LA FOTO DE GOOGLE
      //Llama a tu servicio personalizado (servicioUsuarioOAuth).
      Usuario usuario = servicioUsuarioOAuth.buscarOCrearUsuario(
        email,
        nombre,
        apellido,
        fotoPerfil
      );
      //Guarda ese objeto Usuario obtenido de la BD en la Sesión de HTTP bajo la clave "USUARIO"
      request.getSession().setAttribute("USUARIO", usuario);
      request.getSession().setAttribute("ROL", "CLIENTE");
      response.sendRedirect("/spring/home");
    };
  }
}
