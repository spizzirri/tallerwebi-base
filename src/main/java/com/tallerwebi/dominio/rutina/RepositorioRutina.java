package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.EjercicioNoExisteEnRutinaException;
import com.tallerwebi.dominio.excepcion.RutinaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoTieneLaRutinaBuscadaException;
import com.tallerwebi.dominio.excepcion.UsuarioSinRutinasException;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.presentacion.DatosRutina;

import java.util.List;

public interface RepositorioRutina {
    List<Rutina> getRutinas ();

    Rutina getRutinaByObjetivo(Objetivo objetivo);

    List<Rutina> getRutinasByObjetivo(Objetivo objetivo);

    Rutina getRutinaParaUsuario(Usuario usuario);

    void guardarRutina(Rutina rutina);
    void guardarEjercicio(Ejercicio ejercicio);

    void guardarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina);

    void actualizar(Rutina rutina);

    void actualizar(Ejercicio ejercicio);

    Ejercicio buscarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina) throws EjercicioNoExisteEnRutinaException;

    void asignarRutinaAUsuario(Rutina rutina, Usuario usuario);

    void guardarUsuario(Usuario usuario);

    List<Rutina> getRutinasDeUsuario(Usuario usuario) throws  UsuarioSinRutinasException;

    Rutina buscarRutinaEnUsuario(Rutina rutina, Usuario usuario) throws UsuarioNoTieneLaRutinaBuscadaException;

    Rutina buscarRutinaPorId(Long idRutina) throws RutinaNoEncontradaException;

    List<Rutina> getRutinasPorObjetivoDeUsuario(Usuario usuario);

    Usuario getUsuarioPorId(Long id);
}
