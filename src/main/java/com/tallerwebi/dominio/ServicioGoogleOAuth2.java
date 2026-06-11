package com.tallerwebi.dominio;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interfaz que define el servicio para manejar la autenticación de usuarios mediante
 * Google OAuth2/OpenID Connect. Extiende de {@link OAuth2UserService} para integrar
 * el flujo de carga de usuario de Spring Security.
 */
public interface ServicioGoogleOAuth2 extends OAuth2UserService<OidcUserRequest, OidcUser> {}
