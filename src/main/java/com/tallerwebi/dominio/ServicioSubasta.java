package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioSubasta {
    void crearSubasta(Subasta subasta, String creador);
    List<Categorias> listarCategoriasDisponibles();
}
