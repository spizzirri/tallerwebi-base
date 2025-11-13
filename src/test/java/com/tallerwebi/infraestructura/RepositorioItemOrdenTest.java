package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import javax.persistence.Query;
import javax.transaction.Transactional;

import com.tallerwebi.dominio.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestInfraestructuraConfig.class })
@Transactional
public class RepositorioItemOrdenTest {
    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioOrden repositorioOrden;
    private RepositorioItem repositorioItem;

    @BeforeEach
    void setUp() throws Exception {
        repositorioItem = new RepositorioItemImpl(sessionFactory);
        repositorioOrden = new RepositorioOrdenImpl(sessionFactory);
    }

    @Test
    public void deberiaGuardarItemConOrdenUsandoRepositorios() {
        Orden orden = new Orden();
        orden.setDescripcion("Compara de perifericos");
        repositorioOrden.guardar(orden);

        Item item = new Item();
        item.setNombre("Mouse");
        item.setCantidad(1);
        item.setOrden(orden);

        repositorioItem.guardar(item);

        Item encontrado = repositorioItem.buscarPorId(item.getId());

        assertThat(encontrado, is(notNullValue()));
        assertThat(encontrado.getOrden(), is(notNullValue()));
        assertThat(encontrado.getOrden().getDescripcion(), is("Compara de perifericos"));
    }

    @Test
    public void deberiaObtenerElPedidoDeUnArticulo() {
        Orden orden = new Orden();
        orden.setDescripcion("Cliente A");
        Item item = new Item();
        item.setNombre("Jugo");
        item.setCantidad(2);

        orden.agregarItem(item);

        repositorioOrden.guardar(orden);

        Item itemPersistido = repositorioItem.buscarPorId(item.getId());
        assertThat(itemPersistido.getOrden(), is(notNullValue()));
        assertThat(itemPersistido.getOrden().getDescripcion(), is("Cliente A"));
    }

    @Test
    public void deberiaAgregarYEliminarItemsDeUnPedido() {

        Orden orden = new Orden();
        orden.setDescripcion("Cliente B");

        Item item1 = new Item();
        item1.setNombre("Creatina");
        item1.setCantidad(1);

        Item item2 = new Item();
        item2.setNombre("barra proteica");
        item2.setCantidad(3);

        orden.agregarItem(item1);
        repositorioOrden.guardar(orden);

        // Verificar que se guardó un solo item

        Orden ordenPersistido = repositorioOrden.buscarPorId(orden.getId());
        assertThat(ordenPersistido.getItems(), hasSize(1));

        // Agregamos un segundo item y volvemos a guardar

        ordenPersistido.agregarItem(item2);
        repositorioOrden.guardar(ordenPersistido);

        Orden ordenConDosItems = repositorioOrden.buscarPorId(orden.getId());
        assertThat(ordenConDosItems.getItems(), hasSize(2));

        // Quitamos un item y verificamos

        Item itemAEliminar = ordenConDosItems.getItems().get(0);
        ordenConDosItems.eliminarItem(itemAEliminar);

        repositorioOrden.guardar(ordenConDosItems);

        Orden ordenFinal = repositorioOrden.buscarPorId(orden.getId());

        assertThat(ordenFinal.getItems(), hasSize(1));
    }
}
