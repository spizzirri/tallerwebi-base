package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioNoEsInstructorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RutinaTest {

    @Test
    public void SeDebeObtenerRutinaConElObjetivoDelUsuario(){
        Objetivo objetivo = Objetivo.PERDIDA_DE_PESO;

        Usuario usuario = new Usuario();

        usuario.setObjetivo(objetivo);

        ServicioRutina servicioRutina = new ServicioRutina();

        Rutina rutinaObtenida = servicioRutina.obtenerRutinaParaUsuario(usuario);

        assertEquals(objetivo, rutinaObtenida.getObjetivo());

    }

    @Test
    public void QueUnUsuarioInstructorPuedaCrearUnaNuevaRutina() throws UsuarioNoEsInstructorException {

        Usuario usuarioInstructor = new Usuario("Marcos","Lopez", "a@a.com","contrasenia123",true);

        ServicioRutina servicioRutina = new ServicioRutina();

        Rutina nuevaRutina = servicioRutina.crearRutina(usuarioInstructor,"Rutina nueva para adelgazar",Objetivo.PERDIDA_DE_PESO);

        assertNotNull(nuevaRutina);

    }

    @Test
    public void QueUnUsuarioNoPuedaCrearseUnaRutinaSolo(){

        Usuario usuarioNovato = new Usuario("Juan","Perez", "a@a.com","contrasenia123");

        ServicioRutina servicioRutina = new ServicioRutina();

        assertThrowsExactly(UsuarioNoEsInstructorException.class, () ->
                servicioRutina.crearRutina(usuarioNovato, "Rutina nueva para adelgazar", Objetivo.PERDIDA_DE_PESO)
        );

    }

    @Test
    public void QueUnUsuarioInstructorPuedeAgregarEjerciciosALaRutina() throws UsuarioNoEsInstructorException {
        Rutina rutina = new Rutina("Rutina nueva", Objetivo.GANANCIA_MUSCULAR);
        Usuario usuarioInstructor = new Usuario("Marcos","Lopez", "a@a.com","contrasenia123",true);

        Ejercicio ejercicio1 = new Ejercicio("Sentadillas", "Fortalece piernas y glúteos", GrupoMuscularObjetivo.PIERNAS, TipoEjercicio.FUERZA, 3, 12);
        Ejercicio ejercicio2 = new Ejercicio("Press banca", "Desarrolla pectoral", GrupoMuscularObjetivo.PECHO, TipoEjercicio.FUERZA, 4, 8);

        ServicioRutina servicioRutina = new ServicioRutina();

        servicioRutina.agregarEjercicioARutina(usuarioInstructor, rutina , ejercicio1);
        servicioRutina.agregarEjercicioARutina(usuarioInstructor, rutina , ejercicio2);


        assertEquals(2, rutina.getEjercicios().size());
        assertTrue(rutina.getEjercicios().contains(ejercicio1));
        assertTrue(rutina.getEjercicios().contains(ejercicio2));
    }

    @Test
    public void QueUnUsuarioNoPuedaAgregarEjerciciosAUnaRutina() {
        Rutina rutina = new Rutina("Rutina nueva", Objetivo.GANANCIA_MUSCULAR);
        Usuario usuarioNovato = new Usuario("Juan","Perez", "a@a.com","contrasenia123");

        Ejercicio ejercicio1 = new Ejercicio("Sentadillas", "Fortalece piernas y glúteos", GrupoMuscularObjetivo.PIERNAS, TipoEjercicio.FUERZA, 3, 12);

        ServicioRutina servicioRutina = new ServicioRutina();

        assertThrowsExactly(UsuarioNoEsInstructorException.class, () ->
                servicioRutina.agregarEjercicioARutina(usuarioNovato, rutina , ejercicio1)
        );

    }


}
