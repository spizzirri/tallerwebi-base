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

  @Bean
  @Primary
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public static ClientRegistrationRepository clientRegistrationRepository(
    @Value("${google.client.id}") String clientId,
    @Value("${google.client.secret}") String clientSecret
  ) {
    ClientRegistration registration = ClientRegistration
      .withRegistrationId("google")
      .clientId(clientId)
      .clientSecret(clientSecret)
      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
      .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
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
  public void configure(
    org.springframework.security.config.annotation.web.builders.WebSecurity web
  ) {
    web.ignoring().antMatchers("/validar-login", "/registrarme");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf()
      .disable()
      .authorizeRequests()
      .antMatchers("/**")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .formLogin()
      .disable()
      .oauth2Login()
      .loginPage("/login")
      .successHandler(oauth2SuccessHandler())
      .and()
      .logout()
      .disable();
  }

  @Autowired
  private ServicioUsuarioOAuth servicioUsuarioOAuth;

  @Bean
  public AuthenticationSuccessHandler oauth2SuccessHandler() {
    return (
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
    ) -> {
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      String email = oAuth2User.getAttribute("email");
      String nombre = oAuth2User.getAttribute("given_name");
      String apellido = oAuth2User.getAttribute("family_name");
      Usuario usuario = servicioUsuarioOAuth.buscarOCrearUsuario(email, nombre, apellido);
      request.getSession().setAttribute("USUARIO", usuario);
      response.sendRedirect("/spring/home");
    };
  }
}
