package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.CategoriaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("serviceCategorias")
@Transactional
public class ServiceCategorias {
    private List<CategoriaDto> categorias;

    public ServiceCategorias() {
        this.categorias = new ArrayList<>();
        this.categorias.add(new CategoriaDto("Procesador", "Procesadores", "procesador.jpg"));
        this.categorias.add(new CategoriaDto("Periferico", "perifericos", "perifericos.jpg"));
        this.categorias.add(new CategoriaDto("FuenteDeAlimentacion","Fuentes de alimentacion", "fuentes.jpg"));
        this.categorias.add(new CategoriaDto("MemoriaRAM","Memoria ram", "ram.jpg"));
        this.categorias.add(new CategoriaDto("PlacaDeVideo", "Placa de video", "placadevideo.jpg"));
        this.categorias.add(new CategoriaDto("Almacenamiento", "Almacenamiento", "almacenamiento.jpg"));
        this.categorias.add(new CategoriaDto("Motherboard", "Mothers","mother.jpg"));
        this.categorias.add(new CategoriaDto("Monitor", "Monitores", "monitor.jpg"));
        this.categorias.add(new CategoriaDto("CoolerCPU", "Coolers", "impresora.jpg"));
    }

    public List<CategoriaDto> getCategorias() {
        return categorias;
    }
}
