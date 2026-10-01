package com.tallerwebi.dominio;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioMascota {
  //obtener mascotas de socios
  //agregar mascotas
  // cambiar plan
  void cambiarPlan(Socio socio, Long mascotaId, Long planId);
  // dar de baja
}
