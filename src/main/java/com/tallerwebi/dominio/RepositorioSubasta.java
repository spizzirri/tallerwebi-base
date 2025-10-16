package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioSubasta {
    void guardar(Subasta subasta);
    LocalDateTime obtenerTiempoFin(Integer indicador);
    Subasta obtenerSubasta(Long id);
    List<Subasta> buscarSubastasPorCreador(String emailCreador);
}
