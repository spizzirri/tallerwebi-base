package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.entidades.Componente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RepositorioComponenteImpl implements RepositorioComponente {

    private List<Componente> componentes;

    public RepositorioComponenteImpl() {

        this.componentes = Arrays.asList(
                // Procesadores
                new Componente(1L, "Procesador", "Intel Core i5", 150000.0, "procesador1.jpg", 10),
                new Componente(2L, "Procesador", "AMD Ryzen 5", 140000.0, "procesador2.jpg", 8),
                new Componente(3L, "Procesador", "Intel Core i7", 200000.0, "procesador3.jpg", 5),

                // Motherboards
                new Componente(4L, "Motherboard", "ASUS Prime B550", 100000.0, "motherboard1.jpg", 12),
                new Componente(5L, "Motherboard", "Gigabyte B450", 95000.0, "motherboard2.jpg", 9),
                new Componente(6L, "Motherboard", "MSI Z490", 120000.0, "motherboard3.jpg", 6),

                // Coolers
                new Componente(7L, "Cooler", "Cooler Master Hyper 212", 30000.0, "cooler1.jpg", 15),
                new Componente(8L, "Cooler", "Noctua NH-U12S", 40000.0, "cooler2.jpg", 10),
                new Componente(9L, "Cooler", "DeepCool Gammaxx", 25000.0, "cooler3.jpg", 20),

                // Memorias RAM
                new Componente(10L, "Memoria", "Corsair Vengeance 8GB", 20000.0, "ram1.jpg", 25),
                new Componente(11L, "Memoria", "Kingston HyperX 16GB", 35000.0, "ram2.jpg", 18),
                new Componente(12L, "Memoria", "G.Skill Ripjaws 8GB", 22000.0, "ram3.jpg", 30),

                // GPU
                new Componente(13L, "Gpu", "NVIDIA RTX 3060", 300000.0, "gpu1.jpg", 5),
                new Componente(14L, "Gpu", "AMD Radeon RX 6600", 280000.0, "gpu2.jpg", 7),
                new Componente(15L, "Gpu", "NVIDIA GTX 1660", 200000.0, "gpu3.jpg", 9),

                // Almacenamiento
                new Componente(16L, "Almacenamiento", "Samsung SSD 500GB", 35000.0, "almacenamiento1.jpg", 20),
                new Componente(17L, "Almacenamiento", "WD HDD 1TB", 25000.0, "almacenamiento2.jpg", 18),
                new Componente(18L, "Almacenamiento", "Crucial SSD 1TB", 60000.0, "almacenamiento3.jpg", 10),

                // Fuente
                new Componente(19L, "Fuente", "EVGA 600W", 40000.0, "fuente1.jpg", 14),
                new Componente(20L, "Fuente", "Corsair 750W", 50000.0, "fuente2.jpg", 10),
                new Componente(21L, "Fuente", "Gigabyte 650W", 45000.0, "fuente3.jpg", 11),

                // Gabinete
                new Componente(22L, "Gabinete", "NZXT H510", 60000.0, "gabinete1.jpg", 9),
                new Componente(23L, "Gabinete", "Corsair Carbide", 55000.0, "gabinete2.jpg", 7),
                new Componente(24L, "Gabinete", "Cooler Master Q300L", 50000.0, "gabinete3.jpg", 10),

                // Monitor
                new Componente(25L, "Monitor", "Samsung 24", 85000.0, "monitor1.jpg", 6),
                new Componente(26L, "Monitor", "LG 27", 110000.0, "monitor2.jpg", 4),
                new Componente(27L, "Monitor", "AOC 22", 70000.0, "monitor3.jpg", 8),

                // Periféricos
                new Componente(28L, "Periferico", "Teclado Redragon", 15000.0, "periferico1.jpg", 20),
                new Componente(29L, "Periferico", "Mouse Logitech", 12000.0, "periferico2.jpg", 25),
                new Componente(30L, "Periferico", "Auriculares HyperX", 30000.0, "periferico3.jpg", 15)
        );


    }

    @Override
    public List<Componente> obtenerComponentesPorTipo(String tipo) {
        return this.componentes.stream()
                .filter(c -> c.getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());

    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.componentes.stream()
                .filter(c -> c.getId().equals(idComponente))
                .findFirst() // devolvelo si lo encontras
                .orElse(null); // caso contrario devolveme un null (si no aclaro esto lanza excepcion);
    }
}
