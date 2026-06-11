package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.LoginGoogleException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link ServicioGoogleOAuth2} que gestiona la carga y registro
 * automático de usuarios obtenidos a través de Google OAuth2.
 */
@Service
public class ServicioGoogleOAuth2Impl implements ServicioGoogleOAuth2 {

  @Autowired
  private ServicioLogin servicioLogin;

  private OidcUserService delegate = new OidcUserService();

  /**
   * Carga la información del usuario desde Google, verifica si existe en la base de datos
   * y, en caso contrario, realiza el registro automático del nuevo usuario.
   *
   * @param userRequest el objeto que contiene la solicitud de información del usuario OAuth2
   * @return el objeto {@link OidcUser} con la información autenticada
   * @throws LoginGoogleException si ocurre un error durante el proceso de registro o si el usuario ya existe
   */
  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) {
    // Delegamos la carga inicial del usuario al servicio OIDC de Spring Security
    OidcUser oidcUser = delegate.loadUser(userRequest);
    String email = oidcUser.getAttribute("email");

    // Buscamos si el usuario ya existe en nuestra base de datos
    Usuario usuario = servicioLogin.buscar(email);

    // Si el usuario no existe, lo registramos automáticamente con credenciales para Google
    if (usuario == null) {
      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setEmail(email);
      nuevoUsuario.setActivo(true);
      nuevoUsuario.setRol("USER");
      nuevoUsuario.setPassword("GOOGLE_AUTH");
      try {
        servicioLogin.registrar(nuevoUsuario);
      } catch (UsuarioExistente e) {
        throw new LoginGoogleException("El usuario ya existe: " + email, e);
      } catch (Exception e) {
        throw new LoginGoogleException("Error al registrar usuario: " + email, e);
      }
    }

    return oidcUser;
  }
}
