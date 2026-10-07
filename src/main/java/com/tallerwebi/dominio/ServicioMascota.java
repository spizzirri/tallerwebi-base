package com.tallerwebi.dominio;

public interface ServicioMascota {
  //obtener mascotas de socios
  //agregar mascotas
  // cambiar plan
  void cambiarPlan(Socio socio, Long mascotaId, Long planId);
  // dar de baja
  void darDeBaja(Socio socio, Long mascotaId);
}
