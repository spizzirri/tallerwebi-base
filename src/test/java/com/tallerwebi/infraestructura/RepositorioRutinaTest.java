package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.EjercicioNoExisteEnRutinaException;
import com.tallerwebi.dominio.excepcion.RutinaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoTieneLaRutinaBuscadaException;
import com.tallerwebi.dominio.excepcion.UsuarioSinRutinasException;
import com.tallerwebi.dominio.objetivo.GrupoMuscularObjetivo;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioRutinaTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioRutina repositorioRutina;

    @BeforeEach
    public void init(){
        this.repositorioRutina = new RepositorioRutinaImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaGuardarUnaNuevaRutina(){
        //preparacion
        Rutina rutina = this.crearRutina("Cardio furioso",Objetivo.PERDIDA_DE_PESO);

        //ejecucion
        this.repositorioRutina.guardarRutina(rutina);

        //verificacion
        Rutina rutinaObtenida = (Rutina) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Rutina where nombre = 'Cardio furioso'")
                .getSingleResult();
        assertThat(rutinaObtenida,equalTo(rutina));

    }

    @Test
    @Transactional
    @Rollback
    public void QuesePuedanObtenerRutinasDePerdidaDePeso(){
        //preparacion
        this.crearRutina(anyString(),Objetivo.PERDIDA_DE_PESO);
        this.crearRutina(anyString(),Objetivo.PERDIDA_DE_PESO);
        this.crearRutina(anyString(),Objetivo.GANANCIA_MUSCULAR);

        //ejecucion
        List<Rutina> rutinasObtenidas = this.repositorioRutina.getRutinasByObjetivo(Objetivo.PERDIDA_DE_PESO);

        //verificacion

        assertThat(rutinasObtenidas.size(),equalTo(2));

    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaActualizarElNombreDeUnaRutinaExistente(){
        //preparacion
        Rutina rutina = this.crearRutina("Rutina de cardio corriendo",Objetivo.PERDIDA_DE_PESO);


        //ejecucion
        String nombreEsperado = "Rutina de cardio caminando";
        rutina.setNombre(nombreEsperado);

        this.repositorioRutina.actualizar(rutina);

        //verificacion
        Rutina rutinaObtenida = (Rutina) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Rutina WHERE  nombre = :nombre")
                .setParameter("nombre", nombreEsperado)
                .getSingleResult();

        assertThat(rutinaObtenida.getNombre(),equalTo(nombreEsperado));

    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedanActualizarLasSeriesYRepeticionesDeUnEjercicio(){
        //preparacion
        Ejercicio ejercicio = new Ejercicio("Burpees",Objetivo.PERDIDA_DE_PESO, GrupoMuscularObjetivo.PIERNAS,4,12);


        //ejecucion
        Integer seriesEsperadas = 8;
        Integer repeticionesEsperadas = 10;
        ejercicio.setSeries(seriesEsperadas);
        ejercicio.setRepeticiones(repeticionesEsperadas);
        this.repositorioRutina.actualizar(ejercicio);

        //verificacion
        Ejercicio ejercicioObtenido = (Ejercicio) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Ejercicio WHERE  series = :series AND repeticiones = :repeticiones")
                .setParameter("series", seriesEsperadas)
                .setParameter("repeticiones",repeticionesEsperadas)
                .getSingleResult();

        assertThat(ejercicioObtenido.getSeries(),equalTo(seriesEsperadas));
        assertThat(ejercicioObtenido.getRepeticiones(),equalTo(repeticionesEsperadas));

    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaGuardarUnEjercicioEnUnaRutina(){
        //p
        Rutina rutinaMock = crearRutina("ADELGAZAR",Objetivo.PERDIDA_DE_PESO);
        Ejercicio ejercicioMock = new Ejercicio("Burpees",Objetivo.PERDIDA_DE_PESO, GrupoMuscularObjetivo.PIERNAS,4,12);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        this.repositorioRutina.guardarEjercicio(ejercicioMock);
        this.repositorioRutina.guardarEjercicioEnRutina(ejercicioMock,rutinaMock);

        //v
        Rutina rutinaObtenida = (Rutina) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Rutina r JOIN FETCH r.ejercicios WHERE r.id = :id")
                .setParameter("id", rutinaMock.getIdRutina())
                .getSingleResult();

        assertThat(rutinaObtenida.getEjercicios().get(0), equalTo(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaBuscarUnEjercicioEnUnaRutina() throws EjercicioNoExisteEnRutinaException {
        //p
        Rutina rutinaMock = crearRutina("Volumen",Objetivo.GANANCIA_MUSCULAR);
        Ejercicio ejercicio1 = new Ejercicio("Curl de biceps",Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS,4,12);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        this.repositorioRutina.guardarEjercicio(ejercicio1);
        this.repositorioRutina.guardarEjercicioEnRutina(ejercicio1,rutinaMock);

        //v
        Ejercicio ejercicioObtenido = this.repositorioRutina.buscarEjercicioEnRutina(ejercicio1,rutinaMock);

        assertThat(ejercicioObtenido, equalTo(ejercicio1));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSeBusqueYNoSeEncuentreElEjercicioEnUnaRutina() {
        //p
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);
        Ejercicio ejercicio1 = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);
        Ejercicio ejercicio2 = new Ejercicio("Burpees", Objetivo.PERDIDA_DE_PESO, GrupoMuscularObjetivo.PIERNAS, 4, 12);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        this.repositorioRutina.guardarEjercicio(ejercicio1);
        this.repositorioRutina.guardarEjercicioEnRutina(ejercicio1, rutinaMock);

        Ejercicio ejercicioObtenido = this.repositorioRutina.buscarEjercicioEnRutina(ejercicio2, rutinaMock);

        //v
        assertThat(ejercicioObtenido,equalTo(null));


    }

    @Transactional
    @Rollback
    @Test
    public void QueSePuedaAsignarUnaRutinaAUnUsuario(){
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaPecho = crearRutina("Rutina de volumen - PECHO",Objetivo.GANANCIA_MUSCULAR);

        //e
        // Persistir usuario, rutina y asignar rutina al usuario
        repositorioRutina.asignarRutinaAUsuario(rutinaPecho, usuario);

        // Verificar que la rutina se haya asignado correctamente al usuario
        Rutina rutinaObtenida = (Rutina) sessionFactory.getCurrentSession()
                .createQuery("SELECT r FROM Usuario u JOIN u.rutinas r WHERE u.id = :idUsuario AND r.id = :idRutina")
                .setParameter("idUsuario", usuario.getId())
                .setParameter("idRutina", rutinaPecho.getIdRutina())
                .getSingleResult();

        assertThat(rutinaObtenida, equalTo(rutinaPecho));
    }

    @Transactional
    @Rollback
    @Test
    public void QueSePuedaSaberQueRutinasTieneElUsuario() throws  UsuarioSinRutinasException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaPecho = crearRutina("Rutina de volumen - PECHO",Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaBiceps = crearRutina("Rutina de volumen - BICEPS",Objetivo.GANANCIA_MUSCULAR);

        //e
        this.repositorioRutina.guardarRutina(rutinaPecho);
        this.repositorioRutina.guardarRutina(rutinaBiceps);
        this.repositorioRutina.asignarRutinaAUsuario(rutinaPecho,usuario);
        this.repositorioRutina.asignarRutinaAUsuario(rutinaBiceps,usuario);

        List<Rutina> rutinasEsperadas = new ArrayList<>();
        rutinasEsperadas.add(rutinaPecho);
        rutinasEsperadas.add(rutinaBiceps);

        //v
        List<Rutina> rutinasObtenidas = this.repositorioRutina.getRutinasDeUsuario(usuario);
        assertThat(rutinasEsperadas, equalTo(rutinasObtenidas));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaBuscarUnaRutinaEnUnUsuario() throws UsuarioNoTieneLaRutinaBuscadaException {
        //p
        Rutina rutinaMock = crearRutina("Volumen",Objetivo.GANANCIA_MUSCULAR);
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        this.repositorioRutina.guardarUsuario(usuarioMock);
        this.repositorioRutina.asignarRutinaAUsuario(rutinaMock,usuarioMock);

        //v
        Rutina rutinaObtenida = this.repositorioRutina.buscarRutinaEnUsuario(rutinaMock,usuarioMock);

        assertThat(rutinaObtenida, equalTo(rutinaMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueAlBuscarUnaRutinaEnUnUsuarioYNoEsteArrojeExcepcion() {
        //p
        Rutina rutinaMock = crearRutina("Volumen",Objetivo.GANANCIA_MUSCULAR);
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        this.repositorioRutina.guardarUsuario(usuarioMock);
        Rutina rutinaBuscada = this.repositorioRutina.buscarRutinaEnUsuario(rutinaMock,usuarioMock);

        //v
        assertThat(rutinaBuscada,equalTo(null));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaBuscarUnaRutinaPorSuId() {
        //p
        Rutina rutinaMock = crearRutina("Volumen",Objetivo.GANANCIA_MUSCULAR);

        //e
        this.repositorioRutina.guardarRutina(rutinaMock);
        Rutina rutinaObtenida = this.repositorioRutina.buscarRutinaPorId(rutinaMock.getIdRutina());

        //v
        assertThat(rutinaObtenida, equalTo(rutinaMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaBuscarUnUsuarioPorSuId() {
        //p
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);

        //e
        this.repositorioRutina.guardarUsuario(usuarioMock);

        Usuario usuarioObtenido = this.repositorioRutina.getUsuarioPorId(usuarioMock.getId());

        //v
        assertThat(usuarioObtenido, equalTo(usuarioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaObtenerLasRutinasQueLeInteresanAlUsuarioSegunSuObjetivo()  {
        //p
        Rutina rutinaVolumen = crearRutina("Volumen",Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaCardio1= crearRutina("Rutina de cardio - caminar",Objetivo.PERDIDA_DE_PESO);
        Rutina rutinaCardio2 = crearRutina("Rutina de cardio - Correr",Objetivo.PERDIDA_DE_PESO);
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);


        //e
        this.repositorioRutina.guardarRutina(rutinaVolumen);
        this.repositorioRutina.guardarRutina(rutinaCardio1);
        this.repositorioRutina.guardarRutina(rutinaCardio2);


        List<Rutina> rutinasObtenidas = this.repositorioRutina.getRutinasPorObjetivoDeUsuario(usuarioMock);

        //v
        assertThat(rutinasObtenidas.size(), equalTo(2));
        assertTrue(rutinasObtenidas.contains(rutinaCardio1));
        assertTrue(rutinasObtenidas.contains(rutinaCardio2));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaEliminarEjercicioDeRutina() {
        // Preparación
        Rutina rutina = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);
        Ejercicio ejercicio = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);
        repositorioRutina.guardarRutina(rutina);
        repositorioRutina.guardarEjercicio(ejercicio);
        repositorioRutina.guardarEjercicioEnRutina(ejercicio, rutina);

        // Ejecución
        repositorioRutina.eliminarEjercicioDeRutina(ejercicio, rutina);

        // Verificación
        Ejercicio ejercicioObtenido = repositorioRutina.buscarEjercicioEnRutina(ejercicio, rutina);
        assertNull(ejercicioObtenido);

        List<Ejercicio> ejerciciosDeRutina = this.repositorioRutina.getEjerciciosDeRutina(rutina);
        assertFalse(ejerciciosDeRutina.contains(ejercicio));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaEliminarRutinaDeUsuario() {
        // Preparación
        Usuario usuario = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);
        repositorioRutina.guardarUsuario(usuario);
        repositorioRutina.guardarRutina(rutina);
        repositorioRutina.asignarRutinaAUsuario(rutina, usuario);

        // Ejecución
        repositorioRutina.eliminarRutinaDeUsuario(rutina, usuario);

        // Verificación
        Rutina rutinaObtenida = repositorioRutina.buscarRutinaEnUsuario(rutina, usuario);
        assertNull(rutinaObtenida);

        List<Rutina> rutinas = repositorioRutina.getRutinasDeUsuario(usuario);
        assertFalse(rutinas.contains(rutina));
    }

    public Rutina crearRutina(String nombre, Objetivo objetivo){
        Rutina rutina = new Rutina(nombre,objetivo);
        this.sessionFactory.getCurrentSession().save(rutina);
        return rutina;
    }

}
