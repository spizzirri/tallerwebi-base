package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.presentacion.DatosRutina;

import java.util.List;

public interface ServicioRutina {

    List<DatosRutina> getRutinas ();

    boolean agregarRutina(Rutina rutina) throws RutinaYaExisteException;

    boolean agregarEjercicio(Ejercicio ejercicio) throws EjercicioYaExisteException;

    boolean eliminarRutina(Rutina rutina) throws RutinaNoEncontradaException;

    boolean eliminarEjercicio(Ejercicio ejercicio) throws EjercicioNoEncontradoException;

    DatosRutina getRutinaByObjetivo(Objetivo objetivo);

    DatosRutina getRutinaParaUsuario(Usuario usuario);

    List<DatosRutina> getRutinasDeUsuario(Usuario usuario) throws UsuarioSinRutinasException;

    boolean validarObjetivosDeUsuarioYRutina(Usuario usuario, Rutina rutina) throws DiferenciaDeObjetivosExcepcion, UsuarioNoExisteException, RutinaNoEncontradaException;

    List<DatosRutina> getRutinasPorObjetivoDeUsuario(Usuario usuario);

    Usuario getUsuarioById(Long id) throws UsuarioNoExisteException;

    Rutina getRutinaById(Long idRutina) throws RutinaNoEncontradaException;
    DatosRutina getDatosRutinaById(Long idRutina) throws RutinaNoEncontradaException;

    Ejercicio getEjercicioById(Long idEjercicio) throws EjercicioNoEncontradoException;

    boolean existeRutinaEnUsuario(Rutina rutina, Usuario usuario);
    boolean existeEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina);

    List<DatosRutina> getRutinasPorObjetivo(Objetivo objetivo);

    DatosRutina getRutinaActualDelUsuario(Usuario usuario) throws UsuarioNoExisteException;

    void asignarRutinaAUsuario(Rutina rutina, Usuario usuario) throws RutinaYaExisteException;

    void liberarRutinaActivaDelUsuario(Usuario usuario);
}
