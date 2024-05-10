package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.Objetivo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEsInstructorException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioRutinaImpl implements ServicioRutina {
    public Rutina obtenerRutinaParaUsuario(Usuario usuario) {
        return new Rutina("Rutina test", Objetivo.PERDIDA_DE_PESO);
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

    @Override
    public List<Rutina> obtenerRutinas() {
        return List.of();
    }
}