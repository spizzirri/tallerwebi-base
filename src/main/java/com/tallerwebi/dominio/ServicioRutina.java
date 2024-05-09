package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioNoEsInstructorException;


public class ServicioRutina {
    public Rutina obtenerRutinaParaUsuario(Usuario usuario) {
        return new Rutina("Rutina test",Objetivo.PERDIDA_DE_PESO);
    }

    public Rutina crearRutina(Usuario usuario,String nombre,Objetivo objetivo) throws UsuarioNoEsInstructorException {
        if (usuario.isInstructor()){
            return new Rutina(nombre,objetivo);
        }else {
            throw new UsuarioNoEsInstructorException();
        }

    }

    public void agregarEjercicioARutina(Usuario usuario, Rutina rutina, Ejercicio ejercicio) throws UsuarioNoEsInstructorException {

        if (usuario.isInstructor()){
            rutina.getEjercicios().add(ejercicio);
        }else {
            throw new UsuarioNoEsInstructorException();
        }
    }
}