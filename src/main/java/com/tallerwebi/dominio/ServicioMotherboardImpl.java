package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Almacenamiento;
import com.tallerwebi.dominio.entidades.MemoriaRAM;
import com.tallerwebi.dominio.entidades.Motherboard;
import com.tallerwebi.dominio.entidades.Procesador;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioMotherboard")
@Transactional
public class ServicioMotherboardImpl implements ServicioMotherboard {
    @Override
    public Boolean verificarCompatibilidadDeMotherboardConProcesador(Motherboard motherboard, Procesador procesador) {
        return motherboard != null && procesador != null && motherboard.getSocket().equals(procesador.getSocket());
    }

    @Override
    public Boolean verificarCompatibilidadDeMotherboardConMemoriaRAM(Motherboard motherboard, MemoriaRAM memoriaRAM) {
        return motherboard != null && memoriaRAM != null && motherboard.getTipoMemoria().equals(memoriaRAM.getTecnologiaRAM());
    }

    @Override
    public Boolean verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(Motherboard motherboard, Almacenamiento almacenamiento) {
        return motherboard != null && almacenamiento != null
                && (motherboard.getCantSlotsM2() > 0 && almacenamiento.getTipoDeConexion().equals("M2")
                || motherboard.getCantPuertosSata() > 0 && almacenamiento.getTipoDeConexion().equals("SATA"));
    }
}
