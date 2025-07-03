package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ControladorArmaTuPcTest {

    private ServicioArmaTuPc servicioArmaTuPcMock;
    private HttpSession sessionMock;
    private ArmadoPcDto armadoPcDtoMock;
    private ComponenteDto componenteDtoMock;
    private ControladorArmaTuPc controladorArmaTuPc;
    private ServicioPrecios servicioPreciosMock;

    @BeforeEach
    public void init() {
        servicioArmaTuPcMock = mock(ServicioArmaTuPc.class);
        servicioPreciosMock = mock(ServicioPrecios.class);
        sessionMock = mock(HttpSession.class);
        armadoPcDtoMock = mock(ArmadoPcDto.class);
        componenteDtoMock = mock(ComponenteDto.class);
        controladorArmaTuPc = new ControladorArmaTuPc(servicioArmaTuPcMock, servicioPreciosMock);
    }

    // Tests para obtenerArmadoPcDtoDeLaSession (Lógica interna)
    @Test
    public void cuandoNoExisteUnArmadoEnLaSessionSeCreaUnoNuevoYSeGuarda() {
        // Preparación
        when(sessionMock.getAttribute("armadoPcDto"))
                .thenReturn(null)                         // primera vez
                .thenReturn(new ArmadoPcDto());          // segunda vez (y siguientes)

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.cargarComponentes("procesador", null, sessionMock);

        // Verificación
        verify(sessionMock, times(1)).setAttribute(eq("armadoPcDto"), ArgumentMatchers.any(ArmadoPcDto.class));
        assertThat(mav.getModel().get("armadoPcDto"), is(notNullValue()));
        assertThat(mav.getModel().get("armadoPcDto"), is(instanceOf(ArmadoPcDto.class)));
    }

    @Test
    public void cuandoYaExisteUnArmadoEnLaSessionSeUtilizaElExistente() {
        // Preparación
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);

        // Ejecución (indirecta)
        controladorArmaTuPc.cargarComponentes("procesador", null, sessionMock);

        // Verificación
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), ArgumentMatchers.any(ArmadoPcDto.class));
        verify(sessionMock, times(2)).getAttribute("armadoPcDto"); // Una en el if, otra en el return
    }

    // Tests para cargarComponentes
    @Test
    public void cuandoSeCargaLaPaginaDeUnComponenteSinQuerySeObtienenLosComponentesCompatibles() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenReturn(List.of(componenteDtoMock));
        when(armadoPcDtoMock.getComponentesDto()).thenReturn(Collections.emptyList());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/motherboard"));
        assertThat(mav.getModel().get("componentesLista"), is(notNullValue()));
        assertThat((List<ComponenteDto>) mav.getModel().get("componentesLista"), hasSize(1));
        assertThat(mav.getModel().get("pasoActual"), equalTo("motherboard"));
        assertThat(mav.getModel().get("pasoAnterior"), equalTo("procesador"));
        assertThat(mav.getModel().get("pasoSiguiente"), equalTo("cooler"));
        verify(servicioArmaTuPcMock).obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock);
        verify(servicioArmaTuPcMock, never()).obtenerListaDeComponentesCompatiblesFiltradosDto(anyString(), anyString(), ArgumentMatchers.any(ArmadoPcDto.class));
    }

    @Test
    public void cuandoSeCargaLaPaginaConQuerySeObtienenLosComponentesFiltrados() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "gpu";
        String query = "Nvidia";
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDtoMock))
                .thenReturn(List.of(componenteDtoMock));
        when(armadoPcDtoMock.getComponentesDto()).thenReturn(Collections.emptyList());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, query, sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/gpu"));
        assertThat(mav.getModel().get("componentesLista"), is(notNullValue()));
        verify(servicioArmaTuPcMock).obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDtoMock);
        verify(servicioArmaTuPcMock, never()).obtenerListaDeComponentesCompatiblesDto(anyString(), ArgumentMatchers.any(ArmadoPcDto.class));
    }

    @Test
    public void cuandoSeCargaLaPaginaSeAgreganLosIdsDeComponentesSeleccionadosAlModelo() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "gpu";
        ComponenteDto comp1 = mock(ComponenteDto.class);
        ComponenteDto comp2 = mock(ComponenteDto.class);

        when(comp1.getId()).thenReturn(10L);
        when(comp2.getId()).thenReturn(20L);

        List<ComponenteDto> componentesSeleccionados = List.of(comp1, comp2);

        when(armadoPcDtoMock.getComponentesDto()).thenReturn(componentesSeleccionados);
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenReturn(List.of(componenteDtoMock));

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación
        assertThat(mav.getModel().get("idsDeComponentesSeleccionados"), is(notNullValue()));
        assertThat( mav.getModel().get("idsDeComponentesSeleccionados"), instanceOf(Set.class));
        assertThat(((Set<Long>)mav.getModel().get("idsDeComponentesSeleccionados")), hasItems(10L, 20L));
    }

    @Test
    public void cuandoNoSeHaSeleccionadoUnComponenteDeterminanteSeMuestraUnError() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenThrow(new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar un procesador primero"));

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/motherboard"));
        assertThat(mav.getModel().get("errorLista"), equalTo("Debe seleccionar un procesador primero"));
        assertNull(mav.getModel().get("componentesLista"));
    }

    // Tests para procesarAccion
    @Test
    public void cuandoLaAccionEsInvalidaSeRedirigeConMensajeDeError(){
        // Preparación
        String tipoComponente = "procesador";

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.procesarAccion(tipoComponente, "accionInvalida", 1L, 1, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/" + tipoComponente));
        assertThat(model.get("accionInvalida"), equalTo("Ingreso una accion invalida."));
    }

    @Test
    public void cuandoLaAccionEsAgregarSeLlamaAlMetodoDeAgregarComponenteYRedirigeConSiguientePaso() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        String tipoComponente = "procesador";

        Long idComponente = 1L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock)).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.procesarAccion(tipoComponente, "agregar", 1L, 1, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(servicioArmaTuPcMock, times(1)).agregarComponenteAlArmado(idComponente, tipoComponente, cantidad , armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/motherboard"));
    }

    @Test
    public void cuandoLaQuitarEsAgregarSeLlamaAlMetodoDeQuitarComponenteYRedirigeElMismoPaso() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación
        String tipoComponente = "procesador";

        Long idComponente = 1L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock)).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.procesarAccion(tipoComponente, "quitar", 1L, 1, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(servicioArmaTuPcMock, times(1)).quitarComponenteAlArmado(idComponente, tipoComponente, cantidad , armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/procesador"));
    }

    // Tests para agregarComponenteAlArmado
    @Test
    public void cuandoAgregoUnComponenteExitosamenteSeActualizaLaSessionYSeRedirige() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        String tipoComponente = "procesador";
        Long idComponente = 1L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock)).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);
        when(componenteDtoMock.getModelo()).thenReturn("Intel Core i5");
        when(servicioArmaTuPcMock.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDtoMock)).thenReturn(false); // Va al siguiente paso

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, idComponente, cantidad, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(sessionMock).setAttribute("armadoPcDto", armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/motherboard"));
        assertThat(model.get("agregado"), equalTo("x1 Intel Core i5 agregado correctamente al armado!"));
    }

    @Test
    public void cuandoIntentoAgregarMasComponentesDelLimiteSeMuestraUnError() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        String tipoComponente = "memoria";
        Long idComponente = 1L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock))
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, idComponente, cantidad, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(sessionMock, never()).setAttribute(anyString(), any());
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/memoria"));
        assertThat(model.get("errorLimite"), equalTo("Supero el limite de memoria de su armado"));
    }

    // Tests para quitarComponenteDelArmado
    @Test
    public void cuandoQuitoUnComponenteExitosamenteSeActualizaElArmadoYSeMuestraMensaje() throws Exception {
        // Preparación
        String tipoComponente = "gpu";
        Long idComponente = 5L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock)).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);
        when(componenteDtoMock.getModelo()).thenReturn("NVIDIA RTX 3060");

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(sessionMock).setAttribute("armadoPcDto", armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/gpu"));
        assertThat(model.get("quitado"), equalTo("x1 NVIDIA RTX 3060 fue quitado del armado."));
    }

    @Test
    public void cuandoIntentoQuitarUnComponenteNoAgregadoSeMuestraError() throws Exception {
        // Preparación
        String tipoComponente = "gpu";
        Long idComponente = 5L;
        Integer cantidad = 1;

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock))
                .thenThrow(new QuitarComponenteInvalidoException());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(sessionMock, never()).setAttribute(anyString(), any());
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/gpu"));
        assertThat(model.get("errorQuitado"), equalTo("No es posible quitar un componente que no fue agregado al armado."));
    }

    @Test
    public void cuandoIntentoQuitarMasCantidadDeLaQueHaySeMuestraError() throws Exception {
        // Preparación
        String tipoComponente = "memoria";
        Long idComponente = 8L;
        Integer cantidad = 2; // Intenta quitar 2

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock))
                .thenThrow(new QuitarStockDemasDeComponenteException());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, sessionMock);
        ModelMap model = mav.getModelMap();

        // Verificación
        verify(sessionMock, never()).setAttribute(anyString(), any());
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/memoria"));
        assertThat(model.get("errorQuitado"), equalTo("No es posible quitar una cantidad del componente que no posee el armado."));
    }

    // Tests para obtenerResumen
    @Test
    public void cuandoElArmadoEstaCompletoSeMuestraElResumen() {
        // Preparación
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.armadoCompleto(armadoPcDtoMock)).thenReturn(true);

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.obtenerResumen(sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/resumen"));
        assertThat(mav.getModel().get("armadoPcDto"), is(armadoPcDtoMock));
        assertNull(mav.getModel().get("errorResumen"));
    }

    @Test
    public void cuandoElArmadoEstaIncompletoSeMuestraMensajeDeError() {
        // Preparación
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.armadoCompleto(armadoPcDtoMock)).thenReturn(false);

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.obtenerResumen(sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/resumen"));
        assertNull(mav.getModel().get("armadoPcDto"));
        assertThat(mav.getModel().get("errorResumen"), equalTo("Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado"));
    }

    // Test para reiniciarArmado
    @Test
    public void cuandoSeReiniciaElArmadoLaSessionSeLimpiaYRedirigeAlPrimerPaso() {
        // Ejecución
        ModelAndView mav = controladorArmaTuPc.reiniciarArmado(sessionMock);

        // Verificación
        verify(sessionMock, times(1)).removeAttribute("armadoPcDto");
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/procesador"));
    }

    // Tests para verificar las llamadas al ServicioPrecios
    @Test
    public void cuandoSeCarganComponentesSeConviertenSusPreciosAPesos() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "procesador";
        ComponenteDto comp1 = new ComponenteDto();
        comp1.setPrecio(100.0);
        ComponenteDto comp2 = new ComponenteDto();
        comp2.setPrecio(200.0);
        List<ComponenteDto> listaComponentes = List.of(comp1, comp2);

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenReturn(listaComponentes);
        when(servicioPreciosMock.conversionDolarAPeso(100.0)).thenReturn("$ 100.000,00");
        when(servicioPreciosMock.conversionDolarAPeso(200.0)).thenReturn("$ 200.000,00");

        // Ejecución
        controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación
        // Se debe llamar al servicio de precios una vez por cada componente
        verify(servicioPreciosMock, times(1)).conversionDolarAPeso(100.0);
        verify(servicioPreciosMock, times(1)).conversionDolarAPeso(200.0);
        assertThat(comp1.getPrecioFormateado(), is(equalTo("$ 100.000,00")));
        assertThat(comp2.getPrecioFormateado(), is(equalTo("$ 200.000,00")));
    }

    @Test
    public void cuandoSeAgregaUnComponenteSeActualizaElPrecioTotalDelArmadoEnPesos() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        Long idComponente = 1L;
        String tipoComponente = "procesador";
        Double precioTotalEnDolares = 550.5;
        String precioTotalEnPesos = "$ 550.500,50";

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class)))
                .thenReturn(armadoPcDtoMock);
        when(armadoPcDtoMock.getPrecioTotal()).thenReturn(precioTotalEnDolares);
        when(servicioPreciosMock.conversionDolarAPeso(precioTotalEnDolares)).thenReturn(precioTotalEnPesos);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(anyLong())).thenReturn(componenteDtoMock);

        // Ejecución
        controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, idComponente, 1, sessionMock);

        // Verificación
        // Se verifica que se llame al servicio de precios con el total del armado
        verify(servicioPreciosMock, times(1)).conversionDolarAPeso(precioTotalEnDolares);
        // Se verifica que el precio formateado se establezca en el DTO del armado
        verify(armadoPcDtoMock, times(1)).setPrecioFormateado(precioTotalEnPesos);
    }

    // Test para el método sumarArmadoAlCarrito
    @Test
    public void alSumarArmadoAlCarritoSeAgreganLosProductosALaSessionYRedirige() {
        // Preparación
        // Mocks para los DTOs que se agregarán al carrito
        ProductoCarritoDto producto1 = mock(ProductoCarritoDto.class);
        ProductoCarritoDto producto2 = mock(ProductoCarritoDto.class);
        List<ProductoCarritoDto> productosDelArmado = List.of(producto1, producto2);

        // Simular que ya hay un item en el carrito de la sesión
        ProductoCarritoDto productoExistente = mock(ProductoCarritoDto.class);
        List<ProductoCarritoDto> carritoActual = new ArrayList<>();
        carritoActual.add(productoExistente);

        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
        when(sessionMock.getAttribute("carritoSesion")).thenReturn(carritoActual);
        when(servicioArmaTuPcMock.pasajeAProductoDtoParaAgregarAlCarrito(armadoPcDtoMock)).thenReturn(productosDelArmado);

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.sumarArmadoAlCarrito(sessionMock);

        // Verificación

        verify(servicioArmaTuPcMock, times(1)).pasajeAProductoDtoParaAgregarAlCarrito(armadoPcDtoMock);
        // Verificar que la lista final en la sesión contiene todos los productos (los existentes + los nuevos)
        // El tamaño de la lista ahora debería ser 3
        assertThat(carritoActual, hasSize(3));
        assertThat(carritoActual, hasItems(productoExistente, producto1, producto2));
        assertThat(mav.getViewName(), equalTo("redirect:/carritoDeCompras/index"));
    }
}