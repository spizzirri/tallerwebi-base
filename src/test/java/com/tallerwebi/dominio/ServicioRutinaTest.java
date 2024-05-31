package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.GrupoMuscularObjetivo;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.*;
import com.tallerwebi.presentacion.DatosRutina;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    @Rollback
    @Test
    public void QueSePuedanObtenerTodasLasRutinas(){
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
    @Rollback
    @Test
    public void QueSeObtengaUnaRutinaSegunObjetivoDeUsuario(){
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
    @Rollback
    @Test
    public void QueArrojeExcepcionSiElUsuarioNoTieneNingunaRutinaAsignada() throws UsuarioSinRutinasException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);

        //e
        when(this.repositorioRutina.getRutinasDeUsuario(usuario)).thenThrow(new UsuarioSinRutinasException());

        //v
        assertThrowsExactly(UsuarioSinRutinasException.class, () ->
                this.repositorioRutina.getRutinasDeUsuario(usuario)
        );
    }

    @Transactional
    @Rollback
    @Test
    public void QueSePuedaValidarElObjetivoDeLaRutinaYLaDelUsuarioAlAgregarUnaRutina() throws UsuarioNoTieneLaRutinaBuscadaException, DiferenciaDeObjetivosExcepcion, UsuarioNoExisteException, RutinaNoEncontradaException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = new Rutina("Rutina de volumen",Objetivo.GANANCIA_MUSCULAR);
        rutina.setIdRutina(1L);

        //e
        when(this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina())).thenReturn(rutina);
        when(this.repositorioRutina.getUsuarioPorId(usuario.getId())).thenReturn(usuario);


        //v
        assertTrue(this.servicioRutina.validarObjetivosDeUsuarioYRutina
        (this.repositorioRutina.getUsuarioPorId(usuario.getId()),this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina())));

    }



    @Transactional
    @Rollback
    @Test
    public void QueSePuedaPedirYValidarRutinaConElObjetivoQueTieneElUsuario() throws DiferenciaDeObjetivosExcepcion, UsuarioNoExisteException, RutinaNoEncontradaException {
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
    @Rollback
    public void QueSePuedaBuscarUnaRutinaPorSuId() throws RutinaNoEncontradaException {
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
    @Rollback
    public void QueAlBuscarUnaRutinaPorSuIdArrojeExcepcionSiNoSeEncuentra() throws RutinaNoEncontradaException {
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
    @Rollback
    public void QueSePuedaBuscarUnEjercicioPorSuId() throws EjercicioNoEncontradoException {
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
    @Rollback
    public void QueAlBuscarUnEjercicioPorSuIdArrojeExcepcionSiNoSeEncuentra() throws EjercicioNoEncontradoException {
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
    @Rollback
    public void QueAlEliminarUnaRutinaSeObtengaTrue() throws RutinaNoEncontradaException{
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
    @Rollback
    public void QueAlQuererEliminarUnaRutinaArrojeRutinaNoEncontradaExcepcion() throws RutinaNoEncontradaException{
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
    @Rollback
    public void QueAlEliminarUnEjercicioSeObtengaTrue() throws EjercicioNoEncontradoException{
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
    @Rollback
    public void QueAlQuererEliminarUnEjercicioArrojeEjercicioNoEncontradoExcepcion() throws EjercicioNoEncontradoException{
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
    @Rollback
    public void QueSePuedaAgregarUnaRutina() throws RutinaYaExisteException {
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
    @Rollback
    public void QueNoSePuedaAgregarRutinaPorqueYaExisteUnaConElMismoId() throws RutinaYaExisteException {
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
    @Rollback
    public void QueSePuedaAgregarUnEjercicio() throws EjercicioYaExisteException {
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
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueYaExisteUnoConElMismoId() throws EjercicioYaExisteException {
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
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueNoTieneObjetivoDefinido() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", null, GrupoMuscularObjetivo.BRAZOS, 4, 12);


        //Ejecución y Verificación
        assertFalse(this.servicioRutina.agregarEjercicio(ejercicioMock));

    }

    @Test
    @Transactional
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueNoTieneNombre() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio(null, Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 12);

        // Ejecución y Verificación
        assertFalse(this.servicioRutina.agregarEjercicio(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueNoTieneGrupoMuscularObjetivo() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, null, 4, 12);

        // Ejecución y Verificación
        assertFalse(this.servicioRutina.agregarEjercicio(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueNoTieneSeriesDefinidas() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 0, 12);

        // Ejecución y Verificación
        assertFalse(this.servicioRutina.agregarEjercicio(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueNoSePuedaAgregarEjercicioPorqueNoTieneRepeticionesDefinidas() throws EjercicioYaExisteException {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Curl de biceps", Objetivo.GANANCIA_MUSCULAR, GrupoMuscularObjetivo.BRAZOS, 4, 0);

        // Ejecución y Verificación
        assertFalse(this.servicioRutina.agregarEjercicio(ejercicioMock));
    }

    @Test
    @Transactional
    @Rollback
    public void QueSePuedaObtenerLosDatosRutinaDeUnaRutinaConUnObjetivoEspecifico() {
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
    @Rollback
    public void QueSePuedaObtenerUnaListaDeDatosRutinaConLasRutinasDeUnUsuario() throws UsuarioSinRutinasException {
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
    @Rollback
    public void QueAlQuererObtenerUnaListaDeDatosRutinaDeLasRutinasDelUsuarioArrojeExcepcionPorNoTenerRutinasAsociadas() throws UsuarioSinRutinasException {
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
    @Rollback
    public void QueSePuedaObtenerUnUsuarioConSuId() throws UsuarioNoExisteException {
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
    @Rollback
    public void QueAlBuscarUnUsuarioPorSuIdArrojeUsuarioNoExisteException() throws UsuarioNoExisteException {
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
    @Rollback
    public void QueSePuedaSaberSiUnUsuarioTieneUnaRutinaEspecifica(){
        // Preparación
        Usuario usuarioMock = new Usuario("Lautaro",Objetivo.DEFINICION);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);
        usuarioMock.getRutinas().add(rutinaMock);

        when(this.repositorioRutina.buscarRutinaEnUsuario(rutinaMock,usuarioMock)).thenReturn(rutinaMock);

        //Ejecución
        boolean resultado = this.servicioRutina.existeRutinaEnUsuario(rutinaMock,usuarioMock);

        //Verificación
        assertTrue(resultado);
    }

    @Test
    @Transactional
    @Rollback
    public void QueAlQuererSaberSiUnUsuarioTieneUnaRutinaEspecificaArrojeFalse() {
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
    @Rollback
    public void QueSePuedaSaberSiUnaRutinaTieneUnEjercicioEspecifico() {
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
    @Rollback
    public void QueAlQuererSaberSiUnaRutinaTieneUnEjercicioEspecificoArrojeFalse() {
        // Preparación
        Ejercicio ejercicioMock = new Ejercicio("Press banca",Objetivo.GANANCIA_MUSCULAR,GrupoMuscularObjetivo.PECHO,4,12);
        Rutina rutinaMock = new Rutina("Rutina de definicion de piernas",Objetivo.DEFINICION);

        when(this.repositorioRutina.buscarEjercicioEnRutina(ejercicioMock,rutinaMock)).thenReturn(null);

        //Ejecución
        boolean resultado = this.servicioRutina.existeEjercicioEnRutina(ejercicioMock,rutinaMock);

        //Verificación
        assertFalse(resultado);
    }

}
