package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.rutina.EstadoEjercicio;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.usuario.UsuarioRutina;
import com.tallerwebi.dominio.excepcion.EjercicioNoExisteEnRutinaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoTieneLaRutinaBuscadaException;
import com.tallerwebi.dominio.excepcion.UsuarioSinRutinasException;
import com.tallerwebi.dominio.objetivo.GrupoMuscularObjetivo;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import com.tallerwebi.presentacion.DatosRutina;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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
        this.limpiarBaseDeDatos();
    }

    public void limpiarBaseDeDatos() {
        Session session = sessionFactory.getCurrentSession();
        session.createQuery("DELETE FROM UsuarioRutina").executeUpdate();
        session.createQuery("DELETE FROM Rutina").executeUpdate();
        session.createQuery("DELETE FROM Usuario").executeUpdate();
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaGuardarUnaNuevaRutina(){
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
    public void queSePuedaGuardarUnUsuario(){
        //preparacion
        Usuario usuario = new Usuario("Lautaro",Objetivo.GANANCIA_MUSCULAR);

        //ejecucion
        this.repositorioRutina.guardarUsuario(usuario);

        //verificacion
        Usuario usuarioObtenido = (Usuario) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Usuario where nombre = 'Lautaro' AND objetivo = 'GANANCIA_MUSCULAR'")
                .getSingleResult();
        assertThat(usuarioObtenido,equalTo(usuario));

    }

    @Test
    @Transactional
    @Rollback
    public void quesePuedanObtenerRutinasDePerdidaDePeso() {
        // Preparación
        Rutina rutina1 = this.crearRutina("Rutina Cardio 1", Objetivo.PERDIDA_DE_PESO);
        Rutina rutina2 = this.crearRutina("Rutina Cardio 2", Objetivo.PERDIDA_DE_PESO);
        Rutina rutina3 = this.crearRutina("Rutina Volumen 1", Objetivo.GANANCIA_MUSCULAR);

        // Persistir rutinas
        sessionFactory.getCurrentSession().save(rutina1);
        sessionFactory.getCurrentSession().save(rutina2);
        sessionFactory.getCurrentSession().save(rutina3);

        // Ejecución
        List<Rutina> rutinasObtenidas = this.repositorioRutina.getRutinasByObjetivo(Objetivo.PERDIDA_DE_PESO);

        // Verificación
        assertThat(rutinasObtenidas.size(), equalTo(2));
        assertThat(rutinasObtenidas, containsInAnyOrder(rutina1, rutina2));
    }

    @Test
    @Transactional
    @Rollback
    public void quesePuedanObtenerLosEjerciciosQueTieneUnaRutina() {
        // Preparación
        Rutina rutinaMock = this.crearRutina("Rutina Cardio 1", Objetivo.PERDIDA_DE_PESO);
        Ejercicio ejercicioMock = new Ejercicio("Burpees",Objetivo.PERDIDA_DE_PESO, GrupoMuscularObjetivo.PIERNAS,4,12);
        Ejercicio ejercicioMock2 = new Ejercicio("Burpees",Objetivo.PERDIDA_DE_PESO, GrupoMuscularObjetivo.PIERNAS,4,12);

        sessionFactory.getCurrentSession().save(rutinaMock);
        sessionFactory.getCurrentSession().save(ejercicioMock);
        sessionFactory.getCurrentSession().save(ejercicioMock2);

        this.repositorioRutina.guardarEjercicioEnRutina(ejercicioMock,rutinaMock);
        this.repositorioRutina.guardarEjercicioEnRutina(ejercicioMock2,rutinaMock);

        // Ejecución
        List<Ejercicio> ejerciciosObtenidos = this.repositorioRutina.getEjerciciosDeRutina(rutinaMock);

        // Verificación
        assertThat(ejerciciosObtenidos.size(), equalTo(2));
        assertThat(ejerciciosObtenidos, containsInAnyOrder(ejercicioMock, ejercicioMock2));
    }



    @Test
    @Transactional
    @Rollback
    public void queSePuedaActualizarElNombreDeUnaRutinaExistente(){
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
    public void queSePuedaEstablecerObjetivoDeUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        Objetivo nuevoObjetivo = Objetivo.GANANCIA_MUSCULAR;

        // Ejecución
        repositorioRutina.setObjetivoUsuario(nuevoObjetivo, usuarioMock);

        // Verificación
        assertEquals(nuevoObjetivo, usuarioMock.getObjetivo());
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedanActualizarLasSeriesYRepeticionesDeUnEjercicio(){
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
    public void queSePuedaGuardarUnEjercicioEnUnaRutina(){
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
    public void queSePuedaBuscarUnEjercicioEnUnaRutina() throws EjercicioNoExisteEnRutinaException {
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
    public void queSeBusqueYNoSeEncuentreElEjercicioEnUnaRutina() {
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
    public void queSePuedaAsignarUnaRutinaAUnUsuario() {
        // Preparación
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaPecho = crearRutina("Rutina de volumen - PECHO", Objetivo.GANANCIA_MUSCULAR);

        // Persistir usuario y rutina
        sessionFactory.getCurrentSession().save(usuario);
        sessionFactory.getCurrentSession().save(rutinaPecho);

        // Ejecución: Asignar rutina al usuario
        repositorioRutina.asignarRutinaAUsuario(rutinaPecho, usuario);

        // Verificación: Verificar que la rutina se haya asignado correctamente al usuario
        UsuarioRutina usuarioRutinaObtenida = (UsuarioRutina) sessionFactory.getCurrentSession()
                .createQuery("SELECT ur FROM UsuarioRutina ur WHERE ur.usuario.id = :idUsuario AND ur.rutina.id = :idRutina")
                .setParameter("idUsuario", usuario.getId())
                .setParameter("idRutina", rutinaPecho.getIdRutina())
                .getSingleResult();

        assertNotNull(usuarioRutinaObtenida);
        assertThat(usuarioRutinaObtenida.getRutina(), equalTo(rutinaPecho));
        assertThat(usuarioRutinaObtenida.getUsuario(), equalTo(usuario));
    }


    @Transactional
    @Rollback
    @Test
    public void queSePuedaObtenerLasRutinasQueTieneAsignadasUnUsuario() throws UsuarioSinRutinasException {
        // Preparación
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaPecho = crearRutina("Rutina de volumen - PECHO", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaBiceps = crearRutina("Rutina de volumen - BICEPS", Objetivo.GANANCIA_MUSCULAR);

        // Persistir usuario
        sessionFactory.getCurrentSession().save(usuario);

        // Ejecución
        this.repositorioRutina.guardarRutina(rutinaPecho);
        this.repositorioRutina.guardarRutina(rutinaBiceps);
        this.repositorioRutina.asignarRutinaAUsuario(rutinaPecho, usuario);
        this.repositorioRutina.asignarRutinaAUsuario(rutinaBiceps, usuario);

        List<Rutina> rutinasEsperadas = new ArrayList<>();
        rutinasEsperadas.add(rutinaPecho);
        rutinasEsperadas.add(rutinaBiceps);

        // Verificación
        List<Rutina> rutinasObtenidas = this.repositorioRutina.getRutinasDeUsuario(usuario);
        assertThat(rutinasObtenidas, equalTo(rutinasEsperadas));
    }


    @Test
    @Transactional
    @Rollback
    public void queSePuedaSaberSiUnaRutinaFueAsignadaAUnUsuario() throws UsuarioNoTieneLaRutinaBuscadaException {
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
    public void queAlBuscarUnaRutinaEnUnUsuarioYNoEsteArrojeExcepcion() {
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
    public void queSePuedaBuscarUnaRutinaPorSuId() {
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
    public void queSePuedaBuscarUnEjercicioPorSuId() {
        //p
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);

        //e
        this.repositorioRutina.guardarEjercicio(ejercicioMock);
        Ejercicio ejercicioObtenido = this.repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio());

        //v
        assertThat(ejercicioObtenido, equalTo(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerUnUsuarioPorSuId() {
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
    public void queSePuedanObtenerTodasLasRutinasConElMismoObjetivoQueElUsuarioTiene()  {
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
    public void queSePuedaEliminarUnEjercicioDeUnaRutina() {
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
    public void queSePuedaEliminarUnaRutinaDeUnUsuario() {
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

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerLaRutinaActivaDelUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Ejecución
        Rutina rutinaObtenida = repositorioRutina.getRutinaActualDelUsuario(usuarioMock);

        // Verificación
        assertNotNull(rutinaObtenida);
        assertThat(rutinaObtenida, equalTo(rutinaMock));
    }


    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerUnaListaDeRutinasConObjetivoDefinicion() {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina def 1",Objetivo.DEFINICION);
        Rutina rutinaMock2 = new Rutina("Rutina def 2",Objetivo.DEFINICION);
        Objetivo objetivoMock = Objetivo.DEFINICION;
        this.sessionFactory.getCurrentSession().save(rutinaMock);
        this.sessionFactory.getCurrentSession().save(rutinaMock2);

        // Ejecución
        List<Rutina> rutinasMock = repositorioRutina.getRutinasByObjetivo(objetivoMock);

        // Verificación


        assertEquals(rutinasMock.size(),2);
        assertTrue(rutinasMock.contains(rutinaMock));
        assertTrue(rutinasMock.contains(rutinaMock2));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaGuardarUnaRelacionUsuarioRutinaTeniendoUnUsuarioYUnaRutina(){
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);

        // Crear una relación UsuarioRutina
        UsuarioRutina usuarioRutina = new UsuarioRutina();
        usuarioRutina.setUsuario(usuarioMock);
        usuarioRutina.setRutina(rutinaMock);
        usuarioRutina.setFechaInicio(new Date());
        usuarioRutina.setActivo(true);

        // Ejecución
        repositorioRutina.guardarUsuarioRutina(usuarioRutina);

        // Verificación
        UsuarioRutina usuarioRutinaObtenida = repositorioRutina.buscarUsuarioRutinaPorUsuarioYRutina(usuarioMock, rutinaMock);
        assertNotNull(usuarioRutinaObtenida);
        assertThat(usuarioRutinaObtenida.getUsuario(), equalTo(usuarioMock));
        assertThat(usuarioRutinaObtenida.getRutina(), equalTo(rutinaMock));
        assertTrue(usuarioRutinaObtenida.isActivo());
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaLiberarLaRutinaActivaDelUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Verificación inicial: la rutina debe estar activa
        Rutina rutinaActivaInicial = repositorioRutina.getRutinaActualDelUsuario(usuarioMock);
        assertNotNull(rutinaActivaInicial);
        assertThat(rutinaActivaInicial, equalTo(rutinaMock));

        // Ejecución
        repositorioRutina.liberarRutinaActivaDelUsuario(usuarioMock);

        // Verificación: la rutina ya no debe estar activa
        Rutina rutinaActivaPosterior = repositorioRutina.getRutinaActualDelUsuario(usuarioMock);
        assertNull(rutinaActivaPosterior);
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaBuscarUnaRelacionUsuarioRutinaPorUsuarioYRutina() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Ejecución
        UsuarioRutina usuarioRutinaObtenida = repositorioRutina.buscarUsuarioRutinaPorUsuarioYRutina(usuarioMock, rutinaMock);

        // Verificación
        assertNotNull(usuarioRutinaObtenida);
        assertThat(usuarioRutinaObtenida.getUsuario(), equalTo(usuarioMock));
        assertThat(usuarioRutinaObtenida.getRutina(), equalTo(rutinaMock));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaActualizarElEstadoDeUnEjercicioDeUnaRutinaRelacionadaConUnUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);

        // Guardar usuario y ejercicio
        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarEjercicio(ejercicioMock);

        // Crear estado inicial del ejercicio para el usuario
        EstadoEjercicio estadoInicial = new EstadoEjercicio(usuarioMock, ejercicioMock, EstadoEjercicio.Estado.NO_EMPEZADO);
        repositorioRutina.guardarEstadoEjercicio(estadoInicial);

        // Verificar el estado inicial
        EstadoEjercicio estadoEjercicioObtenido = repositorioRutina.buscarEstadoEjercicioPorUsuarioYEjercicio(usuarioMock, ejercicioMock);
        assertNotNull(estadoEjercicioObtenido);
        assertThat(estadoEjercicioObtenido.getEstado(), equalTo(EstadoEjercicio.Estado.NO_EMPEZADO));

        // Ejecución: Actualizar estado del ejercicio
        repositorioRutina.actualizarEstadoEjercicio(usuarioMock, ejercicioMock.getIdEjercicio(), EstadoEjercicio.Estado.COMPLETO);

        // Refrescar la sesión para asegurarse de obtener los datos actualizados
        sessionFactory.getCurrentSession().flush();

        // Verificación: Obtener el estado actualizado
        EstadoEjercicio estadoEjercicioActualizado = repositorioRutina.buscarEstadoEjercicioPorUsuarioYEjercicio(usuarioMock, ejercicioMock);

        assertNotNull(estadoEjercicioActualizado);
        assertThat(estadoEjercicioActualizado.getEstado(), equalTo(EstadoEjercicio.Estado.COMPLETO));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerUnaListaDeRelacionesUsuarioRutinaEntreUnUsuarioYUnaRutina() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Crear una segunda relación con otra fecha de inicio para simular múltiples relaciones
        UsuarioRutina segundaRelacion = new UsuarioRutina();
        segundaRelacion.setUsuario(usuarioMock);
        segundaRelacion.setRutina(rutinaMock);
        segundaRelacion.setFechaInicio(new Date());
        segundaRelacion.setActivo(true);
        sessionFactory.getCurrentSession().save(segundaRelacion);

        // Ejecución
        List<UsuarioRutina> listaUsuarioRutinaObtenida = repositorioRutina.buscarListaDeUsuarioRutinaPorUsuarioYRutina(usuarioMock, rutinaMock);

        // Verificación
        assertNotNull(listaUsuarioRutinaObtenida);
        assertFalse(listaUsuarioRutinaObtenida.isEmpty());
        assertEquals(2, listaUsuarioRutinaObtenida.size());
        assertThat(listaUsuarioRutinaObtenida, hasItems(
                allOf(
                        hasProperty("usuario", equalTo(usuarioMock)),
                        hasProperty("rutina", equalTo(rutinaMock))
                )
        ));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaBuscarLaRelacionUsuarioRutinaActivaPasandoPorParametroUnUsuarioYUnaRutina() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutinaMock = crearRutina("Volumen", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarRutina(rutinaMock);
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Crear una segunda relación y marcarla como inactiva
        UsuarioRutina segundaRelacion = new UsuarioRutina();
        segundaRelacion.setUsuario(usuarioMock);
        segundaRelacion.setRutina(rutinaMock);
        segundaRelacion.setFechaInicio(new Date());
        segundaRelacion.setActivo(false);
        sessionFactory.getCurrentSession().save(segundaRelacion);

        // Ejecución
        UsuarioRutina usuarioRutinaActivaObtenida = repositorioRutina.buscarUsuarioRutinaActivoPorUsuarioYRutina(usuarioMock, rutinaMock);

        // Verificación
        assertNotNull(usuarioRutinaActivaObtenida);
        assertThat(usuarioRutinaActivaObtenida.getUsuario(), equalTo(usuarioMock));
        assertThat(usuarioRutinaActivaObtenida.getRutina(), equalTo(rutinaMock));
        assertTrue(usuarioRutinaActivaObtenida.isActivo());
    }


    @Test
    @Transactional
    @Rollback
    public void queSePuedaActualizarUnaRelacionUsuarioRutina() {
        // Preparación
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = crearRutina("Rutina de volumen - PECHO", Objetivo.GANANCIA_MUSCULAR);

        // Persistir usuario y rutina
        sessionFactory.getCurrentSession().save(usuario);
        sessionFactory.getCurrentSession().save(rutina);

        // Asignar rutina al usuario
        UsuarioRutina usuarioRutina = new UsuarioRutina();
        usuarioRutina.setUsuario(usuario);
        usuarioRutina.setRutina(rutina);
        usuarioRutina.setActivo(true);
        usuarioRutina.setFechaInicio(new Date());
        sessionFactory.getCurrentSession().save(usuarioRutina);

        // Verificar que la relación se haya asignado correctamente
        UsuarioRutina usuarioRutinaObtenida = this.repositorioRutina.buscarUsuarioRutinaPorUsuarioYRutina(usuario, rutina);
        assertNotNull(usuarioRutinaObtenida);
        assertTrue(usuarioRutinaObtenida.isActivo());

        // Actualizar la relación
        usuarioRutinaObtenida.setActivo(false);
        this.repositorioRutina.actualizarUsuarioRutina(usuarioRutinaObtenida);

        // Verificar que la relación se haya actualizado correctamente
        UsuarioRutina usuarioRutinaActualizada = this.repositorioRutina.buscarUsuarioRutinaPorUsuarioYRutina(usuario, rutina);
        assertNotNull(usuarioRutinaActualizada);
        assertFalse(usuarioRutinaActualizada.isActivo());
    }
    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerListaDeEstadosDeEjercicioPorUsuarioYRutina() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        Ejercicio ejercicioMock1 = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);
        Ejercicio ejercicioMock2 = new Ejercicio("Press de banca", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.PECHO, 3, 10);

        // Guardar usuario y ejercicios
        repositorioRutina.guardarUsuario(usuarioMock);
        repositorioRutina.guardarEjercicio(ejercicioMock1);
        repositorioRutina.guardarEjercicio(ejercicioMock2);

        // Crear y guardar rutina
        Rutina rutinaMock = new Rutina("Volumen", Objetivo.GANANCIA_MUSCULAR);
        repositorioRutina.guardarRutina(rutinaMock);

        // Asignar ejercicios a la rutina
        repositorioRutina.guardarEjercicioEnRutina(ejercicioMock1, rutinaMock);
        repositorioRutina.guardarEjercicioEnRutina(ejercicioMock2, rutinaMock);

        // Asignar rutina al usuario
        repositorioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Verificación de que los estados de ejercicios se inicializaron
        List<EstadoEjercicio> estadosEjercicios = repositorioRutina.getEstadoEjercicioList(usuarioMock, new DatosRutina(rutinaMock.getIdRutina(), rutinaMock.getNombre(), rutinaMock.getObjetivo(), rutinaMock.getEjercicios()));

        // Verificación
        assertNotNull(estadosEjercicios);
        assertEquals(2, estadosEjercicios.size());

        // Verificar que los estados de los ejercicios se inicializaron correctamente
        assertTrue(estadosEjercicios.stream().anyMatch(estado -> estado.getEjercicio().equals(ejercicioMock1) && estado.getEstado().equals(EstadoEjercicio.Estado.NO_EMPEZADO)));
        assertTrue(estadosEjercicios.stream().anyMatch(estado -> estado.getEjercicio().equals(ejercicioMock2) && estado.getEstado().equals(EstadoEjercicio.Estado.NO_EMPEZADO)));
    }


    @Test
    @Transactional
    @Rollback
    public void queSePuedaSaberCualFueLaUltimaRelacionUsuarioRutinaQueRealizoElUsuario() {
        // Preparación
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina1 = new Rutina("Rutina de volumen - PECHO", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina2 = new Rutina("Rutina de volumen - BICEPS", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina3 = new Rutina("Rutina de volumen - ESPALDA", Objetivo.GANANCIA_MUSCULAR);

        this.repositorioRutina.guardarUsuario(usuario);
        this.repositorioRutina.guardarRutina(rutina1);
        this.repositorioRutina.guardarRutina(rutina2);
        this.repositorioRutina.guardarRutina(rutina3);

        UsuarioRutina usuarioRutina1 = new UsuarioRutina(usuario, rutina1);
        usuarioRutina1.setActivo(false);
        usuarioRutina1.setFechaInicio(Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        UsuarioRutina usuarioRutina2 = new UsuarioRutina(usuario, rutina2);
        usuarioRutina2.setActivo(false);
        usuarioRutina2.setFechaInicio(Date.from(LocalDate.now().minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        UsuarioRutina usuarioRutina3 = new UsuarioRutina(usuario, rutina3);
        usuarioRutina3.setActivo(false);
        usuarioRutina3.setFechaInicio(Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        UsuarioRutina usuarioRutina4 = new UsuarioRutina(usuario, rutina3);
        usuarioRutina3.setActivo(false);
        usuarioRutina3.setFechaInicio(Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        this.repositorioRutina.guardarUsuarioRutina(usuarioRutina1);
        this.repositorioRutina.guardarUsuarioRutina(usuarioRutina2);
        this.repositorioRutina.guardarUsuarioRutina(usuarioRutina3);
        this.repositorioRutina.guardarUsuarioRutina(usuarioRutina4);

        // Ejecución
        UsuarioRutina usuarioRutinaInactiva = this.repositorioRutina.buscarUltimoUsuarioRutinaInactivoPorUsuarioYRutina(usuario, rutina3);

        // Verificación
        assertNotNull(usuarioRutinaInactiva);
        assertThat(usuarioRutinaInactiva.getRutina(), equalTo(rutina3));
    }

    @Test
    @Transactional
    public void queSePuedaObtenerUltimaRutinaRealizadaPorUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        repositorioRutina.guardarUsuario(usuarioMock);

        Rutina rutina1 = new Rutina("Rutina 1", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina2 = new Rutina("Rutina 2", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina3 = new Rutina("Rutina 3", Objetivo.GANANCIA_MUSCULAR);

        repositorioRutina.guardarRutina(rutina1);
        repositorioRutina.guardarRutina(rutina2);
        repositorioRutina.guardarRutina(rutina3);

        UsuarioRutina usuarioRutina1 = new UsuarioRutina(usuarioMock, rutina1, new Date(1000000000000L), false);
        UsuarioRutina usuarioRutina2 = new UsuarioRutina(usuarioMock, rutina2, new Date(2000000000000L), false);
        UsuarioRutina usuarioRutina3 = new UsuarioRutina(usuarioMock, rutina3, new Date(3000000000000L), false);

        sessionFactory.getCurrentSession().save(usuarioRutina1);
        sessionFactory.getCurrentSession().save(usuarioRutina2);
        sessionFactory.getCurrentSession().save(usuarioRutina3);

        // Ejecución
        Rutina ultimaRutina = repositorioRutina.getUltimaRutinaRealizadaPorUsuario(usuarioMock);

        // Verificación
        assertNotNull(ultimaRutina);
        assertEquals("Rutina 3", ultimaRutina.getNombre());
    }



    public Rutina crearRutina(String nombre, Objetivo objetivo){
        Rutina rutina = new Rutina(nombre,objetivo);
        this.sessionFactory.getCurrentSession().save(rutina);
        return rutina;
    }

}
