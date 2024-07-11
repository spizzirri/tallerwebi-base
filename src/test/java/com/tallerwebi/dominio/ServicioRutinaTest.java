package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.GrupoMuscularObjetivo;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.rutina.*;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.usuario.UsuarioRutina;
import com.tallerwebi.presentacion.DatosRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class ServicioRutinaTest {

    private ServicioRutina servicioRutina;
    private RepositorioRutina repositorioRutina;

    @BeforeEach
    public void init(){
        this.repositorioRutina = mock(RepositorioRutina.class);
        this.servicioRutina = new ServicioRutinaImpl(this.repositorioRutina);
    }

    @Transactional
    @Test
    public void queSePuedanObtenerTodasLasRutinasCargadas(){
        //p
        List<Rutina> rutinasMock = new ArrayList<>();
        rutinasMock.add(new Rutina("Rutina volumen 1",Objetivo.GANANCIA_MUSCULAR));
        rutinasMock.add(new Rutina("Rutina perdida de peso 1",Objetivo.PERDIDA_DE_PESO));
        rutinasMock.add(new Rutina("Rutina de definicion 1",Objetivo.DEFINICION));
        when(this.repositorioRutina.getRutinas()).thenReturn(rutinasMock);


        //e
        List<DatosRutina> rutinas = this.servicioRutina.getRutinas();

        //v
        assertThat(rutinas.size(),equalTo(3));
    }

    @Transactional
    @Test
    public void queSeObtengaUnaRutinaCorrespondienteSegunElObjetivoDelUsuario(){
        //p
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        Rutina rutinaMock = new Rutina("ADELGAZAR",Objetivo.PERDIDA_DE_PESO);

        when(this.repositorioRutina.getRutinaParaUsuario(usuarioMock)).thenReturn(rutinaMock);

        //e
        DatosRutina rutina = this.servicioRutina.getRutinaParaUsuario(usuarioMock);


        //v
        assertThat(rutina.getObjetivo(),equalTo(usuarioMock.getObjetivo()));
    }

    @Transactional

    @Test
    public void queAlQuererObtenerLasRutinasDelusuarioArrojeExcepcionSiElUsuarioNoTieneNingunaRutinaAsignada() throws UsuarioSinRutinasException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);

        List<Rutina> rutinasmock = new ArrayList<>();

        //e
        when(this.repositorioRutina.getRutinasDeUsuario(usuario)).thenReturn(rutinasmock);

        //v
        UsuarioSinRutinasException exception = assertThrows(
                UsuarioSinRutinasException.class,
                () -> servicioRutina.getRutinasDeUsuario(usuario)
        );

        assertThat(exception.getMessage(), equalTo("El usuario no tiene rutinas asignadas."));
    }

    @Transactional
    @Test
    public void queSePuedaValidarElObjetivoDeLaRutinaYLaDelUsuarioAlAgregarUnaRutina() throws UsuarioNoTieneLaRutinaBuscadaException, DiferenciaDeObjetivosExcepcion, UsuarioNoExisteException, RutinaNoEncontradaException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = new Rutina("Rutina de volumen",Objetivo.GANANCIA_MUSCULAR);
        usuario.setId(1L);
        rutina.setIdRutina(1L);

        //e
        when(this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina())).thenReturn(rutina);
        when(this.repositorioRutina.getUsuarioPorId(usuario.getId())).thenReturn(usuario);


        //v
        assertTrue(this.servicioRutina.validarObjetivosDeUsuarioYRutina
        (this.repositorioRutina.getUsuarioPorId(usuario.getId()),this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina())));

    }

    @Transactional
    @Test
    public void queSePuedaPedirYValidarRutinaConElObjetivoQueTieneElUsuario() throws DiferenciaDeObjetivosExcepcion, UsuarioNoExisteException, RutinaNoEncontradaException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = new Rutina("Rutina de volumen",Objetivo.GANANCIA_MUSCULAR);
        rutina.setIdRutina(1L);
        usuario.setId(1L);

        //e
        when(this.repositorioRutina.getUsuarioPorId(usuario.getId())).thenReturn(usuario);
        when(this.repositorioRutina.getRutinaByObjetivo(usuario.getObjetivo())).thenReturn(rutina);

        //v
        assertTrue(this.servicioRutina.validarObjetivosDeUsuarioYRutina(this.repositorioRutina.getUsuarioPorId(usuario.getId()),this.repositorioRutina.getRutinaByObjetivo(usuario.getObjetivo())));

    }

    @Test
    @Transactional
    public void queSePuedaBuscarUnaRutinaPorSuId() throws RutinaNoEncontradaException {
        // Preparación
        Rutina rutinaMock = new Rutina("Volumen", Objetivo.GANANCIA_MUSCULAR);
        rutinaMock.setIdRutina(1L);

        when(this.repositorioRutina.buscarRutinaPorId(rutinaMock.getIdRutina())).thenReturn(rutinaMock);

        // Ejecución
        Rutina rutinaObtenida = this.servicioRutina.getRutinaById(rutinaMock.getIdRutina());

        // Verificación
        assertThat(rutinaObtenida, equalTo(rutinaMock));
    }

    @Test
    @Transactional
    public void queAlBuscarUnaRutinaPorSuIdArrojeExcepcionSiNoSeEncuentra() throws RutinaNoEncontradaException {
        // Preparación
        Long idRutinaInexistente = 1L;

        // Configuración del mock para lanzar la excepción
        when(this.repositorioRutina.buscarRutinaPorId(idRutinaInexistente)).thenReturn(null);

        // Verificación
        assertThrowsExactly(RutinaNoEncontradaException.class, () ->
                this.servicioRutina.getRutinaById(idRutinaInexistente)
        );
    }

    @Test
    @Transactional
    public void queSeObtenganDatosRutinaCuandoRutinaExiste() throws RutinaNoEncontradaException {
        // Preparación
        Long idRutina = 1L;
        Rutina rutinaMock = new Rutina("Rutina de Prueba", Objetivo.GANANCIA_MUSCULAR);
        when(repositorioRutina.buscarRutinaPorId(idRutina)).thenReturn(rutinaMock);

        // Ejecución
        DatosRutina datosRutinaObtenidos = servicioRutina.getDatosRutinaById(idRutina);

        // Verificación
        assertNotNull(datosRutinaObtenidos);
        assertEquals("Rutina de Prueba", datosRutinaObtenidos.getNombre());
        assertEquals(Objetivo.GANANCIA_MUSCULAR, datosRutinaObtenidos.getObjetivo());
    }

    @Test
    @Transactional
    public void queAlQuererObtenerLosDatosRutinaDeUnaRutinaPorSuIdSeLanceExcepcionSiLaRutinaNoExiste() {
        // Preparación
        Long idRutina = 1L;
        when(repositorioRutina.buscarRutinaPorId(idRutina)).thenReturn(null);

        // Ejecución y Verificación
        assertThrows(RutinaNoEncontradaException.class, () -> {
            servicioRutina.getDatosRutinaById(idRutina);
        });
    }



    @Test
    @Transactional
    public void queSePuedaBuscarUnEjercicioPorSuId() throws EjercicioNoEncontradoException {
        //p
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        when(this.repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio())).thenReturn(ejercicioMock);

        //e
        Ejercicio ejercicioObtenido = this.servicioRutina.getEjercicioById(ejercicioMock.getIdEjercicio());

        //v
        assertThat(ejercicioObtenido, equalTo(ejercicioMock));
    }

    @Test
    @Transactional
    public void queAlBuscarUnEjercicioPorSuIdArrojeExcepcionSiNoSeEncuentra() throws EjercicioNoEncontradoException {
        //p

        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        Long idEjercicio  =ejercicioMock.getIdEjercicio();

        //e y v
        assertThrowsExactly(EjercicioNoEncontradoException.class, () ->
                this.servicioRutina.getEjercicioById(idEjercicio)
        );
    }

    @Test
    @Transactional
    public void queAlIntentarEliminarUnaRutinaSeObtengaTrue() throws RutinaNoEncontradaException{
        // Preparación
        Rutina rutinaHombros = new Rutina("Rutina de definición de hombros", Objetivo.DEFINICION);
        rutinaHombros.setIdRutina(1L); // Simulando que ya tiene un ID asignado

        when(repositorioRutina.buscarRutinaPorId(rutinaHombros.getIdRutina())).thenReturn(rutinaHombros);

        // Ejecución
        boolean resultado = servicioRutina.eliminarRutina(rutinaHombros);

        // Verificación
        assertThat(resultado, equalTo(true));
        verify(repositorioRutina).eliminarRutina(rutinaHombros);

    }
    @Test
    @Transactional
    public void queAlQuererEliminarUnaRutinaArrojeRutinaNoEncontradaExcepcion() throws RutinaNoEncontradaException{
        // Preparación
        Rutina rutinaInexistente = new Rutina();
        rutinaInexistente.setIdRutina(999L); // ID que seguramente no existe

        when(repositorioRutina.buscarRutinaPorId(rutinaInexistente.getIdRutina())).thenReturn(null);

        // Ejecución y Verificación
        RutinaNoEncontradaException exception = assertThrows(
                RutinaNoEncontradaException.class,
                () -> servicioRutina.eliminarRutina(rutinaInexistente)
        );

        assertThat(exception.getMessage(), equalTo("No se encontró la rutina."));
    }

    @Test
    @Transactional
    public void queAlIntentarEliminarUnEjercicioSeObtengaTrue() throws EjercicioNoEncontradoException{
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        ejercicioMock.setIdEjercicio(1L);// Simulando que ya tiene un ID asignado

        when(repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio())).thenReturn(ejercicioMock);

        // Ejecución
        boolean resultado = servicioRutina.eliminarEjercicio(ejercicioMock);

        // Verificación
        assertThat(resultado, equalTo(true));
        verify(repositorioRutina).eliminarEjercicio(ejercicioMock);

    }
    @Test
    @Transactional
    public void queAlQuererEliminarUnEjercicioArrojeEjercicioNoEncontradoExcepcion() throws EjercicioNoEncontradoException{
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        ejercicioMock.setIdEjercicio(999L); // ID que seguramente no existe

        when(repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio())).thenReturn(null);

        // Ejecución y Verificación
        EjercicioNoEncontradoException exception = assertThrows(
                EjercicioNoEncontradoException.class,
                () -> servicioRutina.eliminarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("No se encontró el ejercicio."));
    }

    @Test
    @Transactional
    public void queSePuedaAgregarUnaRutina() throws RutinaYaExisteException, RutinaInvalidaException {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina de pecho",Objetivo.GANANCIA_MUSCULAR);
        rutinaMock.setIdRutina(2L);

        when(this.repositorioRutina.buscarRutinaPorId(rutinaMock.getIdRutina())).thenReturn(null);

        //Ejecución
        boolean resultado = servicioRutina.agregarRutina(rutinaMock);

        //Verificación
        assertThat(resultado, equalTo(true));
        verify(repositorioRutina).guardarRutina(rutinaMock);

    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarUnaRutinaPorqueYaExisteUnaConElMismoId() throws RutinaYaExisteException {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina de espalda",Objetivo.GANANCIA_MUSCULAR);
        rutinaMock.setIdRutina(1L);
        Rutina rutinaMock2 = new Rutina("Rutina de espalda",Objetivo.GANANCIA_MUSCULAR);
        rutinaMock.setIdRutina(1L);

        when(this.repositorioRutina.buscarRutinaPorId(rutinaMock.getIdRutina())).thenReturn(rutinaMock2);

        //Ejecución y Verificación
        RutinaYaExisteException exception = assertThrows(
                RutinaYaExisteException.class,
                () -> servicioRutina.agregarRutina(rutinaMock)
        );

        assertThat(exception.getMessage(), equalTo("La rutina ya existe"));

    }

    @Test
    @Transactional
    public void queSePuedaAgregarUnEjercicio() throws EjercicioYaExisteException, EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        ejercicioMock.setIdEjercicio(1L);

        when(this.repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio())).thenReturn(null);

        //Ejecución
        boolean resultado = servicioRutina.agregarEjercicio(ejercicioMock);

        //Verificación
        assertThat(resultado, equalTo(true));
        verify(repositorioRutina).guardarEjercicio(ejercicioMock);

    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarEjercicioPorqueYaExisteUnoConElMismoId() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        ejercicioMock.setIdEjercicio(1L);
        Ejercicio ejercicioMock2 = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        ejercicioMock.setIdEjercicio(1L);

        when(this.repositorioRutina.buscarEjercicioPorId(ejercicioMock.getIdEjercicio())).thenReturn(ejercicioMock2);

        //Ejecución y Verificación
        EjercicioYaExisteException exception = assertThrows(
                EjercicioYaExisteException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("El ejercicio ya existe"));

    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarEjercicioPorqueNoTieneUnObjetivoDefinido() throws EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", null, GrupoMuscularObjetivo.BRAZOS, 4, 12);


        // Ejecución y Verificación
        EjercicioInvalidoException exception = assertThrows(
                EjercicioInvalidoException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("Los datos del ejercicio son invalidos."));

    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarUnEjercicioPorqueNoTieneNombre() throws EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio(null, Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);

        // Ejecución y Verificación
        EjercicioInvalidoException exception = assertThrows(
                EjercicioInvalidoException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("Los datos del ejercicio son invalidos."));

    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarUnEjercicioPorqueNoTieneGrupoMuscularObjetivo() throws EjercicioYaExisteException, EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, null, 4, 12);

        // Ejecución y Verificación
        EjercicioInvalidoException exception = assertThrows(
                EjercicioInvalidoException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("Los datos del ejercicio son invalidos."));
    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarUnEjercicioPorqueNoTieneSeriesDefinidas() throws EjercicioYaExisteException, EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 0, 12);

        // Ejecución y Verificación
        EjercicioInvalidoException exception = assertThrows(
                EjercicioInvalidoException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("Los datos del ejercicio son invalidos."));
    }

    @Test
    @Transactional
    public void queNoSePuedaAgregarUnEjercicioPorqueNoTieneRepeticionesDefinidas() throws EjercicioYaExisteException, EjercicioInvalidoException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 0);

        // Ejecución y Verificación
        EjercicioInvalidoException exception = assertThrows(
                EjercicioInvalidoException.class,
                () -> servicioRutina.agregarEjercicio(ejercicioMock)
        );

        assertThat(exception.getMessage(), equalTo("Los datos del ejercicio son invalidos."));
    }

    @Test
    @Transactional
    public void queSePuedaObtenerLosDatosRutinaDeUnaRutinaConUnObjetivoEspecifico() {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);
        rutinaMock.setIdRutina(1L);
        DatosRutina datosRutinaMock = new DatosRutina(rutinaMock);

        when(this.repositorioRutina.getRutinaByObjetivo(rutinaMock.getObjetivo())).thenReturn(rutinaMock);

        // Ejecución
        DatosRutina datosRutinaEsperada = this.servicioRutina.getRutinaByObjetivo(rutinaMock.getObjetivo());

        //Verificación
        assertThat(datosRutinaMock.getObjetivo(),equalTo(datosRutinaEsperada.getObjetivo()));
    }

    @Test
    @Transactional
    public void queSePuedaObtenerUnaListaDeDatosRutinaConLasRutinasDeUnUsuario() throws UsuarioSinRutinasException {
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);
        Rutina rutinaMock2 = new Rutina("Rutina de definicion de biceps",Objetivo.DEFINICION);

        List<Rutina> listaDeRutinas = new ArrayList<>();
        listaDeRutinas.add(rutinaMock);
        listaDeRutinas.add(rutinaMock2);

        List<DatosRutina> listaDeDatosRutinaMock = new ArrayList<>();
        DatosRutina datosRutinaMock = new DatosRutina(rutinaMock);
        DatosRutina datosRutinaMock2 = new DatosRutina(rutinaMock2);
        listaDeDatosRutinaMock.add(datosRutinaMock);
        listaDeDatosRutinaMock.add(datosRutinaMock2);


        when(this.repositorioRutina.getRutinasDeUsuario(usuarioMock)).thenReturn(listaDeRutinas);

        // Ejecución
        List<DatosRutina> datosRutinaEsperada = this.servicioRutina.getRutinasDeUsuario(usuarioMock);

        //Verificación
        assertThat(datosRutinaEsperada.size(),equalTo(listaDeDatosRutinaMock.size()));
    }

    @Test
    @Transactional
    public void queAlQuererObtenerUnaListaDeDatosRutinaDeLasRutinasDelUsuarioArrojeExcepcionPorNoTenerRutinasAsociadas() throws UsuarioSinRutinasException {
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        List<Rutina> listaDeRutinas = new ArrayList<>();

        when(this.repositorioRutina.getRutinasDeUsuario(usuarioMock)).thenReturn(listaDeRutinas);

        //Ejecución y Verificación
        UsuarioSinRutinasException exception = assertThrows(
                UsuarioSinRutinasException.class,
                () -> servicioRutina.getRutinasDeUsuario(usuarioMock)
        );

        assertThat(exception.getMessage(), equalTo("El usuario no tiene rutinas asignadas."));

    }

    @Test
    @Transactional
    public void queSePuedaObtenerUnUsuarioConSuId() throws UsuarioNoExisteException {
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        usuarioMock.setId(1L);

        when(this.repositorioRutina.getUsuarioPorId(usuarioMock.getId())).thenReturn(usuarioMock);

        //Ejecución
        Usuario usuarioBuscado = this.servicioRutina.getUsuarioById(usuarioMock.getId());

        //Verificación
        assertThat(usuarioBuscado,equalTo(usuarioMock));
    }

    @Test
    @Transactional
    public void queAlBuscarUnUsuarioPorSuIdArrojeUsuarioNoExisteException() throws UsuarioNoExisteException {
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        usuarioMock.setId(512L);

        when(this.repositorioRutina.getUsuarioPorId(usuarioMock.getId())).thenReturn(null);

        //Ejecución y Verificación
        UsuarioNoExisteException exception = assertThrows(
                UsuarioNoExisteException.class,
                () -> servicioRutina.getUsuarioById(usuarioMock.getId())
        );

        assertThat(exception.getMessage(), equalTo("El usuario no existe."));
    }

    @Test
    @Transactional
    public void queSePuedaSaberSiUnUsuarioTieneUnaRutinaEspecifica(){
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        usuarioMock.setId(1L);

        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);
        rutinaMock.setIdRutina(1L);

        when(this.repositorioRutina.buscarRutinaEnUsuario(rutinaMock, usuarioMock)).thenReturn(rutinaMock);

        // Ejecución
        boolean resultado = this.servicioRutina.existeRutinaEnUsuario(rutinaMock, usuarioMock);

        // Verificación
        assertTrue(resultado);

        verify(this.repositorioRutina).buscarRutinaEnUsuario(rutinaMock, usuarioMock);
    }

    @Test
    @Transactional
    public void queAlQuererSaberSiUnUsuarioTieneUnaRutinaEspecificaArrojeFalse() {
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);

        when(this.repositorioRutina.buscarRutinaEnUsuario(rutinaMock,usuarioMock)).thenReturn(null);

        //Ejecución
        boolean resultado = this.servicioRutina.existeRutinaEnUsuario(rutinaMock,usuarioMock);

        //Verificación
        assertFalse(resultado);
    }

    @Test
    @Transactional
    public void queSePuedaSaberSiUnaRutinaTieneUnEjercicioEspecifico() {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);
        rutinaMock.getEjercicios().add(ejercicioMock);

        when(this.repositorioRutina.buscarEjercicioEnRutina(ejercicioMock,rutinaMock)).thenReturn(ejercicioMock);

        //Ejecución
        boolean resultado = this.servicioRutina.existeEjercicioEnRutina(ejercicioMock,rutinaMock);

        //Verificación
        assertTrue(resultado);
    }

    @Test
    @Transactional
    public void queAlQuererSaberSiUnaRutinaTieneUnEjercicioEspecificoArrojeFalse() {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);

        when(this.repositorioRutina.buscarEjercicioEnRutina(ejercicioMock,rutinaMock)).thenReturn(null);

        //Ejecución
        boolean resultado = this.servicioRutina.existeEjercicioEnRutina(ejercicioMock,rutinaMock);

        //Verificación
        assertFalse(resultado);
    }

    @Test
    @Transactional
    public void queSePuedaObtenerUnaListaDeDatosRutinaDeRutinasConObjetivoPerdidaDePeso() throws ListaDeRutinasVaciaException {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina def 1",Objetivo.DEFINICION);
        Rutina rutinaMock2 = new Rutina("Rutina def 2",Objetivo.DEFINICION);
        List<Rutina> rutinasEsperadas = new ArrayList<>();
        rutinasEsperadas.add(rutinaMock);
        rutinasEsperadas.add(rutinaMock2);
        Objetivo objetivoMock = Objetivo.DEFINICION;

        //Ejecución
        when(this.repositorioRutina.getRutinasByObjetivo(objetivoMock)).thenReturn(rutinasEsperadas);

        //Verificación
        List<DatosRutina> rutinasObtenidas = this.servicioRutina.getRutinasPorObjetivo(objetivoMock);

        assertEquals(rutinasObtenidas.size(),2);
    }

    @Test
    public void queSeLanceExcepcionCuandoNoHayRutinasConElObjetivoEspecificado() throws ListaDeRutinasVaciaException {
        // Ejecución y Verificación
        assertThrows(ListaDeRutinasVaciaException.class, () -> {
            servicioRutina.getRutinasPorObjetivo(Objetivo.DEFINICION);
        });
    }


    @Test
    public void queAlQuererObtenerUnaListaDeDatosRutinasSeLanceExcepcionCuandoObjetivoEsNulo() {
        // Ejecución y Verificación
        assertThrows(IllegalArgumentException.class, () -> {
            servicioRutina.getRutinasPorObjetivo(null);
        });
    }


    @Test
    @Transactional
    public void queNoSeObtenganRutinasCuandoLosObjetivosDelUsuarioYLosDeLasRutinasCargadasNoCoinciden() {
        // Preparación
        Usuario usuarioMock = new Usuario("Juan", Objetivo.GANANCIA_MUSCULAR);
        repositorioRutina.guardarUsuario(usuarioMock);

        Rutina rutina1 = new Rutina("Rutina 1", Objetivo.DEFINICION);
        Rutina rutina2 = new Rutina("Rutina 2", Objetivo.DEFINICION);
        List<Rutina> rutinasEsperadas = Arrays.asList(rutina1, rutina2);

        when(this.repositorioRutina.getRutinasPorObjetivoDeUsuario(usuarioMock)).thenReturn(rutinasEsperadas);

        // Ejecución
        List<DatosRutina> rutinasObtenidas = this.servicioRutina.getRutinasPorObjetivoDeUsuario(usuarioMock);

        // Verificación
        assertTrue(rutinasObtenidas.isEmpty());
    }

    @Test
    @Transactional
    public void queSePuedaObtenerRutinaActualDelUsuarioCuandoUsuarioExisteYTieneRutinaActiva() throws UsuarioNoExisteException, RutinaNoEncontradaException {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        Usuario usuarioBuscadoMock = new Usuario();
        usuarioBuscadoMock.setId(1L);

        Rutina rutinaMock = new Rutina("Rutina Activa", Objetivo.GANANCIA_MUSCULAR);

        when(repositorioRutina.getUsuarioPorId(1L)).thenReturn(usuarioBuscadoMock);
        when(repositorioRutina.getRutinaActivaDelUsuario(usuarioBuscadoMock)).thenReturn(rutinaMock);

        // Ejecución
        DatosRutina datosRutina = servicioRutina.getRutinaActualDelUsuario(usuarioMock);

        // Verificación
        assertNotNull(datosRutina);
        assertEquals("Rutina Activa", datosRutina.getNombre());
        assertEquals(Objetivo.GANANCIA_MUSCULAR, datosRutina.getObjetivo());
    }

    @Test
    @Transactional
    public void queSeLanceExcepcionCuandoUsuarioNoExisteAlQuererObtenerLaRutinaActualDelUsuario() {
        // Ejecución y Verificación
        assertThrows(UsuarioNoExisteException.class, () -> {
            servicioRutina.getRutinaActualDelUsuario(new Usuario());
        });
    }

    @Test
    @Transactional
    public void queSeRetorneNullCuandoUsuarioExistePeroNoTieneRutinaActiva() throws UsuarioNoExisteException, RutinaNoEncontradaException {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        Usuario usuarioBuscadoMock = new Usuario();
        usuarioBuscadoMock.setId(1L);

        when(repositorioRutina.getUsuarioPorId(1L)).thenReturn(usuarioBuscadoMock);
        when(repositorioRutina.getRutinaActivaDelUsuario(usuarioBuscadoMock)).thenReturn(null);

        // Ejecución y verificación
        assertThrows(RutinaNoEncontradaException.class, () -> {
            servicioRutina.getRutinaActualDelUsuario(usuarioMock);
        });
    }

    @Test
    @Transactional
    public void queSePuedaAsignarUnaRutinaAUsuarioCuandoLaRutinaNoSeEncuentraActivaEnElUsuario() throws RutinaYaExisteException {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        Rutina rutinaMock = new Rutina("Nueva Rutina", Objetivo.GANANCIA_MUSCULAR);

        when(repositorioRutina.getRutinaActivaDelUsuario(usuarioMock)).thenReturn(null);

        // Ejecución
        servicioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);

        // Verificación
        verify(repositorioRutina, times(1)).asignarRutinaAUsuario(rutinaMock, usuarioMock);
    }

    @Test
    @Transactional
    public void queSeLanceExcepcionCuandoLaRutinaYaEstaActivaYAsignadaAlUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        Rutina rutinaMock = new Rutina("Rutina Activa", Objetivo.GANANCIA_MUSCULAR); // Misma rutina

        // Simula que la rutinaMock ya está activa y asignada al usuario
        when(repositorioRutina.getRutinaActivaDelUsuario(usuarioMock)).thenReturn(rutinaMock);

        // Ejecución y Verificación
        assertThrows(RutinaYaExisteException.class, () -> {
            servicioRutina.asignarRutinaAUsuario(rutinaMock, usuarioMock);
        });

    }

    @Test
    @Transactional
    public void queSePuedaLiberarLaRutinaActivaDelUsuario() {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        Rutina rutinaMock = new Rutina("Rutina Activa", Objetivo.GANANCIA_MUSCULAR);
        UsuarioRutina usuarioRutinaMock = new UsuarioRutina(usuarioMock, rutinaMock, new Date(), true);

        // Configuración del mock para cambiar el estado activo a false
        doAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            // Simulamos que encontramos el UsuarioRutina activo y lo desactivamos
            usuarioRutinaMock.setActivo(false);
            return null;
        }).when(repositorioRutina).liberarRutinaActivaDelUsuario(usuarioMock);

        // Ejecución
        servicioRutina.liberarRutinaActivaDelUsuario(usuarioMock);

        // Verificación
        verify(repositorioRutina, times(1)).liberarRutinaActivaDelUsuario(usuarioMock);
        assertFalse(usuarioRutinaMock.isActivo());
    }

    @Test
    @Transactional
    public void queSePuedaActualizarElEstadoDelEjercicio() {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        Long ejercicioIdMock = 1L;
        EstadoEjercicio.Estado estadoMock = EstadoEjercicio.Estado.COMPLETO;

        // Ejecución
        servicioRutina.actualizarEstadoEjercicio(usuarioMock, ejercicioIdMock, estadoMock);

        // Verificación
        verify(repositorioRutina, times(1)).actualizarEstadoEjercicio(usuarioMock, ejercicioIdMock, estadoMock);
    }

    @Test
    @Transactional
    public void queSePuedanObtenerLosEstadosDeLosEjerciciosBrindandoUnUsuarioYDatosDeRutina() {
        // Preparación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        DatosRutina rutinaMock = new DatosRutina();
        rutinaMock.setId(1L);

        List<EstadoEjercicio> estadosEsperados = new ArrayList<>();
        estadosEsperados.add(new EstadoEjercicio());
        estadosEsperados.add(new EstadoEjercicio());

        when(repositorioRutina.getEstadoEjercicioList(usuarioMock, rutinaMock)).thenReturn(estadosEsperados);

        // Ejecución
        List<EstadoEjercicio> estadosObtenidos = servicioRutina.getEstadosEjercicios(usuarioMock, rutinaMock);

        // Verificación
        assertNotNull(estadosObtenidos);
        assertEquals(estadosEsperados.size(), estadosObtenidos.size());
        assertEquals(estadosEsperados, estadosObtenidos);
    }


    @Test
    @Transactional
    public void queRendimientoSeaDescansoCuandoListaDeEjerciciosEsVacia() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Collections.emptyList();

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.DESCANSO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaAltoCuandoTodosEjerciciosEstanCompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.ALTO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaBajoCuandoTodosEjerciciosEstanIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.BAJO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaDescansoCuandoTodosEjerciciosEstanNoEmpezados() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.DESCANSO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaAltoCuandoMasDel75PorCientoDeEjerciciosEstanCompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.ALTO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoMasDel50PorCientoPeroNoMasDel75PorCientoDeEjerciciosEstanCompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoMenosDel50PorCientoDeEjerciciosEstanCompletosPeroMenosDel50PorCientoDeEjerciciosEstanIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queElRendimientoDeLaRutinaSeaMedioCuandoMenosDel50PorCientoDeEjerciciosEstanCompletosYMasDel50PorCientoDeEjerciciosEstanIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaBajoCuandoMasDel75PorCientoDeEjerciciosNoEstanEmpezados() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.BAJO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoExactamente33PorCientoDeCadaEstado() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoMasDel50PorCientoDeEjerciciosEstanCompletosYMasDel50PorCientoDeEjerciciosEstanIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoExactamente50PorCientoDeEjerciciosEstanCompletosY50PorCientoIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaMedioCuandoMenosDel50PorCientoDeEjerciciosEstanCompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.COMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.MEDIO, rendimiento);
    }

    @Test
    @Transactional
    public void queRendimientoSeaBajoCuandoMenosDel75PorCientoDeEjerciciosNoEstanEmpezadosYElRestosIncompletos() {
        // Preparación
        List<EstadoEjercicio> estadosEjercicios = Arrays.asList(
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.NO_EMPEZADO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO),
                new EstadoEjercicio(EstadoEjercicio.Estado.INCOMPLETO)
        );

        // Ejecución
        Rendimiento rendimiento = servicioRutina.calcularRendimiento(estadosEjercicios);

        // Verificación
        assertEquals(Rendimiento.BAJO, rendimiento);
    }

    @Test
    public void queLanceUsuarioNoExisteExceptionCuandoUsuarioEsNull() {
        // Preparación
        Objetivo objetivo = Objetivo.DEFINICION; // Usando un valor del enum

        // Ejecución y Verificación
        assertThrows(UsuarioNoExisteException.class, () -> {
            servicioRutina.guardarObjetivoEnUsuario(objetivo, null);
        });
    }

    @Test
    public void queLanceObjetivoNoExisteExceptionCuandoObjetivoEsNull() {
        // Preparación
        Usuario usuario = new Usuario(); // Suponiendo que tienes un constructor sin parámetros

        // Ejecución y Verificación
        assertThrows(ObjetivoNoExisteException.class, () -> {
            servicioRutina.guardarObjetivoEnUsuario(null, usuario);
        });
    }

    @Test
    public void queLlameARepositorioRutinaSetObjetivoUsuarioCuandoObjetivoYUsuarioNoSonNull() {
        // Preparación
        Objetivo objetivo = Objetivo.GANANCIA_MUSCULAR;
        Usuario usuario = new Usuario();

        // Ejecución
        try {
            servicioRutina.guardarObjetivoEnUsuario(objetivo, usuario);
        } catch (Exception e) {
            fail("No debería lanzar excepción");
        }

        // Verificación
        Mockito.verify(repositorioRutina, Mockito.times(1)).setObjetivoUsuario(objetivo, usuario);
    }


}
