package com.tallerwebi.dominio;

import java.time.LocalDateTime;

public interface RepositorioSubasta {
    void guardar(Subasta subasta);
    LocalDateTime obtenerTiempoFin(Integer indicador);
    Subasta obtenerSubasta(Long id);
}
