package com.tallerwebi.presentacion;

import com.google.common.base.Verify;
import com.tallerwebi.dominio.RepositorioCompra;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioCompraImpl;
import com.tallerwebi.dominio.entidades.Compra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ControladorHistorialComprasTest {

    @Mock
    private ServicioCompraImpl servicioCompraMock;
    @Mock
    private HttpSession httpSessionMock;

    @Mock
    private RepositorioUsuario repositorioUsuarioMock;

    @Mock
    private RepositorioCompra repositorioCompraMock;

    private ControladorHistorialCompras controladorHistorialCompras;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this); // o initMocks (deprecated)
        controladorHistorialCompras = new ControladorHistorialCompras(servicioCompraMock);
    }

    @Test
    public void cuandoVerElHistorialDeComprasObtengoLaVistaDelHistorial() {
        UsuarioDto usuarioMock = new UsuarioDto();
        usuarioMock.setEmail("test@example.com");

        Compra compra = new Compra();
        List<Compra> comprasUsuario = List.of(compra);

        Mockito.when(httpSessionMock.getAttribute("usuario")).thenReturn(usuarioMock);
        Mockito.when(servicioCompraMock.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioMock))
                .thenReturn(comprasUsuario);

        ModelAndView mostrarVista = controladorHistorialCompras.mostrarHistorialDeCompras(httpSessionMock);
        ModelMap model = (ModelMap) mostrarVista.getModel();

        assertThat(mostrarVista.getViewName(), equalTo("historialDeCompras"));
        assertThat(model.get("comprasUsuario"), equalTo(comprasUsuario));
        assertThat(model.get("cantidadDeCompras"), equalTo(1));
    }

    @Test
    public void cuandoQuieroVerLasComprasDelUsuarioObtengoLaVistaConLasComprasPorUsuario() {
        UsuarioDto usuarioMock = new UsuarioDto();
        usuarioMock.setEmail("test@example.com");

        Compra compra = new Compra();
        List<Compra> comprasUsuario = List.of(compra);

        Mockito.when(httpSessionMock.getAttribute("usuario")).thenReturn(usuarioMock);
        Mockito.when(servicioCompraMock.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioMock))
                .thenReturn(comprasUsuario);

        Map<String, Object> response = controladorHistorialCompras.mostrarComprasUsuario(httpSessionMock);

        assertThat(response.get("success"), equalTo(true));
        assertThat(response.get("comprasUsuario"), equalTo(comprasUsuario));
    }

}
