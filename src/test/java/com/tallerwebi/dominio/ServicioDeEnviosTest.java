package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.EnvioDto;
import com.tallerwebi.presentacion.OpcionEnvio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ServicioDeEnviosTest {

    @Mock
    private RestTemplate restTemplateMock;

    private ServicioDeEnviosImpl servicioDeEnvios;

    private String enviosUrl = "https://683b8d5e28a0b0f2fdc4ead9.mockapi.io/envios";

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        servicioDeEnvios = new ServicioDeEnviosImpl();
        // acceder y modificar campos privados
        ReflectionTestUtils.setField(servicioDeEnvios, "enviosUrl", enviosUrl);
        ReflectionTestUtils.setField(servicioDeEnvios, "restTemplate", restTemplateMock);

    }

    @Test
    public void cuandoCalculoEnvioConCodigoPostalValidoYHayCoberturaObtengoElEnvio(){
        String codigoPostal = "1704";
        String urlEsperada =  enviosUrl + "?codigoPostal=" + codigoPostal;

        OpcionEnvio[] opcionEnvio = {
                crearOpcionEnvio("Andreani", 1500.0, "2-3 dias habiles", "Ramos Mejia", "Buenos Aires")
        };

        when(restTemplateMock.getForObject(urlEsperada, OpcionEnvio[].class)).thenReturn(opcionEnvio);

        EnvioDto envioDto = servicioDeEnvios.calcularEnvio(codigoPostal);

        assertNotNull(envioDto);
        assertEquals(1500.0, envioDto.getCosto());
        assertEquals("2-3 dias habiles", envioDto.getTiempo());
        assertEquals("Ramos Mejia, Buenos Aires", envioDto.getDestino());
    }

    @Test
    public void cuandoCalculoEnvioConCodigoPostalSinCoberturaDevuelveNull(){
        String codigoPostal = "1704";
        String urlEsperada =  enviosUrl + "?codigoPostal=" + codigoPostal;

        OpcionEnvio[] opcionEnvioVacias = {};

        when(restTemplateMock.getForObject(urlEsperada, OpcionEnvio[].class)).thenReturn(opcionEnvioVacias);

        EnvioDto envioDto = servicioDeEnvios.calcularEnvio(codigoPostal);

        assertNull(envioDto);
    }

    @Test
    public void cuandoElCodigoPostalEsInvalidoDevuelveUnaExepcion(){
        String codigoPostal = "122";

        assertNull(servicioDeEnvios.calcularEnvio(codigoPostal));
    }

    @Test
    public void cuandoHayUnErrorEnLaLlamadaALaAPIDevuelveNull(){
        String codigoPostal = "1704";
        String urlEsperada =  enviosUrl + "?codigoPostal=" + codigoPostal;

        when(restTemplateMock.getForObject(urlEsperada, String.class)).thenThrow(new RuntimeException("Error"));

        EnvioDto envioDto = servicioDeEnvios.calcularEnvio(codigoPostal);

        assertNull(envioDto);
    }

    private OpcionEnvio crearOpcionEnvio(String empresa, Double costo, String tiempo,
                                         String localidad, String provincia) {
        OpcionEnvio opcion = new OpcionEnvio();
        opcion.setEmpresa(empresa);
        opcion.setCostoEnvio(costo);
        opcion.setTiempoEstimado(tiempo);
        opcion.setLocalidad(localidad);
        opcion.setProvincia(provincia);
        return opcion;
    }
}
