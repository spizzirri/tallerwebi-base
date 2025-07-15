package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ControladorArmaTuPcTest {

    private ServicioArmaTuPc servicioArmaTuPcMock;
    private ServicioPrecios servicioPreciosMock;
    private HttpSession sessionMock;
    private ControladorArmaTuPc controladorArmaTuPc;

    // DTOs y Mocks comunes
    private ArmadoPcDto armadoPcDtoMock;
    private ComponenteDto componenteDtoMock;

    @BeforeEach
    public void init() {
        // Mocks de los servicios y la sesión
        servicioArmaTuPcMock = mock(ServicioArmaTuPc.class);
        servicioPreciosMock = mock(ServicioPrecios.class);
        sessionMock = mock(HttpSession.class);

        // Mocks de DTOs para uso general
        armadoPcDtoMock = mock(ArmadoPcDto.class);
        componenteDtoMock = mock(ComponenteDto.class);

        // Se instancia el controlador con los mocks, reflejando el constructor real
        controladorArmaTuPc = new ControladorArmaTuPc(servicioArmaTuPcMock, servicioPreciosMock);

        // Comportamiento común para la obtención del DTO de la sesión
        when(sessionMock.getAttribute("armadoPcDto")).thenReturn(armadoPcDtoMock);
    }

    //region Tests para obtenerArmadoPcDtoDeLaSession (Lógica interna)

    @Test
    public void cuandoNoExisteUnArmadoEnLaSessionSeCreaUnoNuevoYSeGuarda() {
        // Preparación (Arrange)
        when(sessionMock.getAttribute("armadoPcDto"))
                .thenReturn(null)
                .thenReturn(new ArmadoPcDto()); // La segunda llamada devolverá el nuevo DTO

        // Ejecución (Act)
        controladorArmaTuPc.cargarComponentes("procesador", null, sessionMock);

        // Verificación (Assert)
        verify(sessionMock, times(1)).setAttribute(eq("armadoPcDto"), ArgumentMatchers.any(ArmadoPcDto.class));
    }

    @Test
    public void cuandoYaExisteUnArmadoEnLaSessionSeUtilizaElExistente() {
        // Preparación (Arrange)
        // El @BeforeEach ya establece que la sesión devolverá armadoPcDtoMock

        // Ejecución (Act)
        controladorArmaTuPc.cargarComponentes("procesador", null, sessionMock);

        // Verificación (Assert)
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), ArgumentMatchers.any(ArmadoPcDto.class));
        verify(sessionMock, atLeastOnce()).getAttribute("armadoPcDto");
    }

    //endregion

    //region Tests para cargarComponentes

    @Test
    public void cuandoSeCargaLaPaginaDeUnComponenteSeObtienenLosComponentesCompatibles() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación (Arrange)
        String tipoComponente = "motherboard";
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenReturn(new HashSet<>(List.of(componenteDtoMock)));
        when(armadoPcDtoMock.getComponentesDto()).thenReturn(Collections.emptyList());

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/motherboard"));
        assertThat(mav.getModel().get("componentesLista"), is(notNullValue()));
        assertThat((List<ComponenteDto>) mav.getModel().get("componentesLista"), hasSize(1));
        verify(servicioArmaTuPcMock).obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock);
        verify(servicioArmaTuPcMock, never()).obtenerListaDeComponentesCompatiblesFiltradosDto(anyString(), anyString(), ArgumentMatchers.any(ArmadoPcDto.class));
    }

    @Test
    public void cuandoSeCargaLaPaginaConQuerySeObtienenLosComponentesFiltrados() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación (Arrange)
        String tipoComponente = "gpu";
        String query = "Nvidia";
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDtoMock))
                .thenReturn(new HashSet<>(List.of(componenteDtoMock)));
        when(armadoPcDtoMock.getComponentesDto()).thenReturn(Collections.emptyList());

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, query, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/gpu"));
        verify(servicioArmaTuPcMock).obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDtoMock);
        verify(servicioArmaTuPcMock, never()).obtenerListaDeComponentesCompatiblesDto(anyString(), ArgumentMatchers.any(ArmadoPcDto.class));
    }

    @Test
    public void cuandoSeCargaLaPaginaDeMemoriasSeAgreganLosSlotsDeLaMotherboardAlModelo(){
        // Preparación (Arrange)
        String tipoComponente = "memoria";
        when(servicioArmaTuPcMock.obtenerSlotsRamDeMotherboard(any())).thenReturn(4);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getModel().get("slotsRamMotherboardElegida"), equalTo(4));
    }

    @Test
    public void cuandoSeCargaLaPaginaDeAlmacenamientoSeAgreganLosSlotsDeLaMotherboardAlModelo(){
        // Preparación (Arrange)
        String tipoComponente = "almacenamiento";
        when(servicioArmaTuPcMock.obtenerSlotsSataDeMotherboard(any())).thenReturn(6);
        when(servicioArmaTuPcMock.obtenerSlotsM2DeMotherboard(any())).thenReturn(2);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getModel().get("slotsSataMotherboardElegida"), equalTo(6));
        assertThat(mav.getModel().get("slotsM2MotherboardElegida"), equalTo(2));
    }

    @Test
    public void cuandoNoSeHaSeleccionadoUnComponenteDeterminanteSeMuestraUnError() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación (Arrange)
        String tipoComponente = "motherboard";
        String mensajeError = "Debe seleccionar un procesador primero";
        when(servicioArmaTuPcMock.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDtoMock))
                .thenThrow(new ComponenteDeterminateDelArmadoEnNullException(mensajeError));

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/motherboard"));
        assertThat(mav.getModel().get("errorLista"), equalTo(mensajeError));
        assertNull(mav.getModel().get("componentesLista"));
    }

    @Test
    public void cuandoSeCargaElPrimerPasoElPasoAnteriorEsNulo() {
        // Preparación (Arrange)
        String tipoComponente = "procesador";

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.cargarComponentes(tipoComponente, null, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getModel().get("pasoActual"), equalTo("procesador"));
        assertNull(mav.getModel().get("pasoAnterior"));
        assertThat(mav.getModel().get("pasoSiguiente"), equalTo("motherboard"));
    }

    //endregion

    //region Tests para procesarAccion

    @Test
    public void cuandoLaAccionEsInvalidaSeRedirigeConMensajeDeError(){
        // Preparación (Arrange)
        String tipoComponente = "procesador";

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.procesarAccion(tipoComponente, "accionInvalida", 1L, 1, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/" + tipoComponente));
        assertThat(mav.getModelMap().get("accionInvalida"), equalTo("Ingreso una accion invalida."));
    }

    //endregion

    //region Tests para agregarComponenteAlArmado

    @Test
    public void cuandoAgregoUnComponenteYNoSePuedenAgregarMasUnidadesSeRedirigeAlSiguientePaso() throws LimiteDeComponenteSobrepasadoEnElArmadoException, ComponenteSinStockPedidoException {
        // Preparación (Arrange)
        String tipoComponente = "procesador";
        Long idComponente = 1L;
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class))).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);
        when(componenteDtoMock.getModelo()).thenReturn("Intel Core i5");
        when(servicioArmaTuPcMock.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDtoMock)).thenReturn(false); // No se pueden agregar más

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, idComponente, 1, sessionMock);

        // Verificación (Assert)
        verify(sessionMock).setAttribute("armadoPcDto", armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/motherboard")); // Va al siguiente
        assertThat((String)mav.getModelMap().get("agregado"), containsString("Intel Core i5"));
    }

    @Test
    public void cuandoAgregoUnComponenteYSePuedenAgregarMasUnidadesSeRedirigeAlMismoPaso() throws LimiteDeComponenteSobrepasadoEnElArmadoException, ComponenteSinStockPedidoException {
        // Preparación (Arrange)
        String tipoComponente = "memoria";
        Long idComponente = 2L;
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class))).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);
        when(componenteDtoMock.getModelo()).thenReturn("Corsair Vengeance");
        when(servicioArmaTuPcMock.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDtoMock)).thenReturn(true); // Se pueden agregar más

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, idComponente, 1, sessionMock);

        // Verificación (Assert)
        verify(sessionMock).setAttribute("armadoPcDto", armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/memoria")); // Se queda en el mismo
        assertThat((String)mav.getModelMap().get("agregado"), containsString("Corsair Vengeance"));
    }

    @Test
    public void cuandoIntentoAgregarComponenteYSeSuperaElLimiteSeMuestraError() throws LimiteDeComponenteSobrepasadoEnElArmadoException, ComponenteSinStockPedidoException {
        // Preparación (Arrange)
        String tipoComponente = "memoria";
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class)))
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, 1L, 1, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/memoria"));
        assertThat(mav.getModelMap().get("errorLimite"), equalTo("Supero el limite de memoria de su armado"));
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), any());
    }

    @Test
    public void cuandoIntentoAgregarComponenteSinStockSeMuestraError() throws LimiteDeComponenteSobrepasadoEnElArmadoException, ComponenteSinStockPedidoException {
        // Preparación (Arrange)
        String tipoComponente = "procesador";
        String mensajeError = "No hay stock suficiente para el componente pedido";
        when(servicioArmaTuPcMock.agregarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class)))
                .thenThrow(new ComponenteSinStockPedidoException(mensajeError));

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.agregarComponenteAlArmado(tipoComponente, 1L, 1, sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/procesador"));
        assertThat(mav.getModelMap().get("errorLimite"), equalTo(mensajeError));
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), any());
    }

    //endregion

    //region Tests para quitarComponenteDelArmado

    @Test
    public void cuandoQuitoUnComponenteExitosamenteSeRedirigeAlMismoPaso() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación (Arrange)
        String tipoComponente = "gpu";
        Long idComponente = 5L;
        when(servicioArmaTuPcMock.quitarComponenteAlArmado(anyLong(), anyString(), anyInt(), ArgumentMatchers.any(ArmadoPcDto.class))).thenReturn(armadoPcDtoMock);
        when(servicioArmaTuPcMock.obtenerComponenteDtoPorId(idComponente)).thenReturn(componenteDtoMock);
        when(componenteDtoMock.getModelo()).thenReturn("NVIDIA RTX 3060");

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, 1, sessionMock);

        // Verificación (Assert)
        verify(sessionMock).setAttribute("armadoPcDto", armadoPcDtoMock);
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/gpu"));
        assertThat(mav.getModelMap().get("quitado"), equalTo("x1 NVIDIA RTX 3060 fue quitado del armado."));
    }

    //endregion

    //region Tests para obtenerResumen

    @Test
    public void cuandoElArmadoEstaCompletoSeMuestraElResumen() {
        // Preparación (Arrange)
        when(servicioArmaTuPcMock.armadoCompleto(armadoPcDtoMock)).thenReturn(true);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.obtenerResumen(sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/resumen"));
        assertThat(mav.getModel().get("armadoPcDto"), is(armadoPcDtoMock));
        assertNull(mav.getModel().get("errorResumen"));
    }

    @Test
    public void cuandoElArmadoEstaIncompletoSeMuestraMensajeDeError() {
        // Preparación (Arrange)
        when(servicioArmaTuPcMock.armadoCompleto(armadoPcDtoMock)).thenReturn(false);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.obtenerResumen(sessionMock);

        // Verificación (Assert)
        assertThat(mav.getViewName(), equalTo("arma-tu-pc/tradicional/resumen"));
        assertNull(mav.getModel().get("armadoPcDto"));
        assertThat((String)mav.getModel().get("errorResumen"), containsString("Seleccione almenos un motherboard, cpu, cooler y gabinete"));
    }

    //endregion

    //region Test para reiniciarArmado

    @Test
    public void cuandoSeReiniciaElArmadoSeLlamaAlServicioParaDevolverStockYSeLimpiaLaSession() {
        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.reiniciarArmado(sessionMock);

        // Verificación (Assert)
        verify(servicioArmaTuPcMock, times(1)).devolverStockDeArmado(armadoPcDtoMock);
        verify(sessionMock, times(1)).removeAttribute("armadoPcDto");
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/procesador"));
    }

    //endregion

    //region Tests para sumarArmadoAlCarrito

    @Test
    public void alSumarArmadoAUnCarritoExistenteSeAgreganLosProductos() {
        // Preparación (Arrange)
        ProductoCarritoArmadoDto productoArmado1 = mock(ProductoCarritoArmadoDto.class);
        ProductoCarritoArmadoDto productoArmado2 = mock(ProductoCarritoArmadoDto.class);
        List<ProductoCarritoArmadoDto> productosDelArmado = List.of(productoArmado1, productoArmado2);

        ProductoCarritoDto productoExistente = mock(ProductoCarritoDto.class);
        List<ProductoCarritoDto> carritoActual = new ArrayList<>();
        carritoActual.add(productoExistente);

        when(sessionMock.getAttribute("carritoSesion")).thenReturn(carritoActual);
        when(servicioArmaTuPcMock.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoMock)).thenReturn(productosDelArmado);
        when(sessionMock.getAttribute("reservaArmado")).thenReturn(productosDelArmado);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.sumarArmadoAlCarrito(sessionMock);


        assertThat(carritoActual, hasSize(3));
        assertThat(carritoActual, hasItems(productoExistente, productoArmado1, productoArmado2));
        assertThat(mav.getViewName(), equalTo("redirect:/carritoDeCompras/index"));
    }

    @Test
    public void alSumarArmadoSinCarritoExistenteSeCreaUnoNuevoYSeAgreganLosProductos() {
        // Preparación (Arrange)
        ProductoCarritoArmadoDto productoArmado1 = mock(ProductoCarritoArmadoDto.class);
        ProductoCarritoArmadoDto productoArmado2 = mock(ProductoCarritoArmadoDto.class);
        List<ProductoCarritoArmadoDto> productosDelArmado = List.of(productoArmado1, productoArmado2);

        when(sessionMock.getAttribute("carritoSesion")).thenReturn(null); // No hay carrito
        when(servicioArmaTuPcMock.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoMock)).thenReturn(productosDelArmado);
        when(sessionMock.getAttribute("reservaArmado")).thenReturn(productosDelArmado);

        // Ejecución (Act)
        ModelAndView mav = controladorArmaTuPc.sumarArmadoAlCarrito(sessionMock);


        verify(sessionMock, times(1)).removeAttribute("armadoPcDto");
        verify(sessionMock, times(1)).removeAttribute("reservaArmado");
        assertThat(mav.getViewName(), equalTo("redirect:/carritoDeCompras/index"));
    }

    //endregion

    @Test
    public void cuandoIntentoQuitarComponenteInvalidoSeMuestraError() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación
        String tipoComponente = "gpu";
        Long idComponente = 5L;
        Integer cantidad = 1;

        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock))
                .thenThrow(new QuitarComponenteInvalidoException());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/" + tipoComponente));
        assertThat(mav.getModelMap().get("errorQuitado"), equalTo("No es posible quitar un componente que no fue agregado al armado."));
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), any());
    }

    @Test
    public void cuandoIntentoQuitarMasStockDelDisponibleSeMuestraError() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación
        String tipoComponente = "gpu";
        Long idComponente = 5L;
        Integer cantidad = 2;

        when(servicioArmaTuPcMock.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, armadoPcDtoMock))
                .thenThrow(new QuitarStockDemasDeComponenteException());

        // Ejecución
        ModelAndView mav = controladorArmaTuPc.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, sessionMock);

        // Verificación
        assertThat(mav.getViewName(), equalTo("redirect:/arma-tu-pc/tradicional/" + tipoComponente));
        assertThat(mav.getModelMap().get("errorQuitado"), equalTo("No es posible quitar una cantidad del componente que no posee el armado."));
        verify(sessionMock, never()).setAttribute(eq("armadoPcDto"), any());
    }
}