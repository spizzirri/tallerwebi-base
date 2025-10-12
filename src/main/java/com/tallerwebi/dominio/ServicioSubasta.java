package com.tallerwebi.dominio;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServicioSubasta {
    void crearSubasta(Subasta subasta, String creador, MultipartFile imagen) throws IOException;
    List<Categoria> listarCategoriasDisponibles();
    Subasta buscarSubasta(Long idSubasta);
}
