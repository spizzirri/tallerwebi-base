package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.presentacion.DatosRutina;

import java.util.List;

public interface RepositorioRutina {
    List<Rutina> getRutinas ();

    Rutina getRutinaByObjetivo(Objetivo objetivo);

    Rutina getRutinaParaUsuario(Usuario usuario);
}
