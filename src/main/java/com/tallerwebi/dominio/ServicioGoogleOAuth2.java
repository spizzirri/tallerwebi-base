package com.tallerwebi.dominio;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface ServicioGoogleOAuth2 extends OAuth2UserService<OidcUserRequest, OidcUser> {}
