package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Almacenamiento;
import com.tallerwebi.dominio.entidades.MemoriaRAM;
import com.tallerwebi.dominio.entidades.Motherboard;
import com.tallerwebi.dominio.entidades.Procesador;

public interface ServicioMotherboard {
    Boolean verificarCompatibilidadDeMotherboardConProcesador(Motherboard motherboard, Procesador procesador);

    Boolean verificarCompatibilidadDeMotherboardConMemoriaRAM(Motherboard motherboard, MemoriaRAM memoriaRAM);

    Boolean verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(Motherboard motherboard, Almacenamiento almacenamiento);
}
