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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioCompraTest {

    @Mock private RepositorioUsuario repositorioUsuario;
    @Mock private RepositorioCompra repositorioCompra;
    private ServicioCompraImpl servicioCompra;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        servicioCompra = new ServicioCompraImpl(repositorioCompra, repositorioUsuario);
    }

    @Test
    public void deberiaGuardarCompraConUsuarioLogueado() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto);

        verify(repositorioUsuario).buscarUsuario("test@example.com");
        verify(repositorioCompra).guardarCompraDeUsuario(any(Compra.class));
        verify(repositorioCompra).guardarComonentesEnCompraComponente(any(CompraComponente.class));
    }

    @Test
    public void deberiaGuardarMultiplesProductos() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(250.0, "mercadoPago",
                crearProducto(10L, 2, 100.0),
                crearProducto(20L, 1, 50.0));

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto);

        verify(repositorioCompra, times(2)).guardarComonentesEnCompraComponente(any(CompraComponente.class));
    }

    @Test
    public void deberiaGuardarProductoSinComponenteDto() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraComponenteDto producto = crearProductoSinComponenteDto(15L, 1, 75.0);
        CompraDto compraDto = crearCompraDto(75.0, "tarjetaCredito", producto);

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto);

        ArgumentCaptor<CompraComponente> captor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra).guardarComonentesEnCompraComponente(captor.capture());
        assertEquals(15L, captor.getValue().getComponente().getId());
    }

    //obtenerCompraComponenteDeUnUsuarioLogueado
    @Test
    public void deberiaObtenerComprasDeUsuario() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        List<Compra> comprasEsperadas = Arrays.asList(new Compra(), new Compra());
        when(repositorioCompra.obtenerCompraDeUsuarioLogueado(usuarioDto)).thenReturn(comprasEsperadas);

        List<Compra> resultado = servicioCompra.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioDto);

        assertEquals(comprasEsperadas, resultado);
        verify(repositorioCompra).obtenerCompraDeUsuarioLogueado(usuarioDto);
    }

    //metodos internos
    private UsuarioDto crearUsuarioDto(String email) {
        UsuarioDto usuario = new UsuarioDto();
        usuario.setEmail(email);
        return usuario;
    }

    private Usuario crearUsuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        return usuario;
    }

    private CompraDto crearCompraDto(Double total, String metodoPago, CompraComponenteDto... productos) {
        CompraDto compra = new CompraDto();
        compra.setFecha(LocalDate.now());
        compra.setTotal(total);
        compra.setMetodoDePago(metodoPago);
        compra.setProductosComprados(Arrays.asList(productos));
        return compra;
    }

    private CompraComponenteDto crearProducto(Long id, Integer cantidad, Double precio) {
        ComponenteDto componenteDto = new ComponenteDto();
        componenteDto.setId(id);

        CompraComponenteDto producto = new CompraComponenteDto();
        producto.setId(id);
        producto.setCantidad(cantidad);
        producto.setPrecioUnitario(precio);
        producto.setComponente(componenteDto);
        return producto;
    }

    private CompraComponenteDto crearProductoSinComponenteDto(Long id, Integer cantidad, Double precio) {
        CompraComponenteDto producto = new CompraComponenteDto();
        producto.setId(id);
        producto.setCantidad(cantidad);
        producto.setPrecioUnitario(precio);
        producto.setComponente(null);
        return producto;
    }
}