package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;

public interface ServicioMascota {
  //obtener mascotas de socios
  //agregar mascotas
  void agregarMascota(Socio socio, Mascota mascota, Long planId) throws DatosDeRegistroInvalidos;
  // cambiar plan
  void cambiarPlan(Socio socio, Long mascotaId, Long planId);
  // dar de baja
  void darDeBaja(Socio socio, Long mascotaId);
}
