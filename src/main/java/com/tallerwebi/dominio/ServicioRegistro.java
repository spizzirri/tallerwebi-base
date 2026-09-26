package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.SocioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioRegistro {
  void registrarSocio(Socio socio, Mascota mascota, Long planId)
    throws UsuarioExistente, SocioExistente, DatosDeRegistroInvalidos;
}
