package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Imagen;
import com.tallerwebi.infraestructura.RepositorioComponenteImpl;
import com.tallerwebi.presentacion.CompraComponenteDto;
import com.tallerwebi.presentacion.CompraDto;
import com.tallerwebi.presentacion.UsuarioDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioCompraTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;
    @Mock
    private RepositorioCompra repositorioCompra;
    @Mock
    private HttpSession session;
    @Mock
    private RepositorioComponenteImpl repositorioComponente;

    private ServicioCompraImpl servicioCompra;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        servicioCompra = new ServicioCompraImpl(repositorioCompra, repositorioUsuario, repositorioComponente);
    }

    @Test
    public void deberiaGuardarCompraConUsuarioLogueado() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        Componente componenteMock = crearComponenteConImagenes(10L, "url-imagen.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        verify(repositorioUsuario).buscarUsuario("test@example.com");
        verify(repositorioCompra).guardarCompraDeUsuario(any(Compra.class));
        verify(repositorioCompra).guardarComonentesEnCompraComponente(any(CompraComponente.class));
        verify(repositorioComponente).obtenerComponentePorId(10L);
    }

    @Test
    public void deberiaGuardarMultiplesProductos() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(250.0, "mercadoPago",
                crearProducto(10L, 2, 100.0),
                crearProducto(20L, 1, 50.0));

        Componente componente1 = crearComponenteConImagenes(10L, "url1.jpg");
        Componente componente2 = crearComponenteConImagenes(20L, "url2.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componente1);
        when(repositorioComponente.obtenerComponentePorId(20L)).thenReturn(componente2);
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        verify(repositorioCompra, times(2)).guardarComonentesEnCompraComponente(any(CompraComponente.class));
        verify(repositorioComponente).obtenerComponentePorId(10L);
        verify(repositorioComponente).obtenerComponentePorId(20L);
    }

    @Test
    public void deberiaGuardarProductoSinComponenteDto() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraComponenteDto producto = crearProductoSinComponenteDto(15L, 1, 75.0);
        CompraDto compraDto = crearCompraDto(75.0, "tarjetaCredito", producto);

        Componente componenteMock = crearComponenteConImagenes(15L, "url-imagen.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(15L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        ArgumentCaptor<CompraComponente> captor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra).guardarComonentesEnCompraComponente(captor.capture());
        assertEquals(15L, captor.getValue().getComponente().getId());
        verify(repositorioComponente).obtenerComponentePorId(15L);
    }

    @Test
    public void deberiaGuardarCompraConCpDeSesion() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        Componente componenteMock = crearComponenteConImagenes(10L, "url-imagen.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn("5000"); // CP diferente

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        ArgumentCaptor<Compra> compraCaptor = ArgumentCaptor.forClass(Compra.class);
        verify(repositorioCompra).guardarCompraDeUsuario(compraCaptor.capture());

        verify(session).getAttribute("cp");
    }

    @Test
    public void deberiaUsarCpPorDefectoSiNoHayEnSesion() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        Componente componenteMock = crearComponenteConImagenes(10L, "url-imagen.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn(null); // No hay CP en sesión

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        verify(repositorioCompra).guardarCompraDeUsuario(any(Compra.class));
        verify(session).getAttribute("cp");
    }

    @Test
    public void deberiaSetearUrlImagenDeComponente() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        Componente componenteMock = crearComponenteConImagenes(10L, "url-imagen-test.jpg");

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        ArgumentCaptor<CompraComponente> captor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra).guardarComonentesEnCompraComponente(captor.capture());
        assertEquals("url-imagen-test.jpg", captor.getValue().getUrlImagen());
    }

    @Test
    public void deberiaSetearUrlImagenVaciaSiNoHayImagenes() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(10L, 2, 100.0));

        Componente componenteMock = crearComponenteSinImagenes(10L);

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(10L)).thenReturn(componenteMock);
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        ArgumentCaptor<CompraComponente> captor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra).guardarComonentesEnCompraComponente(captor.capture());
        assertEquals("", captor.getValue().getUrlImagen());
    }

    @Test
    public void deberiaCrearNuevoComponenteSiNoExisteEnRepositorio() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Usuario usuario = crearUsuario(1L, "test@example.com");
        CompraDto compraDto = crearCompraDto(200.0, "tarjetaCredito", crearProducto(999L, 2, 100.0));

        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuario);
        when(repositorioComponente.obtenerComponentePorId(999L)).thenReturn(null); // No existe
        when(session.getAttribute("cp")).thenReturn("1440");

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioDto, session);

        ArgumentCaptor<CompraComponente> captor = ArgumentCaptor.forClass(CompraComponente.class);
        verify(repositorioCompra).guardarComonentesEnCompraComponente(captor.capture());
        assertEquals(999L, captor.getValue().getComponente().getId());
        assertEquals("", captor.getValue().getUrlImagen()); // Sin imagen porque es nuevo
    }

    @Test
    public void deberiaObtenerComprasDeUsuario() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        List<Compra> comprasEsperadas = Arrays.asList(new Compra(), new Compra());
        when(repositorioCompra.obtenerCompraDeUsuarioLogueado(usuarioDto)).thenReturn(comprasEsperadas);

        List<Compra> resultado = servicioCompra.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioDto);

        assertEquals(comprasEsperadas, resultado);
        verify(repositorioCompra).obtenerCompraDeUsuarioLogueado(usuarioDto);
    }

    @Test
    public void deberiaObtenerUltimaCompraDeUsuario() {
        UsuarioDto usuarioDto = crearUsuarioDto("test@example.com");
        Compra compraEsperada = new Compra();
        when(repositorioCompra.obtenerUltimaCompraDeUsuarioLogueado(usuarioDto)).thenReturn(compraEsperada);

        Compra resultado = servicioCompra.obtenerUltimaCompraComponenteDeUnUsuarioLogueado(usuarioDto);

        assertEquals(compraEsperada, resultado);
        verify(repositorioCompra).obtenerUltimaCompraDeUsuarioLogueado(usuarioDto);
    }

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
        compra.setFecha(LocalDateTime.now());
        compra.setTotal(total);
        compra.setMetodoDePago(metodoPago);
        compra.setCp("1440"); // CP por defecto
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
        producto.setEsArmado(false);
        producto.setNumeroDeArmado(null);
        return producto;
    }

    private CompraComponenteDto crearProductoSinComponenteDto(Long id, Integer cantidad, Double precio) {
        CompraComponenteDto producto = new CompraComponenteDto();
        producto.setId(id);
        producto.setCantidad(cantidad);
        producto.setPrecioUnitario(precio);
        producto.setComponente(null);
        producto.setEsArmado(false);
        producto.setNumeroDeArmado(null);
        return producto;
    }

    private Componente crearComponenteConImagenes(Long id, String urlImagen) {
        Componente componente = new Componente();
        componente.setId(id);

        Imagen imagen = new Imagen();
        imagen.setUrlImagen(urlImagen);

        componente.setImagenes(Arrays.asList(imagen));
        return componente;
    }

    private Componente crearComponenteSinImagenes(Long id) {
        Componente componente = new Componente();
        componente.setId(id);
        componente.setImagenes(Arrays.asList()); // Lista vacía
        return componente;
    }
}