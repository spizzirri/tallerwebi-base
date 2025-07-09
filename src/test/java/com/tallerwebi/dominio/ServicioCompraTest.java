package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.CompraComponenteDto;
import com.tallerwebi.presentacion.CompraDto;
import com.tallerwebi.presentacion.UsuarioDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioCompraTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;
    @Mock
    private RepositorioCompra repositorioCompra;

    private ServicioCompraImpl servicioCompraImpl;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        servicioCompraImpl = new ServicioCompraImpl(repositorioCompra, repositorioUsuario);
        ReflectionTestUtils.setField(servicioCompraImpl, "repositorioUsuario", repositorioUsuario);
    }

    @Test
    public void deberiaPoderGuardarUnaCompraAUnUsuaarioLoguado() {
        String emailUsuario = "test@ejemplo.com";
        UsuarioDto usuarioDtoLogueado = new UsuarioDto();
        usuarioDtoLogueado.setEmail(emailUsuario);

        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setEmail(emailUsuario);
        usuarioEntidad.setId(1L);

        ComponenteDto componenteDto = new ComponenteDto();
        componenteDto.setId(10L);

        CompraComponenteDto producto1 = new CompraComponenteDto();
        producto1.setId(10L);
        producto1.setCantidad(2);
        producto1.setPrecioUnitario(100.0);
        producto1.setComponente(componenteDto);

        CompraComponenteDto producto2 = new CompraComponenteDto();
        producto2.setId(20L);
        producto2.setCantidad(1);
        producto2.setPrecioUnitario(50.0);
        producto2.setComponente(componenteDto);

        List<CompraComponenteDto> productosComprados = Arrays.asList(producto1, producto2);

        CompraDto compraDto = new CompraDto();
        compraDto.setFecha(LocalDate.now());
        compraDto.setTotal(250.0);
        compraDto.setMetodoDePago("tarjetaCredito");
        compraDto.setProductosComprados(productosComprados);

        when(repositorioUsuario.buscarUsuario(emailUsuario)).thenReturn(usuarioEntidad);

        servicioCompraImpl.guardarCompraConUsuarioLogueado(compraDto, usuarioDtoLogueado);

        verify(repositorioUsuario, times(1)).buscarUsuario(emailUsuario);

        //permite "capturar" y examinar los argumentos que se pasan a los metodos de los mocks
        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(repositorioCompra, times(1)).guardarCompraDeUsuario(compraCaptor.capture());
        //  Crea un "contenedor" que puede capturar objetos de tipo Compra
        //   usa capture() para "atrapar" el argumento

        Compra compraGuardada = compraCaptor.getValue();
        assertEquals(usuarioEntidad, compraGuardada.getIdUsuario());
        assertEquals(LocalDate.now(), compraGuardada.getFecha());
        assertEquals(250.0, compraGuardada.getTotal());
        assertEquals("tarjetaCredito", compraGuardada.getMetodoDePago());

        ArgumentCaptor<CompraComponente> componenteCaptor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra, times(2)).guardarComonentesEnCompraComponente(componenteCaptor.capture());

        List<CompraComponente> componentesGuardados = componenteCaptor.getAllValues();

        CompraComponente componente1 = componentesGuardados.get(0);
        assertEquals(2, componente1.getCantidad());
        assertEquals(100.0, componente1.getPrecioUnitario());
        assertEquals(compraGuardada, componente1.getCompra());
        assertEquals(10L, componente1.getComponente().getId());

        CompraComponente componente2 = componentesGuardados.get(1);
        assertEquals(1, componente2.getCantidad());
        assertEquals(50.0, componente2.getPrecioUnitario());
        assertEquals(compraGuardada, componente2.getCompra());
    }
}
