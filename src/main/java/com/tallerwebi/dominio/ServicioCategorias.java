package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.CategoriaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("servicioCategorias")
@Transactional
public class ServicioCategorias {
    private List<CategoriaDto> categorias;

    public ServicioCategorias() {
        this.categorias = new ArrayList<>();
        this.categorias.add(new CategoriaDto("Procesador", "Procesadores", "procesador.jpg"));
        this.categorias.add(new CategoriaDto("Periferico", "Perifericos", "perifericos.jpg"));
        this.categorias.add(new CategoriaDto("FuenteDeAlimentacion","Fuentes de alimentacion", "fuentes.jpg"));
        this.categorias.add(new CategoriaDto("MemoriaRAM","Memoria ram", "ram.jpg"));
        this.categorias.add(new CategoriaDto("PlacaDeVideo", "Placa de video", "placadevideo.jpg"));
        this.categorias.add(new CategoriaDto("Almacenamiento", "Almacenamiento", "almacenamiento.jpg"));
        this.categorias.add(new CategoriaDto("Motherboard", "Mothers","mother.jpg"));
        this.categorias.add(new CategoriaDto("Monitor", "Monitores", "monitor.jpg"));
        this.categorias.add(new CategoriaDto("CoolerCPU", "Coolers", "impresora.jpg"));
        this.categorias.add(new CategoriaDto("Gabinete", "Gabinetes", "gabinete.jpg"));
    }

    public List<CategoriaDto> getCategorias() {
        return categorias;
    }
}
