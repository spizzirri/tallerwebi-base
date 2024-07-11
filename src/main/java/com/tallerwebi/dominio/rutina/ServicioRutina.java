package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.presentacion.DatosRutina;

import java.util.List;

public interface ServicioRutina {

    List<DatosRutina> getRutinas ();

    boolean agregarRutina(Rutina rutina) throws RutinaYaExisteException, RutinaInvalidaException;

    boolean agregarEjercicio(Ejercicio ejercicio) throws EjercicioYaExisteException, EjercicioInvalidoException;

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

    List<DatosRutina> getRutinasPorObjetivo(Objetivo objetivo) throws ListaDeRutinasVaciaException;

    DatosRutina getRutinaActualDelUsuario(Usuario usuario) throws UsuarioNoExisteException, RutinaNoEncontradaException;

    void asignarRutinaAUsuario(Rutina rutina, Usuario usuario) throws RutinaYaExisteException;

    void liberarRutinaActivaDelUsuario(Usuario usuario);

    void actualizarEstadoEjercicio(Usuario usuario, Long ejercicioId, EstadoEjercicio.Estado estado);

    List<EstadoEjercicio> getEstadosEjercicios(Usuario usuario, DatosRutina rutina);

    Rendimiento calcularRendimiento(List<EstadoEjercicio> estadosEjercicios);
}
