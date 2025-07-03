package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.infraestructura.config.HibernateConfigTestConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateConfigTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // puse esta anotacion para que el contexto del hibernate se recargue en cada test que se ejecute y no acumule ID entre test, nose si es lo mejor
@Transactional
public class RepositorioComponenteImplTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioComponenteImpl repositorioComponente;

    @BeforeEach
    public void init() {
        this.repositorioComponente = new RepositorioComponenteImpl(sessionFactory);
    }


    @Test
    @Rollback
    public void cuandoPidoComponentesDeTipoProcesadorEntoncesObtengoUnaListaDeComponentesQueSonInstanciasDeProcesador(){

        // Preparacion

        Session session = sessionFactory.getCurrentSession();

        Socket socketLGA1700 = new Socket(null, "LGA1700");
        Socket socketAM4 = new Socket(null, "AM4");
        // guardo primero los sockets
        session.save(socketLGA1700);
        session.save(socketAM4);

        Procesador procesador1 = new Procesador(
                null, "Intel Core i5", 150000.0, 10, "Intel",
                "i5-12400F", socketLGA1700, "10 nm", false, null, "Core i5",
                6, 12, "2.5 GHz", "4.4 GHz", true, "65W",
                "480KB", "7.5MB", "18MB"
        );
        procesador1.getImagenes().add(new Imagen(null, "intel-core-i5-1.jpg", procesador1));

        Procesador procesador2 = new Procesador(
                null, "AMD Ryzen 5", 140000.0, 8, "AMD",
                "Ryzen 5 5600X", socketAM4, "7 nm", false, null, "Ryzen 5",
                6, 12, "3.7 GHz", "4.6 GHz", true, "65W",
                "384KB", "3MB", "32MB"
        );
        procesador2.getImagenes().add(new Imagen(null, "amd-ryzen-5-1.jpg", procesador2));

        Motherboard mother1 = new Motherboard(
                null, "ASUS TUF Gaming B550-PLUS", 100000.0, 12, "ASUS",
                socketAM4, "B550", "AMD", "ATX",
                2, 4, 8,
                "DDR4", 4,
                "50W", 1, 2
        );
        mother1.getImagenes().add(new Imagen(null, "asus-b550-1.jpg", mother1));


        // guardo luego los componentes
        session.save(procesador1);
        session.save(procesador2);
        session.save(mother1);

        // Ejecucion

        List<Componente> listaObtenida = this.repositorioComponente.obtenerComponentesPorTipo(Procesador.class.getSimpleName());

        // Validacion

        assertFalse(listaObtenida.isEmpty());
        assertThat(listaObtenida, everyItem(instanceOf(Procesador.class)));
        assertThat(listaObtenida, hasSize(2));
        assertThat(listaObtenida, not(hasItem(mother1)));
    }

    @Test
    @Rollback
    public void cuandoPidoUnComponentePorIdEntoncesObtengoUnComponenteQueCorrespondeConElIdIngresado(){
        // Preparacion
        Session session = sessionFactory.getCurrentSession();

        Socket socketLGA1700 = new Socket(null, "LGA1700");
        session.save(socketLGA1700);

        Procesador procesador1 = new Procesador(
                null, "Intel Core i5", 150000.0, 10, "Intel",
                "i5-12400F", socketLGA1700, "10 nm", false, null, "Core i5",
                6, 12, "2.5 GHz", "4.4 GHz", true, "65W",
                "480KB", "7.5MB", "18MB"
        );
        session.save(procesador1);

        // Ejecucion
        Componente componenteObtenido = this.repositorioComponente.obtenerComponentePorId(1L);

        // Validacion

        Componente componenteEsperado = new Procesador();
        componenteEsperado.setId(1L);

        assertEquals(componenteEsperado, componenteObtenido);
    }

    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoComponentesPorTipoDeberiaObtenerComponentesPorTipo() {

        List<Componente> resultado = repositorioComponente.obtenerComponentesPorTipo("Procesador");

        assertFalse(resultado.isEmpty());
        assertThat(resultado, everyItem(instanceOf(Procesador.class)));
        assertTrue(resultado.get(0).getId() == 1L);
    }
    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoComponentesEnStockMeTraeComponentesConStockMayorACero() {

        List<Componente> resultado = repositorioComponente.obtenerComponentesEnStock();

        assertFalse(resultado.isEmpty());

        assertTrue(resultado.get(0).getStock() > 0);
    }
    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoObtenerComponentesMeTraeComponentes() {

        List<Componente> resultado = repositorioComponente.obtenerComponentes();

        assertFalse(resultado.isEmpty());
    }
    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoObtenerComponentesPorQueryMeTraeComponentesDeEsaQuery() {


        List<Componente> resultado = repositorioComponente.obtenerComponentesPorQuery("Intel");
        String nombreEsperado = "Intel";
        assertFalse(resultado.isEmpty());

       assertTrue(resultado.get(0).getNombre().contains(nombreEsperado));
    }
    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoObtenerComponentesMenoresAUnPrecioObtieneEsePrecicioPorParametroYDevuelveArticulosMenoresAEsePrecio() {


        List<Componente> resultado = repositorioComponente.obtenerComponentesMenoresDelPrecioPorParametro(100000D);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.get(0).getPrecio()<100000D);
    }
    @Sql("/data.sql")
    @Test
    @Rollback
    public void CuandoLePidoObtenerComponentesPorTipoYPorQueryMeDevuelveComponenteDeEseTipoYEsaQuery() throws ClassNotFoundException {

        Componente claseEsperado = new Procesador();
        List<Componente> resultado = repositorioComponente.obtenerComponentesPorTipoYPorQuery("Procesador","Intel");
        String nombreEsperado = "Intel";

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.get(0).getNombre().contains(nombreEsperado));
        assertEquals(resultado.get(0).getClass(), claseEsperado.getClass());
    }


    @Sql("/data.sql")
    @Test
    @Rollback
    public void cuandoPidoComponentesPorTipoEnStockDevuelveSoloLosDeEseTipoConStockPositivo() {
        // Ejecución
        List<Componente> resultado = repositorioComponente.obtenerComponentesPorTipoEnStock("PlacaDeVideo");

        // Verificación
        assertFalse(resultado.isEmpty());
        assertEquals(98L, resultado.get(0).getId()); // Verifica que sea la 'Placa de Video Zotac GeForce RTX 3060 12GB GDDR6 Twin Edge'
        assertThat(resultado, everyItem(instanceOf(PlacaDeVideo.class)));
        assertThat(resultado, everyItem(hasProperty("stock", greaterThan(0))));
    }


    @Sql("/data.sql")
    @Test
    @Rollback
    public void cuandoPidoComponentesPorTipoEnStockOrdenadosPorPrecioDevuelveListaOrdenadaAscendentemente() {
        // Ejecución
        List<Componente> resultado = repositorioComponente.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Procesador");

        // Verificación
        assertFalse(resultado.isEmpty());
        // El Procesador con ID 2 ('AMD Ryzen 5 5600X') tiene menor precio que el ID 1 ('Intel Core i5-12400F')
        assertEquals(2L, resultado.get(0).getId());
        assertEquals(1L, resultado.get(1).getId());
        assertThat(resultado.get(0).getPrecio(), lessThan(resultado.get(1).getPrecio()));
    }


    @Sql("/data.sql")
    @Test
    @Rollback
    public void cuandoPidoComponentesFiltradosPorNombreYTipoConStockDevuelveListaCorrectaYOrdenada() {
        // Ejecución: Buscamos todos los procesadores con stock que contengan "Ryzen" en su nombre.
        // En data.sql solo hay uno: 'AMD Ryzen 5 5600X'
        List<Componente> resultado = repositorioComponente.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio("Procesador", "Ryzen");

        // Verificación
        assertFalse(resultado.isEmpty());

        Componente componenteEncontrado = resultado.get(0);
        assertEquals(2L, componenteEncontrado.getId());
        assertThat(componenteEncontrado.getNombre(), containsStringIgnoringCase("Ryzen"));
        assertThat(componenteEncontrado.getStock(), greaterThan(0));
    }

    @Sql("/data.sql")
    @Test
    @Rollback
    public void cuandoDescuentoStockDeUnComponenteObtengoUnResultadoExitoso(){
        Long componenteId = 1L;
        Integer cantidadARestar = 1;

        Boolean resultado = repositorioComponente.descontarStockDeUnComponente(componenteId, cantidadARestar);

        assertTrue(resultado);
    }

    @Sql("/data.sql")
    @Test
    @Rollback
    public void cuandoDevuelvoStockDeUnComponenteObtengoUnResultadoExitoso(){
        Long componenteId = 1L;
        Integer cantidadASumar = 1;

        Boolean resultado = repositorioComponente.devolverStockDeUnComponente(componenteId, cantidadASumar);

        assertTrue(resultado);
    }
}

