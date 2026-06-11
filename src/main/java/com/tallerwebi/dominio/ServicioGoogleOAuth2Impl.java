package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.LoginGoogleException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class ServicioGoogleOAuth2Impl implements ServicioGoogleOAuth2 {

  @Autowired
  private ServicioLogin servicioLogin;

  private OidcUserService delegate = new OidcUserService();

  /**
   * Método obligatorio definido en la interfaz OAuth2UserService/OidcUserService.
   * Spring Security lo invoca automáticamente tras obtener el token de usuario del proveedor (Google).
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
