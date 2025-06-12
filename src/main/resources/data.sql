INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- insert de entidades del proyecto para componentes default

-- Inserts para la tabla de lookup: Socket
-- Se crean primero ya que Motherboard y CoolerCPU dependen de ellos.
INSERT INTO Socket (id, nombre) VALUES (1, 'AM4');
INSERT INTO Socket (id, nombre) VALUES (2, 'AM5');
INSERT INTO Socket (id, nombre) VALUES (3, 'LGA1700');

-- =================================================================
-- Procesadores AMD
-- =================================================================

-- Componente 1: Procesador AMD RYZEN 3 3200G (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 3 3200G', 74900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            1, 'Ryzen 3 3200G', 1, '12 nm', true,
            'Radeon Vega 8', 'AMD Ryzen 3', 4, 4, '3600 MHz', '4000 MHz',
            true, '65 W', '0.3 MB', '2 MB', '4 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen3-3200G-1.jpg', 1),
       ('Procesador-Ryzen3-3200G-2.jpg', 1),
       ('Procesador-Ryzen3-3200G-3.jpg', 1),
       ('Procesador-Ryzen3-3200G-4.jpg', 1);

-- Componente 2: Procesador AMD RYZEN 3 4100 (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 3 4100', 73550.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            2, 'Ryzen 3 4100', 1, '7 nm', false,
            NULL, 'AMD Ryzen 3', 4, 8, '3800 MHz', '4000 MHz',
            true, '65 W', NULL, '2 MB', '4 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen3-4100-1.jpg', 2),
       ('Procesador-Ryzen3-4100-2.jpg', 2);

-- Componente 3: Procesador AMD RYZEN 3 5300G (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 3 5300G', 109900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            3, 'Ryzen 3 5300G', 1, '7 nm', true,
            'Radeon Vega 6', 'AMD Ryzen 3', 4, 8, '4000 MHz', '4200 MHz',
            true, '65 W', NULL, '2 MB', '8 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen3-5300G-1.jpg', 3);

-- Componente 4: Procesador AMD Ryzen 5 5500 (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 5500', 103500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            4, 'Ryzen 5 5500', 1, '7 nm', false,
            NULL, 'AMD Ryzen 5', 6, 12, '3600 MHz', '4200 MHz',
            true, '65 W', NULL, '3 MB', '16 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-5500-1.jpg', 4),
       ('Procesador-Ryzen5-5500-2.jpg', 4);

-- Componente 5: Procesador AMD Ryzen 5 5600GT (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 5600GT', 172600.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            5, 'Ryzen 5 5600GT', 1, '7 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3600 MHz', '4600 MHz',
            true, '65 W', NULL, '3 MB', '16 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-5600GT-1.jpg', 5),
       ('Procesador-Ryzen5-5600GT-2.jpg', 5),
       ('Procesador-Ryzen5-5600GT-3.jpg', 5);

-- Componente 6: Procesador AMD Ryzen 5 5600XT (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 5600XT', 203500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            6, 'Ryzen 5 5600XT', 1, '7 nm', false,
            NULL, 'AMD Ryzen 5', 6, 12, '3700 MHz', '4700 MHz',
            true, '65 W', NULL, NULL, '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-5600XT-1.jpg', 6);

-- Componente 7: Procesador AMD Ryzen 5 8600G (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 8600G', 240900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             7, 'Ryzen 5 8600G', 2, '4 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '4300 MHz', '5000 MHz',
             true, '65 W', NULL, '6 MB', '16 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-8600G-1.jpg', 7),
       ('Procesador-Ryzen5-8600G-2.jpg', 7);

-- Componente 8: Procesador AMD Ryzen 5 7600 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 7600', 275650.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            8, 'Ryzen 5 7600', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3800 MHz', '5100 MHz',
            true, '65 W', '0.384 MB', '6 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-7600-1.jpg', 8),
       ('Procesador-Ryzen5-7600-2.jpg', 8),
       ('Procesador-Ryzen5-7600-3.jpg', 8),
       ('Procesador-Ryzen5-7600-4.jpg', 8);


-- Componente 9: Procesador AMD Ryzen 5 9600X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 9600X', 314900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            9, 'Ryzen 5 9600X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3900 MHz', '5400 MHz',
            false, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-9600X-1.jpg', 9);

-- Componente 10: Procesador AMD Ryzen 5 9600 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 9600', 321500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            10, 'Ryzen 5 9600', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3800 MHz', '5200 MHz',
            true, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-9600-1.jpg', 10);

-- Componente 11: Procesador AMD Ryzen 7 5700 (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 5700', 174900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            11, 'Ryzen 7 5700', 1, '7 nm', false,
            NULL, 'AMD Ryzen 7', 8, 16, '3700 MHz', '4600 MHz',
            true, '65 W', NULL, '4 MB', '16 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-5700-1.jpg', 11),
       ('Procesador-Ryzen7-5700-2.jpg', 11);

-- Componente 12: Procesador AMD Ryzen 7 5800XT (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 5800XT', 266900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            12, 'Ryzen 7 5800XT', 1, '12 nm', false,
            NULL, 'AMD Ryzen 7', 8, 16, '3800 MHz', '4800 MHz',
            true, '105 W', NULL, '4 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-5800XT-1.jpg', 12),
       ('Procesador-Ryzen7-5800XT-2.jpg', 12);

-- Componente 13: Procesador AMD Ryzen 7 8700G (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 8700G', 347500.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            13, 'Ryzen 7 8700G', 2, '4 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4200 MHz', '5000 MHz',
            true, '65 W', NULL, '8 MB', '16 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-8700G-1.jpg', 13),
       ('Procesador-Ryzen7-8700G-2.jpg', 13);

-- Componente 14: Procesador AMD Ryzen 7 7700 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 7700', 407900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            14, 'Ryzen 7 7700', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '3800 MHz', '5300 MHz',
            true, '65 W', '0.512 MB', '8 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-7700-1.jpg', 14),
       ('Procesador-Ryzen7-7700-2.jpg', 14),
       ('Procesador-Ryzen7-7700-3.jpg', 14),
       ('Procesador-Ryzen7-7700-4.jpg', 14);

-- Componente 15: Procesador AMD Ryzen 7 9700X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 9700X', 446900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            15, 'Ryzen 7 9700X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '3800 MHz', '5500 MHz',
            false, '65 W', NULL, '8 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-9700X-1.jpg', 15);

-- Componente 16: Procesador AMD Ryzen 7 7800X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 7800X3D', 600000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            16, 'Ryzen 7 7800X3D', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4200 MHz', '5000 MHz',
            false, '120 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-7800X3D-1.jpg', 16);

-- Componente 17: Procesador AMD Ryzen 7 9800X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 9800X3D', 664900.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            17, 'Ryzen 7 9800X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4700 MHz', '5200 MHz',
            false, '200 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-9800X3D-1.jpg', 17);

-- Componente 18: Procesador AMD Ryzen 9 5900XT (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 5900XT', 451850.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            18, 'Ryzen 9 5900XT', 1, '12 nm', false,
            NULL, 'AMD Ryzen 9', 16, 32, '3300 MHz', '4800 MHz',
            false, '105 W', NULL, '8 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-5900XT-1.jpg', 18);

-- Componente 19: Procesador AMD Ryzen 9 7900 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 7900', 485000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            19, 'Ryzen 9 7900', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '3700 MHz', '5400 MHz',
            true, '65 W', '0.768 MB', '12 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-7900-1.jpg', 19),
       ('Procesador-Ryzen9-7900-2.jpg', 19),
       ('Procesador-Ryzen9-7900-3.jpg', 19);

-- Componente 20: Procesador AMD Ryzen 9 9900X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9900X', 570600.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            20, 'Ryzen 9 9900X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5600 MHz',
            false, '120 W', NULL, '12 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9900X-1.jpg', 20);

-- Componente 21: Procesador AMD Ryzen 9 7950X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 7950X', 756000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            21, 'Ryzen 9 7950X', 2, '5 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4500 MHz', '5700 MHz',
            false, '170 W', '1 MB', '16 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-7950X-1.jpg', 21),
       ('Procesador-Ryzen9-7950X-2.jpg', 21),
       ('Procesador-Ryzen9-7950X-3.jpg', 21);

-- Componente 22: Procesador AMD Ryzen 9 9950X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9950X', 825000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            22, 'Ryzen 9 9950X', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4300 MHz', '5700 MHz',
            false, '170 W', NULL, '16 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9950X-1.jpg', 22);

-- Componente 23: Procesador AMD Ryzen 9 9900X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9900X3D', 869000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            23, 'Ryzen 9 9900X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5500 MHz',
            false, '200 W', NULL, '12 MB', '128 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9900X3D-1.jpg', 23);

-- Componente 24: Procesador AMD Ryzen 9 9950X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9950X3D', 999000.0, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            24, 'Ryzen 9 9950X3D', 2, '6 nm', true,
            'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4300 MHz', '5700 MHz',
            false, '200 W', NULL, '16 MB', '128 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9950X3D-1.jpg', 24);

-- =================================================================
-- Procesadores INTEL
-- =================================================================

-- Componente 25: Procesador Intel Core i3 14100F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i3 14100F', 115000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            25, 'Intel Core i3 14100F', 3, '10 nm', false,
            NULL, 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
            true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei3-14100F-1.jpg', 25);

-- Componente 26: Procesador Intel Core i3 12100 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i3 12100', 153000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            26, 'Intel Core i3 12100', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i3', 4, 8, '3300 MHz', '4300 MHz',
            true, '65 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei3-12100-1.jpg', 26),
       ('Procesador-IntelCorei3-12100-2.jpg', 26),
       ('Procesador-IntelCorei3-12100-3.jpg', 26);

-- Componente 27: Procesador Intel Core i3 14100 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i3 14100', 160000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            27, 'Intel Core i3 14100', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
            true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei3-14100-1.jpg', 27);

-- Componente 28: Procesador Intel Core i5 12400F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 12400F', 160000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            28, 'Intel Core i5 12400F', 3, '10 nm', false,
            NULL, 'Intel Core i5', 6, 12, '2500 MHz', '4400 MHz',
            true, '65 W', '0.48 MB', '7.5 MB', '18 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-12400F-1.jpg', 28),
       ('Procesador-IntelCorei5-12400F-2.jpg', 28),
       ('Procesador-IntelCorei5-12400F-3.jpg', 28);

-- Componente 29: Procesador Intel Core i5 12400 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 12400', 198000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            29, 'Intel Core i5 12400', 3, '10 nm', true,
            'UHD Graphics 730', 'Intel Core i5', 6, 12, '2500 MHz', '4400 MHz',
            true, '65 W', '0.48 MB', '7.5 MB', '18 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-12400-1.jpg', 29),
       ('Procesador-IntelCorei5-12400-2.jpg', 29),
       ('Procesador-IntelCorei5-12400-3.jpg', 29);

-- Componente 30: Procesador Intel Core i5 14400F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 14400F', 205000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            30, 'Intel Core i5 14400F', 3, '10 nm', false,
            NULL, 'Intel Core i5', 10, 16, '2500 MHz', '4700 MHz',
            true, '65 W', NULL, '9.5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-14400F-1.jpg', 30);

-- Componente 31: Procesador Intel Core i5 12600K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 12600K', 266000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            31, 'Intel Core i5 12600K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i5', 10, 16, '2800 MHz', '4900 MHz',
            false, '125 W', NULL, '9.5 MB',NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-12600K-1.jpg', 31),
       ('Procesador-IntelCorei5-12600K-2.jpg', 31),
       ('Procesador-IntelCorei5-12600k-3.jpg', 31);

-- Componente 32: Procesador Intel Core i5 14600K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 14600K', 350000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            32, 'Intel Core i5 14600K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i5', 14, 20, '3500 MHz', '5300 MHz',
            false, '181 W', NULL, '20 MB', '24 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-14600k-1.jpg', 32);

-- Componente 33: Procesador Intel Core i7 12700K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 12700K', 334000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            33, 'Intel Core i7 12700K', 3, '10 nm', true,
            'UHD Graphics 770', 'Intel Core i7', 12, 20, '2700 MHz', '5000 MHz',
            false, '105 W', NULL, '12 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-12700K-1.jpg', 33),
       ('Procesador-IntelCorei7-12700K-2.jpg', 33),
       ('Procesador-IntelCorei7-12700K-3.jpg', 33);

-- Componente 34: Procesador Intel Core i7 12700F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 12700F', 345000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             34, 'Intel Core i7 12700F', 3, '10 nm', false,
             NULL, 'Intel Core i7', 12, 20, '1600 MHz', '5000 MHz',
             true, '180 W', NULL, '12 MB', '25 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-12700F-1.jpg', 34),
       ('Procesador-IntelCorei7-12700F-2.jpg', 34),
       ('Procesador-IntelCorei7-12700F-3.jpg', 34);

-- Componente 35: Procesador Intel Core i7 12700 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 12700', 383000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             35, 'Intel Core i7 12700', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 12, 20, '2100 MHz', '4900 MHz',
             true, '65 W', NULL, '12 MB', '13 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-12700-1.jpg', 35),
       ('Procesador-IntelCorei7-12700-2.jpg', 35),
       ('Procesador-IntelCorei7-12700-3.jpg', 35);

-- Componente 36: Procesador Intel Core i7 14700F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700F', 430000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            36, 'Intel Core i7 14700F', 3, '10 nm', false,
            NULL, 'Intel Core i7', 20, 28, '1500 MHz', '5300 MHz',
            true, '65 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700F-1.jpg', 36);

-- Componente 37: Procesador Intel Core i7 14700 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700', 452000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             37, 'Intel Core i7 14700', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2100 MHz', '5400 MHz',
             true, '200 W', NULL, '28 MB', '33 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700-1.jpg', 37);

-- Componente 38: Procesador Intel Core i7 14700KF (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700KF', 469000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             38, 'Intel Core i7 14700KF', 3, '10 nm', false,
             NULL, 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700KF-1.jpg', 38);

-- Componente 39: Procesador Intel Core i7 14700K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700K', 489000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             39, 'Intel Core i7 14700K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '26 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700K-1.jpg', 39);

-- Componente 40: Procesador Intel Core i9 12900KF (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 12900KF', 492000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
            40, 'Intel Core i9 12900KF', 3, '10 nm', false,
            NULL, 'Intel Core i9', 16, 24, '2400 MHz', '5200 MHz',
            false, '125 W', NULL, '14 MB', '30 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-12900KF-1.jpg', 40),
       ('Procesador-IntelCorei9-12900KF-2.jpg', 40),
       ('Procesador-IntelCorei9-12900KF-3.jpg', 40);

-- Componente 41: Procesador Intel Core i9 12900K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 12900K', 560000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             41, 'Intel Core i9 12900K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 16, 24, '2400 MHz', '5200 MHz',
             false, '125 W', NULL, '14 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-12900K-1.jpg', 41),
       ('Procesador-IntelCorei9-12900K-2.jpg', 41),
       ('Procesador-IntelCorei9-12900K-3.jpg', 41);

-- Componente 42: Procesador Intel Core i9 12900 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 12900', 568000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             42, 'Intel Core i9 12900', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 16, 24, '2400 MHz', '5100 MHz',
             true, '65 W', NULL, '14 MB', '30 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-12900-1.jpg', 42),
       ('Procesador-IntelCorei9-12900-2.jpg', 42),
       ('Procesador-IntelCorei9-12900-3.jpg', 42);

-- Componente 43: Procesador Intel Core i9 14900KF (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900KF', 643000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             43, 'Intel Core i9 14900KF', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '14 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900KF-1.jpg', 43);

-- Componente 44: Procesador Intel Core i9 14900K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900K', 673000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             44, 'Intel Core i9 14900K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '32 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900K-1.jpg', 44);

-- Componente 45: Procesador Intel Core i9 14900F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900F', 799000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             45, 'Intel Core i9 14900F', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '4300 MHz', '5800 MHz',
             false, '219 W', NULL, '32 MB', '36 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900F-1.jpg', 45);

-- Componente 46: Procesador Intel Core i9 14900 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900', 838000.0, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             46, 'Intel Core i9 14900', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 24, 32, '4300 MHz', '5800 MHz',
             false, '219 W', NULL, '32 MB', '36 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900-1.jpg', 46);

-- =================================================================
-- Motherboards AMD
-- =================================================================

-- Componente 47: Gigabyte A520M K V2 AM4 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Gigabyte A520M K V2 AM4 DDR4', 73790.0, 10, 'Gigabyte');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-Gigabyte-A520M-K-V2-1.jpg', 47),
       ('Motherboard-AMD-Gigabyte-A520M-K-V2-2.jpg', 47),
       ('Motherboard-AMD-Gigabyte-A520M-K-V2-3.jpg', 47),
       ('Motherboard-AMD-Gigabyte-A520M-K-V2-4.jpg', 47);

-- Componente 48: ASUS TUF GAMING A520M-PLUS WIFI AM4 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS TUF GAMING A520M-Plus WIFI AM4 DDR4', 128700.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-1.jpg', 48),
       ('Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-2.jpg', 48),
       ('Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-3.jpg', 48),
       ('Motherboard-AMD-ASUS-TUF-A520M-Plus-WIFI-4.jpg', 48);

-- Componente 49: ASUS PRIME B550M-A AC AM4 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PRIME B550M-A AC AM4 DDR4', 129000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-PRIME-B550M-A-AC-1.jpg', 49),
       ('Motherboard-AMD-ASUS-PRIME-B550M-A-AC-2.jpg', 49),
       ('Motherboard-AMD-ASUS-PRIME-B550M-A-AC-3.jpg', 49),
       ('Motherboard-AMD-ASUS-PRIME-B550M-A-AC-4.jpg', 49);

-- Componente 50: ASUS TUF GAMING B550M-PLUS WIFI II AM4 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS TUF GAMING B550M-PLUS WIFI II AM4 DDR4', 196000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-1.jpg', 50),
       ('Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-2.jpg', 50),
       ('Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-3.jpg', 50),
       ('Motherboard-AMD-ASUS-TUF-B550M-Plus-WIFI-II-4.jpg', 50);

-- Componente 51: ASUS ROG STRIX B550-F GAMING WIFI II AM4 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS ROG STRIX B550-F GAMING WIFI II AM4 DDR4', 261500.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-1.jpg', 51),
       ('Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-2.jpg', 51),
       ('Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-3.jpg', 51),
       ('Motherboard-AMD-ASUS-ROG-STRIX-B550-F-GAMING-WIFI-II-4.jpg', 51);

-- Componente 52: ASUS PRIME A620M-A AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PRIME A620M-A AM5 DDR5', 173000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-PRIME-A620M-A-1.jpg', 52),
       ('Motherboard-AMD-ASUS-PRIME-A620M-A-2.jpg', 52),
       ('Motherboard-AMD-ASUS-PRIME-A620M-A-3.jpg', 52),
       ('Motherboard-AMD-ASUS-PRIME-A620M-A-4.jpg', 52);

-- Componente 53: ASUS TUF GAMING A620M-PLUS WIFI AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS TUF GAMING A620M-PLUS WIFI AM5 DDR5', 185000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-1.jpg', 53),
       ('Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-2.jpg', 53),
       ('Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-3.jpg', 53),
       ('Motherboard-AMD-ASUS-TUF-A620M-Plus-WIFI-4.jpg', 53);

-- Componente 54: Gigabyte B840M-A AORUS ELITE WIFI 6E AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Gigabyte B840M-A AORUS ELITE WIFI 6E AM5 DDR5', 250000.0, 10, 'Gigabyte');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-1.jpg', 54),
       ('Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-2.jpg', 54),
       ('Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-3.jpg', 54),
       ('Motherboard-AMD-Gigabyte-AORUS-ELITE-B840M-A-WIFI-4.jpg', 54);

-- Componente 55: ASUS PROART B650-CREATOR AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PROART B650-CREATOR AM5 DDR5', 324000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-PROART-B650-CREATOR-1.jpg', 55),
       ('Motherboard-AMD-ASUS-PROART-B650-CREATOR-2.jpg', 55),
       ('Motherboard-AMD-ASUS-PROART-B650-CREATOR-3.jpg', 55),
       ('Motherboard-AMD-ASUS-PROART-B650-CREATOR-4.jpg', 55);

-- Componente 56: Asrock X870 STEEL LEGEND WIFI AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Asrock X870 STEEL LEGEND WIFI AM5 DDR5', 403000.0, 10, 'Asrock');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-1.jpg', 56),
       ('Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-2.jpg', 56),
       ('Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-3.jpg', 56),
       ('Motherboard-AMD-Asrock-STEEL-LEGEND-X870-WIFI-4.jpg', 56);

-- Componente 57: ASUS TUF GAMING X870-PLUS WIFI AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS TUF GAMING X870-PLUS WIFI AM5 DDR5', 446000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-1.jpg', 57),
       ('Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-2.jpg', 57),
       ('Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-3.jpg', 57),
       ('Motherboard-AMD-ASUS-TUF-X870-Plus-WIFI-4.jpg', 57);

-- Componente 58: ASUS ROG CROSSHAIR X870E HERO AM5 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS ROG CROSSHAIR X870E HERO AM5 DDR5', 999000.0, 10, 'ASUS');

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

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-1.jpg', 58),
       ('Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-2.jpg', 58),
       ('Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-3.jpg', 58),
       ('Motherboard-AMD-ASUS-ROG-CROSSHAIR-HERO-X870E-4.jpg', 58);

-- Componente 59: ASUS PRIME H610M-K 1700 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PRIME H610M-K 1700 DDR4', 100900.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            59, 3, 'Intel H610', 'Intel', 'M-ATX',
            1, 4, 6,
            'DDR4', 2,
            '35 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-ASUS-PRIME-H610M-K-1.jpg', 59),
       ('Motherboard-Intel-ASUS-PRIME-H610M-K-2.jpg', 59),
       ('Motherboard-Intel-ASUS-PRIME-H610M-K-3.jpg', 59),
       ('Motherboard-Intel-ASUS-PRIME-H610M-K-4.jpg', 59);

-- Componente 60: Gigabyte B760M K V2 1700 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Gigabyte B760M K V2 1700 DDR4', 124000.0, 10, 'Gigabyte');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            60, 3, 'Intel B760', 'Intel', 'M-ATX',
            1, 4, 6,
            'DDR4', 2,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-Gigabyte-B760M-K-V2-1.jpg', 60),
       ('Motherboard-Intel-Gigabyte-B760M-K-V2-2.jpg', 60),
       ('Motherboard-Intel-Gigabyte-B760M-K-V2-3.jpg', 60),
       ('Motherboard-Intel-Gigabyte-B760M-K-V2-4.jpg', 60);

-- Componente 61: Gigabyte B760M D3HP 1700 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Gigabyte B760M D3HP 1700 DDR4', 137000.0, 10, 'Gigabyte');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            61, 3, 'Intel B760', 'Intel', 'M-ATX',
            2, 4, 5,
            'DDR4', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-Gigabyte-B760M-D3HP-1.jpg', 61),
       ('Motherboard-Intel-Gigabyte-B760M-D3HP-2.jpg', 61),
       ('Motherboard-Intel-Gigabyte-B760M-D3HP-3.jpg', 61),
       ('Motherboard-Intel-Gigabyte-B760M-D3HP-4.jpg', 61);

-- Componente 62: Asrock Z790M PG LIGHTNING 1700 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Asrock Z790M PG LIGHTNING 1700 DDR4', 228000.0, 10, 'Asrock');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            62, 3, 'Intel Z790', 'Intel', 'M-ATX',
            2, 4, 7,
            'DDR4', 4,
            '47 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-Asrock-Z790M-PG-LIGHTNING-1.jpg', 62),
       ('Motherboard-Intel-Asrock-Z790M-PG-LIGHTNING-2.jpg', 62),
       ('Motherboard-Intel-Asrock-Z790M-PG-LIGHTNING-3.jpg', 62);

-- Componente 63: ASUS ROG STRIX Z690-A GAMING WIFI D4 1700 DDR4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS ROG STRIX Z690-A GAMING WIFI D4 1700 DDR4', 285000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            63, 3, 'Intel Z690', 'Intel', 'ATX',
            4, 6, 8,
            'DDR4', 4,
            '35 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-ASUS-ROG-STRIX-Z690-A-GAMING-WIFI-D4-1.jpg', 63),
       ('Motherboard-Intel-ASUS-ROG-STRIX-Z690-A-GAMING-WIFI-D4-2.jpg', 63),
       ('Motherboard-Intel-ASUS-ROG-STRIX-Z690-A-GAMING-WIFI-D4-3.jpg', 63),
       ('Motherboard-Intel-ASUS-ROG-STRIX-Z690-A-GAMING-WIFI-D4-4.jpg', 63);

-- Componente 64: ASUS PRIME B760M-A CSM 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PRIME B760M-A CSM 1700 DDR4', 167000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            64, 3, 'Intel B760', 'Intel', 'M-ATX',
            2, 4, 6,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-ASUS-PRIME-B760M-A-CSM-1.jpg', 64),
       ('Motherboard-Intel-ASUS-PRIME-B760M-A-CSM-2.jpg', 64),
       ('Motherboard-Intel-ASUS-PRIME-B760M-A-CSM-3.jpg', 64),
       ('Motherboard-Intel-ASUS-PRIME-B760M-A-CSM-4.jpg', 64);

-- Componente 65: Gigabyte B760M AORUS ELITE AX WIFI 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Gigabyte B760M AORUS ELITE AX WIFI 1700 DDR4', 254000.0, 10, 'Gigabyte');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            65, 3, 'Intel B760', 'Intel', 'M-ATX',
            2, 4, 8,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-Gigabyte-B760M-AORUS-ELITE-AX-WIFI-1.jpg', 65),
       ('Motherboard-Intel-Gigabyte-B760M-AORUS-ELITE-AX-WIFI-2.jpg', 65),
       ('Motherboard-Intel-Gigabyte-B760M-AORUS-ELITE-AX-WIFI-3.jpg', 65),
       ('Motherboard-Intel-Gigabyte-B760M-AORUS-ELITE-AX-WIFI-4.jpg', 65);

-- Componente 66: ASUS TUF GAMING B760M-PLUS WIFI II 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS TUF GAMING B760M-PLUS WIFI II 1700 DDR4', 299000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            66, 3, 'Intel B760', 'Intel', 'M-ATX',
            3, 4, 7,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-ASUS-TUF-GAMING-B760M-PLUS-WIFI-II-1.jpg', 66),
       ('Motherboard-Intel-ASUS-TUF-GAMING-B760M-PLUS-WIFI-II-2.jpg', 66),
       ('Motherboard-Intel-ASUS-TUF-GAMING-B760M-PLUS-WIFI-II-3.jpg', 66),
       ('Motherboard-Intel-ASUS-TUF-GAMING-B760M-PLUS-WIFI-II-4.jpg', 66);

-- Componente 67: ASUS PRIME Z790-P 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard ASUS PRIME Z790-P 1700 DDR4', 314000.0, 10, 'ASUS');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            67, 3, 'Intel Z790', 'Intel', 'ATX',
            3, 4, 7,
            'DDR5', 4,
            '45 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-ASUS-PRIME-Z790-P-1.jpg', 67),
       ('Motherboard-Intel-ASUS-PRIME-Z790-P-2.jpg', 67),
       ('Motherboard-Intel-ASUS-PRIME-Z790-P-3.jpg', 67),
       ('Motherboard-Intel-ASUS-PRIME-Z790-P-4.jpg', 67);

-- Componente 68: MSI MAG Z790 TOMAHAWK WIFI MAX 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard MSI MAG Z790 TOMAHAWK WIFI MAX 1700 DDR4', 457000.0, 10, 'MSI');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            68, 3, 'Intel Z790', 'Intel', 'ATX',
            4, 8, 8,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-MSI-MAG-Z790-TOMAHAWK-WIFI-MAX-1.jpg', 68),
       ('Motherboard-Intel-MSI-MAG-Z790-TOMAHAWK-WIFI-MAX-2.jpg', 68),
       ('Motherboard-Intel-MSI-MAG-Z790-TOMAHAWK-WIFI-MAX-3.jpg', 68),
       ('Motherboard-Intel-MSI-MAG-Z790-TOMAHAWK-WIFI-MAX-4.jpg', 68);

-- Componente 69: Asrock Z790 TAICHI CARRARA 1700 DDR5
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Motherboard Asrock Z790 TAICHI CARRARA 1700 DDR4', 690000.0, 10, 'Asrock');

INSERT INTO Motherboard (
    id, socket_id, chipsetPrincipal, plataforma, factor,
    cantSlotsM2, cantPuertosSata, cantPuertosUSB,
    tipoMemoria, cantSlotsRAM,
    consumo, cantConector24Pines, cantConector4Pines
) VALUES (
            69, 3, 'Intel Z790', 'Intel', 'E-ATX',
            5, 8, 10,
            'DDR5', 4,
            '50 W', 1, 1
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Motherboard-Intel-Asrock-Z790-TAICHI-CARRARA-1.jpg', 69),
       ('Motherboard-Intel-Asrock-Z790-TAICHI-CARRARA-2.jpg', 69),
       ('Motherboard-Intel-Asrock-Z790-TAICHI-CARRARA-3.jpg', 69),
       ('Motherboard-Intel-Asrock-Z790-TAICHI-CARRARA-4.jpg', 69);

-- =================================================================
-- Coolers CPU
-- =================================================================

-- Componente 70: DeepCool AK620 DIGITAL PRO WHITE Display
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool AK620 DIGITAL PRO WHITE Display', 133000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
            70, '10 W', '260 W', 'Aire',
            2, '120 mm', 'ARGB', '25 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (70, 1), -- AM4
       (70, 2), -- AM5
       (70, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-AK620-DIGITAL-PRO-WHITE-Display-1.jpg', 70);

-- Componente 71: DeepCool AK620 ZERO DARK
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool AK620 ZERO DARK', 134000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
            71, '5 W', '260 W', 'Aire',
            2, '120 mm', NULL, '28 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (71, 1), -- AM4
       (71, 2), -- AM5
       (71, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-AK620-ZERO-DARK-1.jpg', 71),
       ('CoolerCPU-DeepCool-AK620-ZERO-DARK-2.jpg', 71),
       ('CoolerCPU-DeepCool-AK620-ZERO-DARK-3.jpg', 71);

-- Componente 72: DeepCool AK620 DIGITAL Display
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool AK620 DIGITAL Display', 146000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
            72, '5 W', '260 W', 'Aire',
            2, '120 mm', 'ARGB', '28 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (72, 1), -- AM4
       (72, 2), -- AM5
       (72, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-AK620-DIGITAL-Display-1.jpg', 72),
       ('CoolerCPU-DeepCool-AK620-DIGITAL-Display-2.jpg', 72),
       ('CoolerCPU-DeepCool-AK620-DIGITAL-Display-3.jpg', 72);

-- Componente 73: Be Quiet! DARK ROCK ELITE
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Be Quiet! DARK ROCK ELITE', 213000.0, 10, 'Be Quiet!');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             73, '10 W', '280 W', 'Aire',
             2, '120 mm', NULL, '25.8 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (73, 1), -- AM4
       (73, 2), -- AM5
       (73, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-Be-Quiet-DARK-ROCK-ELITE-1.jpg', 73),
       ('CoolerCPU-Be-Quiet-DARK-ROCK-ELITE-2.jpg', 73),
       ('CoolerCPU-Be-Quiet-DARK-ROCK-ELITE-3.jpg', 73),
       ('CoolerCPU-Be-Quiet-DARK-ROCK-ELITE-4.jpg', 73);

-- Componente 74: DeepCool LE240 V2 Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool LE240 V2 Water Cooler', 86300.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             74, '10 W', '280 W', 'Watercooling',
             2, '120 mm', 'ARGB', '31.6 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (74, 1), -- AM4
       (74, 2), -- AM5
       (74, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-LE240-V2-WC-1.jpg', 74);

-- Componente 75: Cooler Master ML240 ILLUSION White Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Cooler Master ML240 ILLUSION White Water Cooler', 141000.0, 10, 'Cooler Master');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             75, '10 W', '250 W', 'Watercooling',
             2, '120 mm', 'ARGB', '30 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (75, 1), -- AM4
       (75, 2), -- AM5
       (75, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-Cooler-Master-ML240-ILLUSION-White-WC-1.jpg', 75),
       ('CoolerCPU-Cooler-Master-ML240-ILLUSION-White-WC-2.jpg', 75),
       ('CoolerCPU-Cooler-Master-ML240-ILLUSION-White-WC-3.jpg', 75),
       ('CoolerCPU-Cooler-Master-ML240-ILLUSION-White-WC-4.jpg', 75);

-- Componente 76: Be Quiet! SILENT LOOP 3 240mm Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Be Quiet! SILENT LOOP 3 240mm Water Cooler', 158000.0, 10, 'Be Quiet!');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             76, '10 W', '280 W', 'Watercooling',
             2, '120 mm', NULL, '37 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (76, 1), -- AM4
       (76, 2), -- AM5
       (76, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-Be-Quiet-SILENT-LOOP-3-WC-1.jpg', 76);

-- Componente 77: DeepCool MYSTIQUE 240 LCD Screen Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool MYSTIQUE 240 LCD Screen Water Cooler', 189000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             77, '10 W', '280 W', 'Watercooling',
             2, '120 mm', 'ARGB', '38 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (77, 1), -- AM4
       (77, 2), -- AM5
       (77, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-MYSTIQUE-LCD-WC-1.jpg', 77);

-- Componente 78: Gigabyte AORUS Waterforce X II 240 Display
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Gigabyte AORUS Waterforce X II 240 Display', 315000.0, 10, 'Gigabyte');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             78, '10 W', '280 W', 'Watercooling',
             2, '120 mm', 'ARGB', '37 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (78, 1), -- AM4
       (78, 2), -- AM5
       (78, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-Gigabyte-AORUS-Waterforce-X-II-Display-1.jpg', 78);

-- Componente 79: DeepCool LE360 V2 White Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool LE360 V2 White Water Cooler', 115000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             79, '10 W', '300 W', 'Watercooling',
             3, '120 mm', 'ARGB', '31.6 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (79, 1), -- AM4
       (79, 2), -- AM5
       (79, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-LE360-V2-WC-1.jpg', 79);

-- Componente 80: DeepCool LT360 Water Cooler
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU DeepCool LT360 Water Cooler', 189000.0, 10, 'DeepCool');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             80, '10 W', '300 W', 'Watercooling',
             3, '120 mm', 'ARGB', '38.71 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (80, 1), -- AM4
       (80, 2), -- AM5
       (80, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-DeepCool-LT360-WC-1.jpg', 80);

-- Componente 81: Gigabyte AORUS Waterforce X II 360 Display
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Gigabyte AORUS Waterforce X II 360 Display', 373000.0, 10, 'Gigabyte');

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             81, '10 W', '320 W', 'Watercooling',
             3, '120 mm', 'ARGB', '37 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (81, 1), -- AM4
       (81, 2), -- AM5
       (81, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('CoolerCPU-Gigabyte-AORUS-Waterforce-X-II-360-Display-1.jpg', 81);

-- =================================================================
-- Memorias RAM DDR4
-- =================================================================

-- Componente 82: Corsair DDR4 8GB 3600Mhz Vengeance LPX Black CL18
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Corsair DDR4 8GB 3600Mhz Vengeance LPX Black CL18', 24600.0, 10, 'Corsair');

INSERT INTO MemoriaRAM (
    id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje,
    disipador, disipadorAlto
) VALUES (
            82, '8 GB', '3600 MHz', 'DDR4', 'CL18', '1.35V',
            true, true
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Corsair-DDR4-8GB-3600-1.jpg', 82);

-- Componente 83: Memoria Team DDR4 8GB 3200MHz T-Force Vulcan Z Red CL16
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR4 8GB 3200MHz T-Force Vulcan Z Red CL16', 22650.0, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (83, '8 GB', '3200 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-8GB-3200-1.jpg', 83);

-- Componente 84: Memoria Crucial DDR4 16GB 3200Mhz Basics
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Crucial DDR4 16GB 3200Mhz Basics', 35100.0, 10, 'Crucial');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (84, '16 GB', '3200 MHz', 'DDR4', 'CL22', '1.2V', false, false);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Crucial-DDR4-16GB-3200-1.jpg', 84);

-- Componente 85: Memoria OLOy DDR4 16GB Owl Black 3000MHz CL16
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM OLOy DDR4 16GB Owl Black 3000MHz CL16', 41450.0, 10, 'OLOy');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (85, '16 GB', '3000 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-OLOy-DDR4-16GB-3000-1.jpg', 85), ('MemoriaRAM-OLOy-DDR4-16GB-3000-2.jpg', 85);

-- Componente 86: Memoria Patriot Viper DDR4 16GB 3600MHz Steel
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Patriot Viper DDR4 16GB 3600MHz Steel', 46400.0, 10, 'Patriot');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (86, '16 GB', '3600 MHz', 'DDR4', 'CL18', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Patriot-Viper-DDR4-16GB-3600-1.jpg', 86), ('MemoriaRAM-Patriot-Viper-DDR4-16GB-3600-2.jpg', 86);

-- Componente 87: Memoria Team DDR4 16GB 3000MHz T-Force Vulcan Z Gray
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR4 16GB 3000MHz T-Force Vulcan Z Gray', 69100.0, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (87, '16 GB', '3000 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-16GB-3000-1.jpg', 87);

-- Componente 88: Memoria Crucial DDR4 32GB 3200MHz CL22
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Crucial DDR4 32GB 3200MHz CL22', 68600.0, 10, 'Crucial');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (88, '32 GB', '3200 MHz', 'DDR4', 'CL22', '1.2V', false, false);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Crucial-DDR4-32GB-3200-1.jpg', 88);

-- Componente 89: Memoria ADATA DDR4 32GB 3200MHz XPG Spectrix D35G RGB Black CL16
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM ADATA DDR4 32GB 3200MHz XPG Spectrix D35G RGB Black CL16', 79500.0, 10, 'ADATA');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (89, '32 GB', '3200 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-ADATA-DDR4-32GB-3200-1.jpg', 89);

-- Componente 90: Memoria Team DDR4 32GB 3600MHz T-Force Delta RGB Black CL18
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR4 32GB 3600MHz T-Force Delta RGB Black CL18', 137000.0, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (90, '32 GB', '3600 MHz', 'DDR4', 'CL18', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-32GB-3600-1.jpg', 90);

-- Componente 91: Memoria G.Skill DDR5 16GB 6000MHz AEGIS 5 CL36
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM G.Skill DDR5 16GB 6000MHz AEGIS 5 CL36', 66000.0, 10, 'G.Skill');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (91, '16 GB', '6000 MHz', 'DDR5', 'CL36', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-GSkill-DDR5-16GB-6000-1.jpg', 91);

-- Componente 92: Memoria Patriot DDR5 16GB 6000MHz Viper Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Patriot DDR5 16GB 6000MHz Viper Black CL30', 67500.0, 10, 'Patriot');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (92, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Patriot-Viper-DDR5-16GB-6000-1.jpg', 92);

-- Componente 93: Memoria Team DDR5 16GB 6000MHz T-Force Vulcan CL38 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR5 16GB 6000MHz T-Force Vulcan CL38 Black', 69500.0, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (93, '16 GB', '6000 MHz', 'DDR5', 'CL38', '1.25V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR5-16GB-6000-1.jpg', 93), ('MemoriaRAM-T-Force-DDR5-16GB-6000-2.jpg', 93);

-- Componente 94: Memoria Corsair DDR5 16GB 6000MHz Vengeance RGB CL36 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Corsair DDR5 16GB 6000MHz Vengeance RGB CL36 Black', 73000.0, 10, 'Corsair');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (94, '16 GB', '6000 MHz', 'DDR5', 'CL36', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Corsair-DDR5-16GB-6000-1.jpg', 94);

-- Componente 95: Memoria Kingston DDR5 16GB 6000MHz Fury Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Kingston DDR5 16GB 6000MHz Fury Black CL30', 76600.0, 10, 'Kingston');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (95, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Kingston-DDR5-16GB-6000-1.jpg', 95);

-- Componente 96: Memoria Team DDR5 16GB 6000MHz T-Force Delta TUF Alliance RGB Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR5 16GB 6000MHz T-Force Delta TUF Alliance RGB Black CL30', 80400.0, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (96, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-Delta-DDR5-16GB-6000-1.jpg', 96);

-- Componente 97: Memoria Corsair DDR5 32GB 6000MHz Vengeance RGB CL38 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Corsair DDR5 32GB 6000MHz Vengeance RGB CL38 Black', 130000.0, 10, 'Corsair');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (97, '32 GB', '6000 MHz', 'DDR5', 'CL38', '1.25V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Corsair-DDR5-32GB-6000-1.jpg', 97);

-- =================================================================
-- Placas de Video (GPU)
-- =================================================================

/*INSERT INTO Componente (nombre, precio, stock, marca) VALUES ('NVIDIA RTX 3060', 300000.0, 5, 'NVIDIA');
INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE8Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM, cantidadDePCIE6Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines) VALUES (13, 'GeForce RTX 3060', 2, 0, 0, 1, 3, '170W', 1, '1320 MHz', '1777 MHz', 'GDDR6', '12GB', 0, 0, 0);
INSERT INTO Imagen (urlImagen, componente_id) VALUES ('nvidia-rtx-3060-1.jpg', 13);

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
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (24, 'cooler-master-q300l-1.jpg', 24);*/