package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Categorias;
import com.tallerwebi.dominio.ServicioCategorias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorCategoriasTest {

    private ControladorCategorias controladorCategorias;
    private ServicioCategorias servicioCategoriasMock;
    private Model modelMock;

    @BeforeEach
    public void init() {
        servicioCategoriasMock = mock(ServicioCategorias.class);
        modelMock = mock(Model.class);
        controladorCategorias = new ControladorCategorias(servicioCategoriasMock);
    }

    @Test
    public void deberiaMostrarCategoriasEnLaVista() {
        // preparación
        List<Categorias> lista = List.of(
                crearCategoria(1L, "Electrónica", "electronica"),
                crearCategoria(2L, "Hogar", "hogar")
        );
        when(servicioCategoriasMock.listarCategorias()).thenReturn(lista);

        // ejecución
        String vista = controladorCategorias.mostrarCategoriasExistentes(modelMock);

        // validación
        assertThat(vista, equalToIgnoringCase("categorias"));
        verify(modelMock, times(1)).addAttribute("categorias", lista);
        verify(servicioCategoriasMock, times(1)).listarCategorias();
        verifyNoMoreInteractions(servicioCategoriasMock, modelMock);
    }

    @Test
    public void siNoHayCategorias_deberiaEntregarListaVacia() {
        // preparación
        when(servicioCategoriasMock.listarCategorias()).thenReturn(List.of());

        // ejecución
        String vista = controladorCategorias.mostrarCategoriasExistentes(modelMock);

        // validación
        assertThat(vista, equalToIgnoringCase("categorias"));
        verify(modelMock).addAttribute(eq("categorias"), eq(List.of()));
        verify(servicioCategoriasMock).listarCategorias();
        verifyNoMoreInteractions(servicioCategoriasMock, modelMock);
    }

    private Categorias crearCategoria(Long id, String nombre, String nombreEnUrl) {
        Categorias c = new Categorias();
        c.setId(id);
        c.setNombre(nombre);
        c.setNombreEnUrl(nombreEnUrl);
        return c;
    }

}
