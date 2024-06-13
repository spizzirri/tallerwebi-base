package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.UsuarioRutina;
import com.tallerwebi.dominio.objetivo.Objetivo;

import java.util.List;

public interface RepositorioRutina {
    List<Rutina> getRutinas();

    Rutina getRutinaByObjetivo(Objetivo objetivo);

    List<Rutina> getRutinasByObjetivo(Objetivo objetivo);

    Rutina getRutinaParaUsuario(Usuario usuario);

    void guardarRutina(Rutina rutina);

    void actualizar(Rutina rutina);

    void eliminarRutina(Rutina rutina);

    void guardarEjercicio(Ejercicio ejercicio);

    void actualizar(Ejercicio ejercicio);

    void eliminarEjercicio(Ejercicio ejercicio);

    void guardarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina);

    Ejercicio buscarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina);

    void asignarRutinaAUsuario(Rutina rutina, Usuario usuario);

    void guardarUsuario(Usuario usuario);

    List<Rutina> getRutinasDeUsuario(Usuario usuario);

    Rutina buscarRutinaEnUsuario(Rutina rutina, Usuario usuario);

    void eliminarEjercicioDeRutina(Ejercicio ejercicio, Rutina rutina);

    void eliminarRutinaDeUsuario(Rutina rutina, Usuario usuario);

    Ejercicio buscarEjercicioPorId(Long idEjercicio);

    Rutina buscarRutinaPorId(Long idRutina);

    List<Rutina> getRutinasPorObjetivoDeUsuario(Usuario usuario);

    Usuario getUsuarioPorId(Long id);

    List<Ejercicio> getEjerciciosDeRutina(Rutina rutina);

    Rutina getRutinaActualDeLUsuario(Usuario usuario);

    Ejercicio getEjercicioById(Long idEjercicio);

    Rutina getRutinaActivaDelUsuario(Usuario usuario);

    void liberarRutinaActivaDelUsuario(Usuario usuario);

    UsuarioRutina buscarUsuarioRutinaPorUsuarioYRutina(Usuario usuario, Rutina rutina);

    List<UsuarioRutina> buscarListaDeUsuarioRutinaPorUsuarioYRutina(Usuario usuario, Rutina rutina);

    UsuarioRutina buscarUsuarioRutinaActivoPorUsuarioYRutina(Usuario usuario, Rutina rutina);

    UsuarioRutina buscarUltimoUsuarioRutinaInactivoPorUsuarioYRutina(Usuario usuario, Rutina rutina);

    void actualizarUsuarioRutina(UsuarioRutina usuarioRutina);


    void guardarUsuarioRutina(UsuarioRutina usuarioRutina);

    Rutina getUltimaRutinaRealizadaPorUsuario(Usuario usuario);
}
