package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioProductoCarritoImpl;
import com.tallerwebi.dominio.ServicioTarjetaDeCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ControladorTarjetaDeCreditoTest {

    @Mock
    private ServicioTarjetaDeCredito servicioTarjetaMock;
    @Mock
    private ServicioProductoCarritoImpl productoServiceMock;

    private ControladorTarjetaDeCredito controlador;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        controlador = new ControladorTarjetaDeCredito();
        controlador.servicioTarjeta = servicioTarjetaMock;
        controlador.productoService = productoServiceMock;
    }

    @Test
    public void siTodosLosCamposSonValidosRedirigeAPagoExitoso() {
        Mockito.when(servicioTarjetaMock.validarLongitudDeNumeroDeTarjeta(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.validarVencimiento(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.codigoDeSeguridad(Mockito.any())).thenReturn(true);

        ModelAndView vista = controlador.validar("1234567890123456", "12/30", "123");

        assertThat(vista.getViewName(), equalTo("pagoExitoso"));
        assertThat(vista.getModelMap().get("fechaCompra"), notNullValue());
        assertThat(((TarjetaDeCreditoDto) vista.getModelMap().get("tarjeta")).getNumeroDeTarjeta(), equalTo("1234567890123456"));
    }

    @Test
    public void siNumeroDeTarjetaEsInvalidoMuestraError() {
        Mockito.when(servicioTarjetaMock.validarLongitudDeNumeroDeTarjeta(Mockito.any())).thenReturn(false);
        Mockito.when(servicioTarjetaMock.validarVencimiento(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.codigoDeSeguridad(Mockito.any())).thenReturn(true);

        ModelAndView vista = controlador.validar("111", "12/30", "123");

        assertThat(vista.getViewName(), equalTo("tarjetaDeCredito"));
        assertThat(vista.getModelMap().get("mensajeTarjeta"), equalTo("Número de tarjeta inválido"));
    }

    @Test
    public void siFechaVencimientoEsInvalidaMuestraError() {
        Mockito.when(servicioTarjetaMock.validarLongitudDeNumeroDeTarjeta(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.validarVencimiento(Mockito.any())).thenReturn(false);
        Mockito.when(servicioTarjetaMock.codigoDeSeguridad(Mockito.any())).thenReturn(true);

        ModelAndView vista = controlador.validar("1234567890123456", "00/00", "123");

        assertThat(vista.getViewName(), equalTo("tarjetaDeCredito"));
        assertThat(vista.getModelMap().get("mensajeVencimiento"), equalTo("Fecha de vencimiento inválida"));
    }

    @Test
    public void siCodigoSeguridadEsInvalidoMuestraError() {
        Mockito.when(servicioTarjetaMock.validarLongitudDeNumeroDeTarjeta(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.validarVencimiento(Mockito.any())).thenReturn(true);
        Mockito.when(servicioTarjetaMock.codigoDeSeguridad(Mockito.any())).thenReturn(false);

        ModelAndView vista = controlador.validar("1234567890123456", "12/30", "12");

        assertThat(vista.getViewName(), equalTo("tarjetaDeCredito"));
        assertThat(vista.getModelMap().get("mensajeCodigo"), equalTo("Código de seguridad inválido"));
    }

    @Test
    public void siDosCamposSonInvalidosSeMuestranAmbosErrores() {
        Mockito.when(servicioTarjetaMock.validarLongitudDeNumeroDeTarjeta(Mockito.any())).thenReturn(false);
        Mockito.when(servicioTarjetaMock.validarVencimiento(Mockito.any())).thenReturn(false);
        Mockito.when(servicioTarjetaMock.codigoDeSeguridad(Mockito.any())).thenReturn(true);

        ModelAndView vista = controlador.validar("111", "01/20", "123");

        assertThat(vista.getViewName(), equalTo("tarjetaDeCredito"));
        assertThat(vista.getModelMap().get("mensajeTarjeta"), equalTo("Número de tarjeta inválido"));
        assertThat(vista.getModelMap().get("mensajeVencimiento"), equalTo("Fecha de vencimiento inválida"));
        assertThat(vista.getModelMap().containsKey("mensajeCodigo"), is(false)); // no debería haber error de código
    }

    @Test
    public void FormularioTarjetaDevuelveVistaCorrecta() {
        ModelAndView vista = controlador.mostrarFormularioTarjeta();

        assertThat(vista.getViewName(), equalTo("tarjetaDeCredito"));
    }
}
