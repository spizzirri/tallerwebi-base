package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.infraestructura.config.HibernateConfigTestConfig;
import com.tallerwebi.presentacion.UsuarioDto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateConfigTestConfig.class}) // Podés omitir esto si no usás Spring
public class RepositorioCompraImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session sessionMock;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @InjectMocks
    private RepositorioCompraImpl repositorioCompraImpl;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deberiaGuardarLaCompraDeUnUsuario() {
        when(sessionFactory.getCurrentSession()).thenReturn(sessionMock);

        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("123");

        Compra compra = new Compra();
        compra.setIdUsuario(usuario);
        compra.setFecha(LocalDate.now());
        compra.setTotal(999.0);
        compra.setMetodoDePago("tarjetaCredito");

        repositorioCompraImpl.guardarCompraDeUsuario(compra);

        verify(sessionMock).save(compra);
    }

    @Test
    public void deberiaObtenerComprasDeUsuario() {
        when(sessionFactory.getCurrentSession()).thenReturn(sessionMock);

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setEmail("test@example.com");

        Usuario usuarioMock = new Usuario();
        when(repositorioUsuario.buscarUsuario("test@example.com")).thenReturn(usuarioMock);

        Query queryMock = mock(Query.class);
        when(sessionMock.createQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);

        List<Compra> listaDeCompras = List.of(new Compra());
        when(queryMock.getResultList()).thenReturn(listaDeCompras);

        List<Compra> resultado = repositorioCompraImpl.obtenerCompraDeUsuarioLogueado(usuarioDto);

        assertFalse(resultado.isEmpty());
    }
}
