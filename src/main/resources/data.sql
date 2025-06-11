INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- insert de entidades del proyecto para componentes default

-- Inserts para la tabla de lookup: Socket
-- Se crean primero ya que Motherboard y CoolerCPU dependen de ellos.
INSERT INTO Socket (id, nombre) VALUES (1, 'AM4');
INSERT INTO Socket (id, nombre) VALUES (2, 'AM5');
INSERT INTO Socket (id, nombre) VALUES (3, 'LGA1700');
INSERT INTO Socket (id, nombre) VALUES (4, 'LGA1200');

-- =================================================================
-- Procesadores AMD
-- =================================================================

-- Componente 1: Procesador AMD RYZEN 3 3200G (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (1, 'Procesador AMD RYZEN 3 3200G', 74900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            1, 'Ryzen 3 3200G', 1, '12 nm', true,
            'Radeon Vega 8', 'AMD Ryzen 3', 4, 4, '3600 MHz', '4000 MHz',
            true, '65 W', '0.3 MB', '2 MB', '4 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (1, 'Procesador-Ryzen3-3200G-1.jpg', 1),
       (2, 'Procesador-Ryzen3-3200G-2.jpg', 1),
       (3, 'Procesador-Ryzen3-3200G-3.jpg', 1),
       (4, 'Procesador-Ryzen3-3200G-4.jpg', 1);

-- Componente 2: Procesador AMD RYZEN 3 4100 (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (2, 'Procesador AMD RYZEN 3 4100', 73550.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            2, 'Ryzen 3 4100', 1, '7 nm', false,
            NULL, 'AMD Ryzen 3', 4, 8, '3800 MHz', '4000 MHz',
            true, '65 W', NULL, '2 MB', '4 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (5, 'Procesador-Ryzen3-4100-1.jpg', 2),
       (6, 'Procesador-Ryzen3-4100-2.jpg', 2);

-- Componente 3: Procesador AMD RYZEN 3 5300G (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (3, 'Procesador AMD RYZEN 3 5300G', 109900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            3, 'Ryzen 3 5300G', 1, '7 nm', true,
            'Radeon Vega 6', 'AMD Ryzen 3', 4, 8, '4000 MHz', '4200 MHz',
            true, '65 W', NULL, '2 MB', '8 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (7, 'Procesador-Ryzen3-5300G-1.jpg', 3);

-- Componente 4: Procesador AMD Ryzen 5 5500 (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (4, 'Procesador AMD RYZEN 5 5500', 103500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            4, 'Ryzen 5 5500', 1, '7 nm', false,
            NULL, 'AMD Ryzen 5', 6, 12, '3600 MHz', '4200 MHz',
            true, '65 W', NULL, '3 MB', '16 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (8, 'Procesador-Ryzen5-5500-1.jpg', 4),
       (9, 'Procesador-Ryzen5-5500-2.jpg', 4);

-- Componente 5: Procesador AMD Ryzen 5 5600GT (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (5, 'Procesador AMD RYZEN 5 5600GT', 172600.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            5, 'Ryzen 5 5600GT', 1, '7 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3600 MHz', '4600 MHz',
            true, '65 W', NULL, '3 MB', '16 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (10, 'Procesador-Ryzen5-5600GT-1.jpg', 5),
       (11, 'Procesador-Ryzen5-5600GT-2.jpg', 5),
       (11, 'Procesador-Ryzen5-5600GT-3.jpg', 5);

-- Componente 6: Procesador AMD Ryzen 5 5600XT (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (6, 'Procesador AMD RYZEN 5 5600XT', 203500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            6, 'Ryzen 5 5600XT', 1, '7 nm', false,
            NULL, 'AMD Ryzen 5', 6, 12, '3700 MHz', '4700 MHz',
            true, '65 W', NULL, NULL, '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (12, 'Procesador-Ryzen5-5600XT-1.jpg', 6);

-- Componente 7: Procesador AMD Ryzen 5 8600G (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (7, 'Procesador AMD RYZEN 5 8600G', 240900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             7, 'Ryzen 5 8600G', 2, '4 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '4300 MHz', '5000 MHz',
             true, '65 W', NULL, '6 MB', '16 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (13, 'Procesador-Ryzen5-8600G-1.jpg', 7),
       (14, 'Procesador-Ryzen5-8600G-2.jpg', 7);

-- Componente 8: Procesador AMD Ryzen 5 7600 (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (8, 'Procesador AMD RYZEN 5 7600', 275650.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            8, 'Ryzen 5 7600', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3800 MHz', '5100 MHz',
            true, '65 W', '0.384 MB', '6 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (15, 'Procesador-Ryzen5-7600-1.jpg', 8),
       (16, 'Procesador-Ryzen5-7600-2.jpg', 8),
       (17, 'Procesador-Ryzen5-7600-3.jpg', 8),
       (18, 'Procesador-Ryzen5-7600-4.jpg', 8);


-- Componente 9: Procesador AMD Ryzen 5 9600X (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (9, 'Procesador AMD RYZEN 5 9600X', 314900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            9, 'Ryzen 5 9600X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3900 MHz', '5400 MHz',
            false, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (19, 'Procesador-Ryzen5-9600X-1.jpg', 9);

-- Componente 10: Procesador AMD Ryzen 5 9600 (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (10, 'Procesador AMD RYZEN 5 9600', 321500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            10, 'Ryzen 5 9600', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3800 MHz', '5200 MHz',
            true, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (20, 'Procesador-Ryzen5-9600-1.jpg', 10);

-- Componente 11: Procesador AMD Ryzen 7 5700 (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (11, 'Procesador AMD RYZEN 7 5700', 174900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            11, 'Ryzen 7 5700', 1, '7 nm', false,
            NULL, 'AMD Ryzen 7', 8, 16, '3700 MHz', '4600 MHz',
            true, '65 W', NULL, '4 MB', '16 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (21, 'Procesador-Ryzen7-5700-1.jpg', 11),
       (22, 'Procesador-Ryzen7-5700-2.jpg', 11);

-- Componente 12: Procesador AMD Ryzen 7 5800XT (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (12, 'Procesador AMD RYZEN 7 5800XT', 266900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            12, 'Ryzen 7 5800XT', 1, '12 nm', false,
            NULL, 'AMD Ryzen 7', 8, 16, '3800 MHz', '4800 MHz',
            true, '105 W', NULL, '4 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (23, 'Procesador-Ryzen7-5800XT-1.jpg', 12),
       (24, 'Procesador-Ryzen7-5800XT-2.jpg', 12);

-- Componente 13: Procesador AMD Ryzen 7 8700G (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (13, 'Procesador AMD RYZEN 7 8700G', 347500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            13, 'Ryzen 7 8700G', 2, '4 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4200 MHz', '5000 MHz',
            true, '65 W', NULL, '8 MB', '16 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (25, 'Procesador-Ryzen7-8700G-1.jpg', 13),
       (26, 'Procesador-Ryzen7-8700G-2.jpg', 13);

-- Componente 14: Procesador AMD Ryzen 7 7700 (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (14, 'Procesador AMD RYZEN 7 7700', 407900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            14, 'Ryzen 7 7700', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '3800 MHz', '5300 MHz',
            true, '65 W', '0.512 MB', '8 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (27, 'Procesador-Ryzen7-7700-1.jpg', 14),
       (28, 'Procesador-Ryzen7-7700-2.jpg', 14),
       (29, 'Procesador-Ryzen7-7700-3.jpg', 14),
       (30, 'Procesador-Ryzen7-7700-4.jpg', 14);

-- Componente 15: Procesador AMD Ryzen 7 9700X (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (15, 'Procesador AMD RYZEN 7 9700X', 446900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            15, 'Ryzen 7 9700X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '3800 MHz', '5500 MHz',
            false, '65 W', NULL, '8 MB', '32 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (31, 'Procesador-Ryzen7-9700X-1.jpg', 15);

-- Componente 16: Procesador AMD Ryzen 7 7800X3D (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (16, 'Procesador AMD RYZEN 7 7800X3D', 600000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            16, 'Ryzen 7 7800X3D', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4200 MHz', '5000 MHz',
            false, '120 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (32, 'Procesador-Ryzen7-7800X3D-1.jpg', 16);

-- Componente 17: Procesador AMD Ryzen 7 9800X3D (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (17, 'Procesador AMD RYZEN 7 9800X3D', 664900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            17, 'Ryzen 7 9800X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4700 MHz', '5200 MHz',
            false, '200 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (33, 'Procesador-Ryzen7-9800X3D-1.jpg', 17);

-- Componente 18: Procesador AMD Ryzen 9 5900XT (Socket AM4 → 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (18, 'Procesador AMD RYZEN 9 5900XT', 451850.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            18, 'Ryzen 9 5900XT', 1, '12 nm', false,
            NULL, 'AMD Ryzen 9', 16, 32, '3300 MHz', '4800 MHz',
            false, '105 W', NULL, '8 MB', '64 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (34, 'Procesador-Ryzen9-5900XT-1.jpg', 18);

-- Componente 19: Procesador AMD Ryzen 9 7900 (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (19, 'Procesador AMD RYZEN 9 7900', 485000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            19, 'Ryzen 9 7900', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '3700 MHz', '5400 MHz',
            true, '65 W', '0.768 MB', '12 MB', '64 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (35, 'Procesador-Ryzen9-7900-1.jpg', 19),
       (36, 'Procesador-Ryzen9-7900-2.jpg', 19),
       (37, 'Procesador-Ryzen9-7900-3.jpg', 19);

-- Componente 20: Procesador AMD Ryzen 9 9900X (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (20, 'Procesador AMD RYZEN 9 9900X', 570600.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            20, 'Ryzen 9 9900X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5600 MHz',
            false, '120 W', NULL, '12 MB', '64 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (38, 'Procesador-Ryzen9-9900X-1.jpg', 20);

-- Componente 21: Procesador AMD Ryzen 9 7950X (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (21, 'Procesador AMD RYZEN 9 7950X', 756000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            21, 'Ryzen 9 7950X', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4500 MHz', '5700 MHz',
            false, '170 W', '1 MB', '16 MB', '64 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (39, 'Procesador-Ryzen9-7950X-1.jpg', 21),
       (40, 'Procesador-Ryzen9-7950X-2.jpg', 21),
       (41, 'Procesador-Ryzen9-7950X-3.jpg', 21);

-- Componente 22: Procesador AMD Ryzen 9 9950X (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (22, 'Procesador AMD RYZEN 9 9950X', 825000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            22, 'Ryzen 9 9950X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4300 MHz', '5700 MHz',
            false, '170 W', NULL, '16 MB', '64 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (42, 'Procesador-Ryzen9-9950X-1.jpg', 22);

-- Componente 23: Procesador AMD Ryzen 9 9900X3D (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (23, 'Procesador AMD RYZEN 9 9900X3D', 869000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            23, 'Ryzen 9 9900X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5500 MHz',
            false, '200 W', NULL, '12 MB', '128 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (43, 'Procesador-Ryzen9-9900X3D-1.jpg', 23);

-- Componente 24: Procesador AMD Ryzen 9 9950X3D (Socket AM5 → 2)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (24, 'Procesador AMD RYZEN 9 9950X3D', 999000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            24, 'Ryzen 9 9950X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4300 MHz', '5700 MHz',
            false, '200 W', NULL, '16 MB', '128 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (44, 'Procesador-Ryzen9-9950X3D-1.jpg', 24);

-- =================================================================
-- Procesadores INTEL
-- =================================================================

-- Componente 25: Procesador Intel Core i3 14100F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (25, 'Procesador Intel Core i3 14100F', 115000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            25, 'Intel Core i3 14100F', 3, '10 nm', false,
            NULL, 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
            true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (45, 'Procesador-IntelCorei3-14100F-1.jpg', 25);

-- Componente 26: Procesador Intel Core i3 12100 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (26, 'Procesador Intel Core i3 12100', 153000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            26, 'Intel Core i3 12100', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i3', 4, 8, '3300 MHz', '4300 MHz',
            true, '65 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (46, 'Procesador-IntelCorei3-12100-1.jpg', 26),
       (47, 'Procesador-IntelCorei3-12100-2.jpg', 26),
       (48, 'Procesador-IntelCorei3-12100-3.jpg', 26);

-- Componente 27: Procesador Intel Core i3 14100 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (27, 'Procesador Intel Core i3 14100', 160000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            27, 'Intel Core i3 14100', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
            true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (49, 'Procesador-IntelCorei3-14100-1.jpg', 27);

-- Componente 28: Procesador Intel Core i5 12400F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (28, 'Procesador Intel Core i5 12400F', 160000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            28, 'Intel Core i5 12400F', 3, '10 nm', false,
            NULL, 'Intel Core i5', 6, 12, '2500 MHz', '4400 MHz',
            true, '65 W', '0.48 MB', '7.5 MB', '18 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (50, 'Procesador-IntelCorei5-12400F-1.jpg', 28),
       (51, 'Procesador-IntelCorei5-12400F-2.jpg', 28),
       (52, 'Procesador-IntelCorei5-12400F-3.jpg', 28);

-- Componente 29: Procesador Intel Core i5 12400 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (29, 'Procesador Intel Core i5 12400', 198000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            29, 'Intel Core i5 12400', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i5', 6, 12, '2500 MHz', '4400 MHz',
            true, '65 W', '0.48 MB', '7.5 MB', '18 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (53, 'Procesador-IntelCorei5-12400-1.jpg', 29),
       (54, 'Procesador-IntelCorei5-12400-2.jpg', 29),
       (55, 'Procesador-IntelCorei5-12400-3.jpg', 29);

-- Componente 30: Procesador Intel Core i5 14400F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (30, 'Procesador Intel Core i5 14400F', 205000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            30, 'Intel Core i5 14400F', 3, '10 nm', false,
            NULL, 'Intel Core i5', 10, 16, '2500 MHz', '4700 MHz',
            true, '65 W', NULL, '9.5 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (56, 'Procesador-IntelCorei5-14400F-1.jpg', 30);

-- Componente 31: Procesador Intel Core i5 12600K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (31, 'Procesador Intel Core i5 12600K', 266000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            31, 'Intel Core i5 12600K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i5', 10, 16, '2800 MHz', '4900 MHz',
            false, '125 W', NULL, '9.5 MB',NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (57, 'Procesador-IntelCorei5-12600K-1.jpg', 31),
       (58, 'Procesador-IntelCorei5-12600K-2.jpg', 31),
       (59, 'Procesador-IntelCorei5-12600k-3.jpg', 31);

-- Componente 32: Procesador Intel Core i5 14600K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (32, 'Procesador Intel Core i5 14600K', 350000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            32, 'Intel Core i5 14600K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i5', 14, 20, '3500 MHz', '5300 MHz',
            false, '181 W', NULL, '20 MB', '24 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (60, 'Procesador-IntelCorei5-14600k-1.jpg', 32);

-- Componente 33: Procesador Intel Core i7 12700K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (33, 'Procesador Intel Core i7 12700K', 334000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            33, 'Intel Core i7 12700K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i7', 12, 20, '2700 MHz', '5000 MHz',
            false, '105 W', NULL, '12 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (61, 'Procesador-IntelCorei7-12700K-1.jpg', 33),
       (62, 'Procesador-IntelCorei7-12700K-2.jpg', 33),
       (63, 'Procesador-IntelCorei7-12700K-3.jpg', 33);

-- Componente 34: Procesador Intel Core i7 12700F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (34, 'Procesador Intel Core i7 12700F', 345000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             34, 'Intel Core i7 12700F', 3, '10 nm', false,
             NULL, 'Intel Core i7', 12, 20, '1600 MHz', '5000 MHz',
             true, '180 W', NULL, '12 MB', '25 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (64, 'Procesador-IntelCorei7-12700F-1.jpg', 34),
       (65, 'Procesador-IntelCorei7-12700F-2.jpg', 34),
       (66, 'Procesador-IntelCorei7-12700F-3.jpg', 34);

-- Componente 35: Procesador Intel Core i7 12700 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (35, 'Procesador Intel Core i7 12700', 383000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             35, 'Intel Core i7 12700', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 12, 20, '2100 MHz', '4900 MHz',
             true, '65 W', NULL, '12 MB', '13 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (67, 'Procesador-IntelCorei7-12700-1.jpg', 35),
       (68, 'Procesador-IntelCorei7-12700-2.jpg', 35),
       (69, 'Procesador-IntelCorei7-12700-3.jpg', 35);

-- Componente 36: Procesador Intel Core i7 14700F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (36, 'Procesador Intel Core i7 14700F', 430000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            36, 'Intel Core i7 14700F', 3, '10 nm', false,
            NULL, 'Intel Core i7', 20, 28, '1500 MHz', '5300 MHz',
            true, '65 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (70, 'Procesador-IntelCorei7-14700F-1.jpg', 36);

-- Componente 37: Procesador Intel Core i7 14700 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (37, 'Procesador Intel Core i7 14700', 452000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             37, 'Intel Core i7 14700', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2100 MHz', '5400 MHz',
             true, '200 W', NULL, '28 MB', '33 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (71, 'Procesador-IntelCorei7-14700-1.jpg', 37);

-- Componente 38: Procesador Intel Core i7 14700KF (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (38, 'Procesador Intel Core i7 14700KF', 469000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             38, 'Intel Core i7 14700KF', 3, '10 nm', false,
             NULL, 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (72, 'Procesador-IntelCorei7-14700KF-1.jpg', 38);

-- Componente 39: Procesador Intel Core i7 14700K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (39, 'Procesador Intel Core i7 14700K', 489000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             39, 'Intel Core i7 14700K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '26 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (73, 'Procesador-IntelCorei7-14700K-1.jpg', 39);

-- Componente 40: Procesador Intel Core i9 12900KF (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (40, 'Procesador Intel Core i9 12900KF', 492000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            40, 'Intel Core i9 12900KF', 3, '10 nm', false,
            NULL, 'Intel Core i9', 16, 24, '2400 MHz', '5200 MHz',
            false, '125 W', NULL, '14 MB', '30 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (74, 'Procesador-IntelCorei9-12900KF-1.jpg', 40),
       (75, 'Procesador-IntelCorei9-12900KF-2.jpg', 40),
       (76, 'Procesador-IntelCorei9-12900KF-3.jpg', 40);

-- Componente 41: Procesador Intel Core i9 12900K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (41, 'Procesador Intel Core i9 12900K', 560000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             41, 'Intel Core i9 12900K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 16, 24, '2400 MHz', '5200 MHz',
             false, '125 W', NULL, '14 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (77, 'Procesador-IntelCorei9-12900K-1.jpg', 41),
       (78, 'Procesador-IntelCorei9-12900K-2.jpg', 41),
       (79, 'Procesador-IntelCorei9-12900K-3.jpg', 41);

-- Componente 42: Procesador Intel Core i9 12900 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (42, 'Procesador Intel Core i9 12900', 568000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             42, 'Intel Core i9 12900', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 16, 24, '2400 MHz', '5100 MHz',
             true, '65 W', NULL, '14 MB', '30 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (80, 'Procesador-IntelCorei9-12900-1.jpg', 42),
       (81, 'Procesador-IntelCorei9-12900-2.jpg', 42),
       (82, 'Procesador-IntelCorei9-12900-3.jpg', 42);

-- Componente 43: Procesador Intel Core i9 14900KF (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (43, 'Procesador Intel Core i9 14900KF', 643000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             43, 'Intel Core i9 14900KF', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '14 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (83, 'Procesador-IntelCorei9-14900KF-1.jpg', 43);

-- Componente 44: Procesador Intel Core i9 14900K (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (44, 'Procesador Intel Core i9 14900K', 673000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             44, 'Intel Core i9 14900K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '32 MB', NULL
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (84, 'Procesador-IntelCorei9-14900K-1.jpg', 44);

-- Componente 45: Procesador Intel Core i9 14900F (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (45, 'Procesador Intel Core i9 14900F', 799000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             45, 'Intel Core i9 14900F', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '4300 MHz', '5800 MHz',
             false, '219 W', NULL, '32 MB', '36 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (85, 'Procesador-IntelCorei9-14900F-1.jpg', 45);

-- Componente 46: Procesador Intel Core i9 14900 (Socket LGA1700 → 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (46, 'Procesador Intel Core i9 14900', 838000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             46, 'Intel Core i9 14900', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 24, 32, '4300 MHz', '5800 MHz',
             false, '219 W', NULL, '32 MB', '36 MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (86, 'Procesador-IntelCorei9-14900-1.jpg', 46);

-- =================================================================
-- Motherboards
-- =================================================================

-- Componente 47: Gigabyte A520M K V2 AM4 DDR4
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (47, 'Motherboard Gigabyte A520M K V2 AM4 DDR4', 73790.0, 10, 'Gigabyte');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            47, 1, 'AMD A520', 'AMD', 'M-ATX',
            1, 4, 6,
            'DDR4', 2,
            '40 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (87, 'Motherboard-AMD-Gigabyte-A520M-K-V2-1.jpg', 47),
       (88, 'Motherboard-AMD-Gigabyte-A520M-K-V2-2.jpg', 47),
       (89, 'Motherboard-AMD-Gigabyte-A520M-K-V2-3.jpg', 47),
       (90, 'Motherboard-AMD-Gigabyte-A520M-K-V2-4.jpg', 47);

-- Componente 48: ASUS TUF GAMING A520M-PLUS WIFI AM4 DDR4
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (48, 'Motherboard ASUS TUF GAMING A520M-Plus WIFI AM4 DDR4', 128700.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            48, 1, 'AMD A520', 'AMD', 'M-ATX',
            1, 4, 6,
            'DDR4', 4,
            '35 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (91, 'Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-1.jpg', 48),
       (92, 'Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-2.jpg', 48),
       (93, 'Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-3.jpg', 48),
       (94, 'Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-4.jpg', 48);

-- Componente 49: ASUS PRIME B550M-A AC AM4 DDR4
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (49, 'Motherboard ASUS PRIME B550M-A AC AM4 DDR4', 129000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            49, 1, 'AMD B550', 'AMD', 'M-ATX',
            2, 4, 6,
            'DDR4', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (95, 'Motherboard-AMD-ASUS-PRIME-B550M-A-AC-1.jpg', 49),
       (96, 'Motherboard-AMD-ASUS-PRIME-B550M-A-AC-2.jpg', 49),
       (97, 'Motherboard-AMD-ASUS-PRIME-B550M-A-AC-3.jpg', 49),
       (98, 'Motherboard-AMD-ASUS-PRIME-B550M-A-AC-4.jpg', 49);

-- Componente 50: ASUS TUF GAMING B550M-PLUS WIFI II AM4 DDR4
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (50, 'Motherboard ASUS TUF GAMING B550M-PLUS WIFI II AM4 DDR4', 196000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            50, 1, 'AMD B550', 'AMD', 'M-ATX',
            2, 4, 7,
            'DDR4', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (99, 'Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-1.jpg', 50),
       (100, 'Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-2.jpg', 50),
       (101, 'Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-3.jpg', 50),
       (102, 'Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-4.jpg', 50);

-- Componente 51: ASUS ROG STRIX B550-F GAMING WIFI II AM4 DDR4
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (51, 'Motherboard ASUS ROG STRIX B550-F GAMING WIFI II AM4 DDR4', 261500.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            51, 1, 'AMD B550', 'AMD', 'ATX',
            2, 6, 7,
            'DDR4', 4,
            '35 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (103, 'Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-1.jpg', 51),
       (104, 'Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-2.jpg', 51),
       (105, 'Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-3.jpg', 51),
       (106, 'Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-4.jpg', 51);

-- Componente 52: ASUS PRIME A620M-A AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (52, 'Motherboard ASUS PRIME A620M-A AM5 DDR5', 173000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            52, 2, 'AMD A620', 'AMD', 'M-ATX',
            2, 4, 6,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (107, 'Motherboard-AMD-ASUS-PRIME-A620M-A-1.jpg', 52),
       (108, 'Motherboard-AMD-ASUS-PRIME-A620M-A-2.jpg', 52),
       (109, 'Motherboard-AMD-ASUS-PRIME-A620M-A-3.jpg', 52),
       (110, 'Motherboard-AMD-ASUS-PRIME-A620M-A-4.jpg', 52);

-- Componente 53: ASUS TUF GAMING A620M-PLUS WIFI AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (53, 'Motherboard ASUS TUF GAMING A620M-PLUS WIFI AM5 DDR5', 185000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            53, 2, 'AMD A620', 'AMD', 'M-ATX',
            2, 4, 6,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (111, 'Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-1.jpg', 53),
       (112, 'Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-2.jpg', 53),
       (113, 'Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-3.jpg', 53),
       (114, 'Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-4.jpg', 53);

-- Componente 54: Gigabyte B840M-A AORUS ELITE WIFI 6E AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (54, 'Motherboard Gigabyte B840M-A AORUS ELITE WIFI 6E AM5 DDR5', 250000.0, 10, 'Gigabyte');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            54, 2, 'AMD B840', 'AMD', 'M-ATX',
            2, 4, 7,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (115, 'Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-1.jpg', 54),
       (116, 'Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-2.jpg', 54),
       (117, 'Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-3.jpg', 54),
       (118, 'Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-4.jpg', 54);

-- Componente 55: ASUS PROART B650-CREATOR AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (55, 'Motherboard ASUS PROART B650-CREATOR AM5 DDR5', 324000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            55, 2, 'AMD B650', 'AMD', 'ATX',
            3, 4, 6,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (119, 'Motherboard-AMD-ASUS-PROART-B650-CREATOR-1.jpg', 55),
       (120, 'Motherboard-AMD-ASUS-PROART-B650-CREATOR-2.jpg', 55),
       (121, 'Motherboard-AMD-ASUS-PROART-B650-CREATOR-3.jpg', 55),
       (122, 'Motherboard-AMD-ASUS-PROART-B650-CREATOR-4.jpg', 55);

-- Componente 56: Asrock X870 STEEL LEGEND WIFI AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (56, 'Motherboard Asrock X870 STEEL LEGEND WIFI AM5 DDR5', 403000.0, 10, 'Asrock');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            56, 2, 'AMD X870', 'AMD', 'ATX',
            3, 4, 9,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (123, 'Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-1.jpg', 56),
       (124, 'Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-2.jpg', 56),
       (125, 'Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-3.jpg', 56),
       (126, 'Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-4.jpg', 56);

-- Componente 57: ASUS TUF GAMING X870-PLUS WIFI AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (57, 'Motherboard ASUS TUF GAMING X870-PLUS WIFI AM5 DDR5', 446000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            57, 2, 'AMD X870', 'AMD', 'ATX',
            4, 2, 8,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (127, 'Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-1.jpg', 57),
       (128, 'Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-2.jpg', 57),
       (129, 'Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-3.jpg', 57),
       (130, 'Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-4.jpg', 57);

-- Componente 58: ASUS ROG CROSSHAIR X870E HERO AM5 DDR5
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (58, 'Motherboard ASUS ROG CROSSHAIR X870E HERO AM5 DDR5', 999000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            58, 2, 'AMD X870E', 'AMD', 'ATX',
            5, 4, 6,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (131, 'Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-1.jpg', 58),
       (132, 'Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-2.jpg', 58),
       (133, 'Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-3.jpg', 58),
       (134, 'Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-4.jpg', 58);

-- SEGUIR CON LA CARGA DE LAS MOTHER INTEL

-- =================================================================
-- Coolers CPU
-- =================================================================

-- Componente 7: Cooler Master Hyper 212
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (7, 'Cooler Master Hyper 212', 30000.0, 15, 'Cooler Master');
INSERT INTO CoolerCPU (id, consumo, tdpPredeterminado, tipoDeDisipacion, cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido) VALUES (7, '1.92W', '180W', 'Aire', 1, '120mm', 'RGB', '26 dBA');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (7, 'cooler-master-hyper-212-1.jpg', 7);
-- Compatibilidad de Sockets para el Cooler 7
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (7, 1); -- LGA1700
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (7, 3); -- AM4
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (7, 4); -- LGA1200

-- Componente 8: Noctua NH-U12S
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (8, 'Noctua NH-U12S', 40000.0, 10, 'Noctua');
INSERT INTO CoolerCPU (id, consumo, tdpPredeterminado, tipoDeDisipacion, cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido) VALUES (8, '1.56W', '129W', 'Aire', 1, '120mm', 'Ninguna', '22.4 dBA');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (8, 'noctua-nh-u12s-1.jpg', 8);
-- Compatibilidad de Sockets para el Cooler 8
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (8, 1);
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (8, 2);
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (8, 3);
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (8, 4);

-- Componente 9: DeepCool Gammaxx
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (9, 'DeepCool Gammaxx', 25000.0, 20, 'DeepCool');
INSERT INTO CoolerCPU (id, consumo, tdpPredeterminado, tipoDeDisipacion, cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido) VALUES (9, '2.0W', '180W', 'Aire', 1, '120mm', 'LED Rojo', '27 dBA');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (9, 'deepcool-gammaxx-1.jpg', 9);
-- Compatibilidad de Sockets para el Cooler 9
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (9, 3); -- AM4
INSERT INTO cooler_socket (cooler_id, socket_id) VALUES (9, 4); -- LGA1200


-- =================================================================
-- Memorias RAM
-- =================================================================

-- Componente 10: Corsair Vengeance 8GB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (10, 'Corsair Vengeance 8GB', 20000.0, 25, 'Corsair');
INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto) VALUES (10, '8GB', '3200MHz', 'DDR4', 'CL16', '1.35V', true, false);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (10, 'corsair-vengeance-8gb-1.jpg', 10);

-- Componente 11: Kingston HyperX 16GB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (11, 'Kingston HyperX 16GB', 35000.0, 18, 'Kingston');
INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto) VALUES (11, '16GB (2x8GB)', '3600MHz', 'DDR4', 'CL17', '1.35V', true, false);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (11, 'kingston-hyperx-16gb-1.jpg', 11);

-- Componente 12: G.Skill Ripjaws 8GB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (12, 'G.Skill Ripjaws 8GB', 22000.0, 30, 'G.Skill');
INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto) VALUES (12, '8GB', '3000MHz', 'DDR4', 'CL15', '1.35V', true, true);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (12, 'gskill-ripjaws-8gb-1.jpg', 12);


-- =================================================================
-- Placas de Video (GPU)
-- =================================================================

-- Componente 13: NVIDIA RTX 3060
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (13, 'NVIDIA RTX 3060', 300000.0, 5, 'NVIDIA');
INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE8Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM, cantidadDePCIE6Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines) VALUES (13, 'GeForce RTX 3060', 2, 0, 0, 1, 3, '170W', 1, '1320 MHz', '1777 MHz', 'GDDR6', '12GB', 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (13, 'nvidia-rtx-3060-1.jpg', 13);

-- Componente 14: AMD Radeon RX 6600
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (14, 'AMD Radeon RX 6600', 280000.0, 7, 'AMD');
INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE8Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM, cantidadDePCIE6Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines) VALUES (14, 'Radeon RX 6600', 2, 0, 0, 1, 3, '132W', 1, '1626 MHz', '2491 MHz', 'GDDR6', '8GB', 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (14, 'amd-radeon-rx-6600-1.jpg', 14);

-- Componente 15: NVIDIA GTX 1660
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (15, 'NVIDIA GTX 1660', 200000.0, 9, 'NVIDIA');
INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE8Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM, cantidadDePCIE6Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines) VALUES (15, 'GeForce GTX 1660 Super', 2, 0, 1, 1, 1, '125W', 1, '1530 MHz', '1785 MHz', 'GDDR6', '6GB', 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (15, 'nvidia-gtx-1660-1.jpg', 15);


-- =================================================================
-- Almacenamiento
-- =================================================================

-- Componente 16: Samsung SSD 500GB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (16, 'Samsung SSD 500GB', 35000.0, 20, 'Samsung');
INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial) VALUES (16, '500GB', 'SATA III', '3W', 'Solido', '512MB', '560 MB/s', '530 MB/s');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (16, 'samsung-ssd-500gb-1.jpg', 16);

-- Componente 17: WD HDD 1TB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (17, 'WD HDD 1TB', 25000.0, 18, 'Western Digital');
INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial) VALUES (17, '1TB', 'SATA III', '6W', 'Mecanico', '64MB', '150 MB/s', '145 MB/s');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (17, 'wd-hdd-1tb-1.jpg', 17);

-- Componente 18: Crucial SSD 1TB
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (18, 'Crucial SSD 1TB', 60000.0, 10, 'Crucial');
INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial) VALUES (18, '1TB', 'M.2 NVMe', '5W', 'Solido', '1GB', '2400 MB/s', '1900 MB/s');
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (18, 'crucial-ssd-1tb-1.jpg', 18);


-- =================================================================
-- Fuentes de Alimentacion
-- =================================================================

-- Componente 19: EVGA 600W
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (19, 'EVGA 600W', 40000.0, 14, 'EVGA');
INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4PinesPlus, cantConexionesSata, cantConexionesMolex, cantConectoresCpu2PinesPlus, cantConectoresCpu4Pines, cantConectoresCpu6Pines, cantConexionesPcie16Pines) VALUES (19, 'ATX', '600W', '580W', '80 Plus White', 'Integrado', true, 1, 6, 3, 2, 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (19, 'evga-600w-1.jpg', 19);

-- Componente 20: Corsair 750W
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (20, 'Corsair 750W', 50000.0, 10, 'Corsair');
INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4PinesPlus, cantConexionesSata, cantConexionesMolex, cantConectoresCpu2PinesPlus, cantConectoresCpu4Pines, cantConectoresCpu6Pines, cantConexionesPcie16Pines) VALUES (20, 'ATX', '750W', '745W', '80 Plus Bronze', 'Semi-Modular', true, 2, 8, 4, 4, 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (20, 'corsair-750w-1.jpg', 20);

-- Componente 21: Gigabyte 650W
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (21, 'Gigabyte 650W', 45000.0, 11, 'Gigabyte');
INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4PinesPlus, cantConexionesSata, cantConexionesMolex, cantConectoresCpu2PinesPlus, cantConectoresCpu4Pines, cantConectoresCpu6Pines, cantConexionesPcie16Pines) VALUES (21, 'ATX', '650W', '640W', '80 Plus Bronze', 'Integrado', true, 1, 6, 3, 2, 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (21, 'gigabyte-650w-1.jpg', 21);


-- =================================================================
-- Gabinetes
-- =================================================================

-- Componente 22: NZXT H510
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (22, 'NZXT H510', 60000.0, 9, 'NZXT');
INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados) VALUES (22, 'Negro', true, 'Vidrio Templado', 'Mid-Tower', 2, true, 4, 2, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (22, 'nzxt-h510-1.jpg', 22);

-- Componente 23: Corsair Carbide
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (23, 'Corsair Carbide', 55000.0, 7, 'Corsair');
INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados) VALUES (23, 'Negro', true, 'Acrílico', 'Mid-Tower', 2, true, 5, 1, 2, 0, 0, 0, 0, 0, 1, 1, 1, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (23, 'corsair-carbide-1.jpg', 23);

-- Componente 24: Cooler Master Q300L
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (24, 'Cooler Master Q300L', 50000.0, 10, 'Cooler Master');
INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados) VALUES (24, 'Negro', true, 'Acrílico', 'Micro-ATX Tower', 2, true, 4, 1, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (24, 'cooler-master-q300l-1.jpg', 24);