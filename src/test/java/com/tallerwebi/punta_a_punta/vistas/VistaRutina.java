package com.tallerwebi.punta_a_punta.vistas;
import com.microsoft.playwright.Page;
import com.tallerwebi.dominio.rutina.TipoRutina;

import java.util.ArrayList;
import java.util.List;

public class VistaRutina extends VistaWeb {

    public VistaRutina(Page page) {
        super(page);
        page.navigate("localhost:8080/spring/rutina");
    }


    // Método para obtener otros elementos específicos de la Rutina
    public String obtenerNombreRutina() {
        return obtenerTextoDelElemento("selectorCSS_nombreRutina");  // Reemplazar con el selector real
    }

    public List<String> obtenerEjercicios() {
        // Implementar la lógica para obtener una lista de elementos de ejercicios (Ej: lista, tabla)
        List<String> ejercicios = new ArrayList<>();
        // Obtener elementos usando selectores CSS y agregarlos a la lista
        return ejercicios;
    }

    // Otros métodos para interactuar con elementos específicos de la Rutina (opcional)
    // ...
}

