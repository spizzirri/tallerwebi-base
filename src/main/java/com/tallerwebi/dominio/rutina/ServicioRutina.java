package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.presentacion.DatosRutina;

import java.util.List;

public interface ServicioRutina {


    List<DatosRutina> getRutinas ();

    DatosRutina getRutinaByObjetivo(Objetivo objetivo);

    DatosRutina getRutinaParaUsuario(Usuario usuario);



}
