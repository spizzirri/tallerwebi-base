package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository("repositorioComponente")
public class RepositorioComponenteImpl implements RepositorioComponente {

    private List<Componente> componentes;


    public RepositorioComponenteImpl() {

        this.componentes = Arrays.asList(
                // Procesadores
                new Componente(1L, "Intel Core i5", 150000.0, 10),
                new Componente(2L, "AMD Ryzen 5", 140000.0, 8),
                new Componente(3L, "Intel Core i7", 200000.0, 5),

                // Motherboards
                new Componente(4L, "ASUS Prime B550", 100000.0, 12),
                new Componente(5L, "Gigabyte B450", 95000.0, 9),
                new Componente(6L, "MSI Z490", 120000.0, 6),

                // Coolers
                new Componente(7L, "Cooler Master Hyper 212", 30000.0, 15),
                new Componente(8L, "Noctua NH-U12S", 40000.0, 10),
                new Componente(9L, "DeepCool Gammaxx", 25000.0, 20),

                // Memorias RAM
                new Componente(10L, "Corsair Vengeance 8GB", 20000.0, 25),
                new Componente(11L, "Kingston HyperX 16GB", 35000.0, 18),
                new Componente(12L, "G.Skill Ripjaws 8GB", 22000.0, 30),

                // GPU
                new Componente(13L, "NVIDIA RTX 3060", 300000.0, 5),
                new Componente(14L, "AMD Radeon RX 6600", 280000.0, 7),
                new Componente(15L, "NVIDIA GTX 1660", 200000.0, 9),

                // Almacenamiento
                new Componente(16L, "Samsung SSD 500GB", 35000.0, 20),
                new Componente(17L, "WD HDD 1TB", 25000.0, 18),
                new Componente(18L, "Crucial SSD 1TB", 60000.0, 10),

                // Fuente
                new Componente(19L, "EVGA 600W", 40000.0, 14),
                new Componente(20L, "Corsair 750W", 50000.0, 10),
                new Componente(21L, "Gigabyte 650W", 45000.0, 11),

                // Gabinete
                new Componente(22L, "NZXT H510", 60000.0, 9),
                new Componente(23L, "Corsair Carbide", 55000.0, 7),
                new Componente(24L, "Cooler Master Q300L", 50000.0, 10),

                // Monitor
                new Componente(25L, "Samsung 24", 85000.0, 6),
                new Componente(26L, "LG 27", 110000.0, 4),
                new Componente(27L, "AOC 22", 70000.0, 8),

                // Periféricos
                new Componente(28L, "Teclado Redragon", 15000.0, 20),
                new Componente(29L, "Mouse Logitech", 12000.0, 25),
                new Componente(30L, "Auriculares HyperX", 30000.0, 15)
        );


    }

    @Override
    public List<Componente> obtenerComponentesPorTipo(String tipo) {
        //return this.componentes.stream().filter(c -> c.getTipo().equalsIgnoreCase(tipo)).collect(Collectors.toList());
        return null;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.componentes.stream()
                .filter(c -> c.getId().equals(idComponente))
                .findFirst() // devolvelo si lo encontras
                .orElse(null); // caso contrario devolveme un null (si no aclaro esto lanza excepcion);
    }
}
