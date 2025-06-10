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
        this.categorias.add(new CategoriaDto("Procesadores", "procesador.jpg"));
        this.categorias.add(new CategoriaDto("Perifericos", "perifericos.jpg"));
        this.categorias.add(new CategoriaDto("Fuentes", "fuentes.jpg"));
        this.categorias.add(new CategoriaDto("Memorias Ram", "ram.jpg"));
//        this.categorias.add(new CategoriaDto("Placa de video", "placadevideo.jpg"));
        this.categorias.add(new CategoriaDto("Almacenamiento", "almacenamiento.jpg"));
        this.categorias.add(new CategoriaDto("Mothers", "mother.jpg"));
        this.categorias.add(new CategoriaDto("Impresoras", "impresora.jpg"));
    }

    public List<CategoriaDto> getCategorias() {
        return categorias;
    }
}
