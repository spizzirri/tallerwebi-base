package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoCumpleRequisitos;

public interface ServicioAutenticacion {
    boolean esUsuarioValido(Usuario usuario) throws NoCumpleRequisitos;
}
