package com.tallerwebi.dominio.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("googleOAuth2UserService")
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2UserService.class);

  @Autowired
  private RepositorioUsuario repositorioUsuario;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String email = oAuth2User.getAttribute("email");
    Usuario usuario = repositorioUsuario.buscarUsuarioPorEmail(email);

    LOGGER.info("=== OAUTH2 === email: {}", email);
    LOGGER.info("=== OAUTH2 === usuario encontrado: {}", usuario);

    if (usuario == null) {
      LOGGER.info("=== OAUTH2 === creando usuario nuevo...");
      String nombre = oAuth2User.getAttribute("given_name");
      String apellido = oAuth2User.getAttribute("family_name");

      usuario = new Usuario();
      usuario.setEmail(email);
      usuario.setNombre(nombre);
      usuario.setApellido(apellido);
      usuario.setRol("CLIENTE");
      usuario.setActivo(true);
      repositorioUsuario.guardar(usuario);
      LOGGER.info("=== OAUTH2 === usuario guardado");
    }

    return oAuth2User;
  }

  public void setRepositorioUsuario(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }
}
