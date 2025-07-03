INSERT INTO Usuario(id, email, nombre, password, dni, telefono, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'admin', '$2a$10$ooHFgPSccJGztmq.U8WKJe80aMx1r5NGxhzLt9H2/JtwJr0lItvvS', '', '','ADMIN', true);

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
VALUES ('Procesador AMD RYZEN 3 3200G', 61.14, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 3 4100', 60.04, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 3 5300G', 89.71, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             3, 'Ryzen 3 5300G', 1, '7 nm', true,
             'Radeon Vega 6', 'AMD Ryzen 3', 4, 8, '4000 MHz', '4200 MHz',
             true, '65 W', NULL, '2 MB', '8 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen3-5300G-1.jpg', 3);

-- Componente 4: Procesador AMD Ryzen 5 5500 (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 5500', 84.50, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 5 5600GT', 140.90, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 5 5600XT', 166.12, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             6, 'Ryzen 5 5600XT', 1, '7 nm', false,
             NULL, 'AMD Ryzen 5', 6, 12, '3700 MHz', '4700 MHz',
             true, '65 W', NULL, NULL, '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-5600XT-1.jpg', 6);

-- Componente 7: Procesador AMD Ryzen 5 8600G (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 8600G', 196.65, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 5 7600', 225.02, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 5 9600X', 257.06, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             9, 'Ryzen 5 9600X', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3900 MHz', '5400 MHz',
             false, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-9600X-1.jpg', 9);

-- Componente 10: Procesador AMD Ryzen 5 9600 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 5 9600', 262.45, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             10, 'Ryzen 5 9600', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 5', 6, 12, '3800 MHz', '5200 MHz',
             true, '105 W', NULL, '6 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen5-9600-1.jpg', 10);

-- Componente 11: Procesador AMD Ryzen 7 5700 (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 5700', 142.78, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 7 5800XT', 217.88, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 7 8700G', 283.67, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 7 7700', 332.98, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 7 9700X', 364.82, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             15, 'Ryzen 7 9700X', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '3800 MHz', '5500 MHz',
             false, '65 W', NULL, '8 MB', '32 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-9700X-1.jpg', 15);

-- Componente 16: Procesador AMD Ryzen 7 7800X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 7800X3D', 489.80, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             16, 'Ryzen 7 7800X3D', 2, '5 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4200 MHz', '5000 MHz',
             false, '120 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-7800X3D-1.jpg', 16);

-- Componente 17: Procesador AMD Ryzen 7 9800X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 7 9800X3D', 542.78, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             17, 'Ryzen 7 9800X3D', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 7', 8, 16, '4700 MHz', '5200 MHz',
             false, '200 W', NULL, '8 MB', '96 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen7-9800X3D-1.jpg', 17);

-- Componente 18: Procesador AMD Ryzen 9 5900XT (Socket AM4 → 1)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 5900XT', 368.86, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             18, 'Ryzen 9 5900XT', 1, '12 nm', false,
             NULL, 'AMD Ryzen 9', 16, 32, '3300 MHz', '4800 MHz',
             false, '105 W', NULL, '8 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-5900XT-1.jpg', 18);

-- Componente 19: Procesador AMD Ryzen 9 7900 (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 7900', 395.92, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 9 9900X', 465.80, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             20, 'Ryzen 9 9900X', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5600 MHz',
             false, '120 W', NULL, '12 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9900X-1.jpg', 20);

-- Componente 21: Procesador AMD Ryzen 9 7950X (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 7950X', 617.14, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador AMD RYZEN 9 9950X', 673.06, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             22, 'Ryzen 9 9950X', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 9', 16, 32, '4300 MHz', '5700 MHz',
             false, '170 W', NULL, '16 MB', '64 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9950X-1.jpg', 22);

-- Componente 23: Procesador AMD Ryzen 9 9900X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9900X3D', 709.40, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             23, 'Ryzen 9 9900X3D', 2, '6 nm', true,
             'AMD Radeon Graphics', 'AMD Ryzen 9', 12, 24, '4400 MHz', '5500 MHz',
             false, '200 W', NULL, '12 MB', '128 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-Ryzen9-9900X3D-1.jpg', 23);

-- Componente 24: Procesador AMD Ryzen 9 9950X3D (Socket AM5 → 2)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador AMD RYZEN 9 9950X3D', 815.51, 10, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i3 14100F', 93.88, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             25, 'Intel Core i3 14100F', 3, '10 nm', false,
             NULL, 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
             true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei3-14100F-1.jpg', 25);

-- Componente 26: Procesador Intel Core i3 12100 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i3 12100', 124.89, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i3 14100', 130.61, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             27, 'Intel Core i3 14100', 3, '10 nm', true,
             'UHD Graphics 730', 'Intel Core i3', 4, 8, '3500 MHz', '4700 MHz',
             true, '60 W', NULL, '5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei3-14100-1.jpg', 27);

-- Componente 28: Procesador Intel Core i5 12400F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 12400F', 130.61, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i5 12400', 161.63, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i5 14400F', 167.35, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             30, 'Intel Core i5 14400F', 3, '10 nm', false,
             NULL, 'Intel Core i5', 10, 16, '2500 MHz', '4700 MHz',
             true, '65 W', NULL, '9.5 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-14400F-1.jpg', 30);

-- Componente 31: Procesador Intel Core i5 12600K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 12600K', 217.14, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             31, 'Intel Core i5 12600K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i5', 10, 16, '2800 MHz', '4900 MHz',
             false, '125 W', NULL, '9.5 MB',NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-12600K-1.jpg', 31),
       ('Procesador-IntelCorei5-12600K-2.jpg', 31),
       ('Procesador-IntelCorei5-12600K-3.jpg', 31);

-- Componente 32: Procesador Intel Core i5 14600K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i5 14600K', 285.71, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             32, 'Intel Core i5 14600K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i5', 14, 20, '3500 MHz', '5300 MHz',
             false, '181 W', NULL, '20 MB', '24 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei5-14600K-1.jpg', 32);

-- Componente 33: Procesador Intel Core i7 12700K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 12700K', 272.65, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i7 12700F', 281.63, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i7 12700', 312.65, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i7 14700F', 351.02, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             36, 'Intel Core i7 14700F', 3, '10 nm', false,
             NULL, 'Intel Core i7', 20, 28, '1500 MHz', '5300 MHz',
             true, '65 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700F-1.jpg', 36);

-- Componente 37: Procesador Intel Core i7 14700 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700', 369.00, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             37, 'Intel Core i7 14700', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2100 MHz', '5400 MHz',
             true, '200 W', NULL, '28 MB', '33 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700-1.jpg', 37);

-- Componente 38: Procesador Intel Core i7 14700KF (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700KF', 382.86, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             38, 'Intel Core i7 14700KF', 3, '10 nm', false,
             NULL, 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '28 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700KF-1.jpg', 38);

-- Componente 39: Procesador Intel Core i7 14700K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i7 14700K', 399.18, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             39, 'Intel Core i7 14700K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i7', 20, 28, '2500 MHz', '5600 MHz',
             false, '125 W', NULL, '26 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei7-14700K-1.jpg', 39);

-- Componente 40: Procesador Intel Core i9 12900KF (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 12900KF', 401.63, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i9 12900K', 457.14, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i9 12900', 463.67, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Procesador Intel Core i9 14900KF', 524.89, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             43, 'Intel Core i9 14900KF', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '14 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900KF-1.jpg', 43);

-- Componente 44: Procesador Intel Core i9 14900K (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900K', 549.40, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             44, 'Intel Core i9 14900K', 3, '10 nm', true,
             'UHD Graphics 770', 'Intel Core i9', 24, 32, '2400 MHz', '5600 MHz',
             false, '125 W', NULL, '32 MB', NULL
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900K-1.jpg', 44);

-- Componente 45: Procesador Intel Core i9 14900F (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900F', 652.24, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
) VALUES (
             45, 'Intel Core i9 14900F', 3, '10 nm', false,
             NULL, 'Intel Core i9', 24, 32, '4300 MHz', '5800 MHz',
             false, '219 W', NULL, '32 MB', '36 MB'
         );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Procesador-IntelCorei9-14900F-1.jpg', 45);

-- Componente 46: Procesador Intel Core i9 14900 (Socket LGA1700 → 3)
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Procesador Intel Core i9 14900', 684.08, 10, 'INTEL');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, consumo, l1Cache, l2Cache, l3Cache
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
VALUES ('Motherboard Gigabyte A520M K V2 AM4 DDR4', 60.24, 10, 'Gigabyte');

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
VALUES ('Motherboard ASUS TUF GAMING A520M-Plus WIFI AM4 DDR4', 105.06, 10, 'ASUS');

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
VALUES ('Motherboard ASUS PRIME B550M-A AC AM4 DDR4', 105.31, 10, 'ASUS');

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
VALUES ('Motherboard ASUS TUF GAMING B550M-PLUS WIFI II AM4 DDR4', 160.00, 10, 'ASUS');

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
VALUES ('Motherboard ASUS ROG STRIX B550-F GAMING WIFI II AM4 DDR4', 213.47, 10, 'ASUS');

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
VALUES ('Motherboard ASUS PRIME A620M-A AM5 DDR5', 141.22, 10, 'ASUS');

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
VALUES ('Motherboard ASUS TUF GAMING A620M-PLUS WIFI AM5 DDR5', 151.02, 10, 'ASUS');

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
VALUES ('Motherboard Gigabyte B840M-A AORUS ELITE WIFI 6E AM5 DDR5', 204.08, 10, 'Gigabyte');

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
VALUES ('Motherboard ASUS PROART B650-CREATOR AM5 DDR5', 264.49, 10, 'ASUS');

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
VALUES ('Motherboard Asrock X870 STEEL LEGEND WIFI AM5 DDR5', 329.00, 10, 'Asrock');

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
VALUES ('Motherboard ASUS TUF GAMING X870-PLUS WIFI AM5 DDR5', 364.08, 10, 'ASUS');

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
VALUES ('Motherboard ASUS ROG CROSSHAIR X870E HERO AM5 DDR5', 815.51, 10, 'ASUS');

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
VALUES ('Motherboard ASUS PRIME H610M-K 1700 DDR4', 82.37, 10, 'ASUS');

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
VALUES ('Motherboard Gigabyte B760M K V2 1700 DDR4', 101.22, 10, 'Gigabyte');

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
VALUES ('Motherboard Gigabyte B760M D3HP 1700 DDR4', 111.84, 10, 'Gigabyte');

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
VALUES ('Motherboard Asrock Z790M PG LIGHTNING 1700 DDR4', 186.12, 10, 'Asrock');

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
VALUES ('Motherboard ASUS ROG STRIX Z690-A GAMING WIFI D4 1700 DDR4', 232.65, 10, 'ASUS');

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
VALUES ('Motherboard ASUS PRIME B760M-A CSM 1700 DDR5', 136.12, 10, 'ASUS');

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
VALUES ('Motherboard Gigabyte B760M AORUS ELITE AX WIFI 1700 DDR5', 207.35, 10, 'Gigabyte');

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
VALUES ('Motherboard ASUS TUF GAMING B760M-PLUS WIFI II 1700 DDR5', 244.08, 10, 'ASUS');

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
VALUES ('Motherboard ASUS PRIME Z790-P 1700 DDR5', 256.33, 10, 'ASUS');

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
VALUES ('Motherboard MSI MAG Z790 TOMAHAWK WIFI MAX 1700 DDR5', 373.06, 10, 'MSI');

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
VALUES ('Motherboard Asrock Z790 TAICHI CARRARA 1700 DDR5', 563.27, 10, 'Asrock');

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
VALUES ('Cooler CPU DeepCool AK620 DIGITAL PRO WHITE Display', 108.57, 10, 'DeepCool');

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
VALUES ('Cooler CPU DeepCool AK620 ZERO DARK', 109.39, 10, 'DeepCool');

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
VALUES ('Cooler CPU DeepCool AK620 DIGITAL Display', 119.18, 10, 'DeepCool');

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
VALUES ('Cooler CPU Be Quiet! DARK ROCK ELITE', 173.88, 10, 'Be Quiet!');

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
VALUES ('Cooler CPU DeepCool LE240 V2 Water Cooler', 70.45, 10, 'DeepCool');

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
VALUES ('Cooler CPU Cooler Master ML240 ILLUSION White Water Cooler', 115.10, 10, 'Cooler Master');

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
VALUES ('Cooler CPU Be Quiet! SILENT LOOP 3 240mm Water Cooler', 128.98, 10, 'Be Quiet!');

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
VALUES ('Cooler CPU DeepCool MYSTIQUE 240 LCD Screen Water Cooler', 154.29, 10, 'DeepCool');

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
VALUES ('Cooler CPU Gigabyte AORUS Waterforce X II 240 Display', 257.14, 10, 'Gigabyte');

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
VALUES ('Cooler CPU DeepCool LE360 V2 White Water Cooler', 93.88, 10, 'DeepCool');

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
VALUES ('Cooler CPU DeepCool LT360 Water Cooler', 154.29, 10, 'DeepCool');

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
VALUES ('Cooler CPU Gigabyte AORUS Waterforce X II 360 Display', 304.49, 10, 'Gigabyte');

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
VALUES ('Memoria RAM Corsair DDR4 8GB 3600Mhz Vengeance LPX Black CL18', 20.08, 10, 'Corsair');

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
VALUES ('Memoria RAM Team DDR4 8GB 3200MHz T-Force Vulcan Z Red CL16', 18.49, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (83, '8 GB', '3200 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-8GB-3200-1.jpg', 83);

-- Componente 84: Memoria Crucial DDR4 16GB 3200Mhz Basics
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Crucial DDR4 16GB 3200Mhz Basics', 28.65, 10, 'Crucial');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (84, '16 GB', '3200 MHz', 'DDR4', 'CL22', '1.2V', false, false);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Crucial-DDR4-16GB-3200-1.jpg', 84);

-- Componente 85: Memoria OLOy DDR4 16GB Owl Black 3000MHz CL16
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM OLOy DDR4 16GB Owl Black 3000MHz CL16', 33.84, 10, 'OLOy');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (85, '16 GB', '3000 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-OLOy-DDR4-16GB-3000-1.jpg', 85), ('MemoriaRAM-OLOy-DDR4-16GB-3000-2.jpg', 85);

-- Componente 86: Memoria Patriot Viper DDR4 16GB 3600MHz Steel
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Patriot Viper DDR4 16GB 3600MHz Steel', 37.88, 10, 'Patriot');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (86, '16 GB', '3600 MHz', 'DDR4', 'CL18', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Patriot-Viper-DDR4-16GB-3600-1.jpg', 86), ('MemoriaRAM-Patriot-Viper-DDR4-16GB-3600-2.jpg', 86);

-- Componente 87: Memoria Team DDR4 16GB 3000MHz T-Force Vulcan Z Gray
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR4 16GB 3000MHz T-Force Vulcan Z Gray', 56.41, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (87, '16 GB', '3000 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-16GB-3000-1.jpg', 87);

-- Componente 88: Memoria Crucial DDR4 32GB 3200MHz CL22
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Crucial DDR4 32GB 3200MHz CL22', 56.00, 10, 'Crucial');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (88, '32 GB', '3200 MHz', 'DDR4', 'CL22', '1.2V', false, false);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Crucial-DDR4-32GB-3200-1.jpg', 88);

-- Componente 89: Memoria ADATA DDR4 32GB 3200MHz XPG Spectrix D35G RGB Black CL16
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM ADATA DDR4 32GB 3200MHz XPG Spectrix D35G RGB Black CL16', 64.90, 10, 'ADATA');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (89, '32 GB', '3200 MHz', 'DDR4', 'CL16', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-ADATA-DDR4-32GB-3200-1.jpg', 89);

-- Componente 90: Memoria Team DDR4 32GB 3600MHz T-Force Delta RGB Black CL18
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR4 32GB 3600MHz T-Force Delta RGB Black CL18', 111.84, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (90, '32 GB', '3600 MHz', 'DDR4', 'CL18', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR4-32GB-3600-1.jpg', 90);

-- Componente 91: Memoria G.Skill DDR5 16GB 6000MHz AEGIS 5 CL36
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM G.Skill DDR5 16GB 6000MHz AEGIS 5 CL36', 53.88, 10, 'G.Skill');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (91, '16 GB', '6000 MHz', 'DDR5', 'CL36', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-GSkill-DDR5-16GB-6000-1.jpg', 91);

-- Componente 92: Memoria Patriot DDR5 16GB 6000MHz Viper Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Patriot DDR5 16GB 6000MHz Viper Black CL30', 55.10, 10, 'Patriot');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (92, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Patriot-Viper-DDR5-16GB-6000-1.jpg', 92);

-- Componente 93: Memoria Team DDR5 16GB 6000MHz T-Force Vulcan CL38 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR5 16GB 6000MHz T-Force Vulcan CL38 Black', 55.67, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (93, '16 GB', '6000 MHz', 'DDR5', 'CL38', '1.25V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-DDR5-16GB-6000-1.jpg', 93), ('MemoriaRAM-T-Force-DDR5-16GB-6000-2.jpg', 93);

-- Componente 94: Memoria Corsair DDR5 16GB 6000MHz Vengeance RGB CL36 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Corsair DDR5 16GB 6000MHz Vengeance RGB CL36 Black', 59.59, 10, 'Corsair');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (94, '16 GB', '6000 MHz', 'DDR5', 'CL36', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Corsair-DDR5-16GB-6000-1.jpg', 94);

-- Componente 95: Memoria Kingston DDR5 16GB 6000MHz Fury Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Kingston DDR5 16GB 6000MHz Fury Black CL30', 62.53, 10, 'Kingston');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (95, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Kingston-DDR5-16GB-6000-1.jpg', 95);

-- Componente 96: Memoria Team DDR5 16GB 6000MHz T-Force Delta TUF Alliance RGB Black CL30
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Team DDR5 16GB 6000MHz T-Force Delta TUF Alliance RGB Black CL30', 65.63, 10, 'T-Force');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (96, '16 GB', '6000 MHz', 'DDR5', 'CL30', '1.35V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-T-Force-Delta-DDR5-16GB-6000-1.jpg', 96);

-- Componente 97: Memoria Corsair DDR5 32GB 6000MHz Vengeance RGB CL38 Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Memoria RAM Corsair DDR5 32GB 6000MHz Vengeance RGB CL38 Black', 106.12, 10, 'Corsair');

INSERT INTO MemoriaRAM (id, capacidad, velocidad, tecnologiaRAM, latencia, voltaje, disipador, disipadorAlto)
VALUES (97, '32 GB', '6000 MHz', 'DDR5', 'CL38', '1.25V', true, true);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('MemoriaRAM-Corsair-DDR5-32GB-6000-1.jpg', 97);

-- =================================================================
-- Placas de Video (GPU)
-- =================================================================

-- Componente 98: Placa de Video Zotac GeForce RTX 3060 12GB GDDR6 Twin Edge
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video Zotac GeForce RTX 3060 12GB GDDR6 Twin Edge', 346.12, 10, 'Zotac');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (98, 'GeForce RTX 3060', 2, 0, 0, 1, 3, '170 W', 0, 1, 0, 0, '1500 MHz', '1777 MHz', 'GDDR6', '12 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-Zotac-RTX-3060-12GB-1.jpg', 98),
       ('PlacaDeVideo-Zotac-RTX-3060-12GB-2.jpg', 98),
       ('PlacaDeVideo-Zotac-RTX-3060-12GB-3.jpg', 98),
       ('PlacaDeVideo-Zotac-RTX-3060-12GB-4.jpg', 98),
       ('PlacaDeVideo-Zotac-RTX-3060-12GB-5.jpg', 98);

-- Componente 99: Placa de Video MSI GeForce RTX 5060 TI 8GB GAMING TRIO OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video MSI GeForce RTX 5060 TI 8GB GAMING TRIO OC', 591.84, 10, 'MSI');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (99, 'GeForce RTX 5060 Ti', 3, 0, 0, 1, 3, '200 W', 0, 0, 1, 1, '2450 MHz', '2715 MHz', 'GDDR7', '8 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-MSI-RTX-5060-TI-8GB-1.jpg', 99),
       ('PlacaDeVideo-MSI-RTX-5060-TI-8GB-2.jpg', 99),
       ('PlacaDeVideo-MSI-RTX-5060-TI-8GB-3.jpg', 99),
       ('PlacaDeVideo-MSI-RTX-5060-TI-8GB-4.jpg', 99),
       ('PlacaDeVideo-MSI-RTX-5060-TI-8GB-5.jpg', 99);

-- Componente 100: Placa de video Zotac GeForce RTX 5060 Ti 16GB GDDR7 Twin Edge OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de video Zotac GeForce RTX 5060 Ti 16GB GDDR7 Twin Edge OC', 670.20, 10, 'Zotac');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (100, 'GeForce RTX 5060 Ti', 2, 0, 0, 1, 3, '210 W', 0, 0, 1, 1, '2450 MHz', '2730 MHz', 'GDDR7', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-Zotac-RTX-5060-TI-16GB-1.jpg', 100),
       ('PlacaDeVideo-Zotac-RTX-5060-TI-16GB-2.jpg', 100),
       ('PlacaDeVideo-Zotac-RTX-5060-TI-16GB-3.jpg', 100),
       ('PlacaDeVideo-Zotac-RTX-5060-TI-16GB-4.jpg', 100);

-- Componente 101: Placa de Video MSI GeForce RTX 5070 12GB GDDR7 VENTUS 3X
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video MSI GeForce RTX 5070 12GB GDDR7 VENTUS 3X', 841.22, 10, 'MSI');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (101, 'GeForce RTX 5070', 3, 0, 0, 1, 3, '250 W', 0, 0, 1, 1, '2500 MHz', '2800 MHz', 'GDDR7', '12 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-MSI-RTX-5070-12GB-1.jpg', 101),
       ('PlacaDeVideo-MSI-RTX-5070-12GB-2.jpg', 101),
       ('PlacaDeVideo-MSI-RTX-5070-12GB-3.jpg', 101),
       ('PlacaDeVideo-MSI-RTX-5070-12GB-4.jpg', 101);

-- Componente 102: Placa de Video Zotac GeForce RTX 5070 Ti 16GB GDDR7 AMP EXTREME INFINITY
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video Zotac GeForce RTX 5070 Ti 16GB GDDR7 AMP EXTREME INFINITY', 1143.27, 10, 'Zotac');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (102, 'GeForce RTX 5070 Ti', 3, 0, 0, 1, 3, '300 W', 0, 0, 1, 1, '2550 MHz', '2850 MHz', 'GDDR7', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-Zotac-RTX-5070-TI-16GB-1.jpg', 102),
       ('PlacaDeVideo-Zotac-RTX-5070-TI-16GB-2.jpg', 102),
       ('PlacaDeVideo-Zotac-RTX-5070-TI-16GB-3.jpg', 102),
       ('PlacaDeVideo-Zotac-RTX-5070-TI-16GB-4.jpg', 102);

-- Componente 103: Placa de Video ASUS ROG GeForce RTX 5080 16GB GDDR7 ASTRAL OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video ASUS ROG GeForce RTX 5080 16GB GDDR7 ASTRAL OC', 2122.45, 10, 'ASUS');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (103, 'GeForce RTX 5080', 3, 0, 0, 1, 3, '350 W', 0, 0, 1, 1, '2600 MHz', '2900 MHz', 'GDDR7', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-ASUS-RTX-5080-16GB-1.jpg', 103),
       ('PlacaDeVideo-ASUS-RTX-5080-16GB-2.jpg', 103),
       ('PlacaDeVideo-ASUS-RTX-5080-16GB-3.jpg', 103),
       ('PlacaDeVideo-ASUS-RTX-5080-16GB-4.jpg', 103),
       ('PlacaDeVideo-ASUS-RTX-5080-16GB-5.jpg', 103);

-- Componente 104: Placa de Video MSI GeForce RTX 5090 32GB GDDR7 GAMING TRIO OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video MSI GeForce RTX 5090 32GB GDDR7 GAMING TRIO OC', 3428.57, 10, 'MSI');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (104, 'GeForce RTX 5090', 3, 0, 0, 1, 3, '450 W', 0, 0, 1, 1, '2700 MHz', '3100 MHz', 'GDDR7', '32 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-MSI-RTX-5090-32GB-1.jpg', 104),
       ('PlacaDeVideo-MSI-RTX-5090-32GB-2.jpg', 104),
       ('PlacaDeVideo-MSI-RTX-5090-32GB-3.jpg', 104),
       ('PlacaDeVideo-MSI-RTX-5090-32GB-4.jpg', 104),
       ('PlacaDeVideo-MSI-RTX-5090-32GB-5.jpg', 104);

-- Componente 105: Placa de Video Asrock Radeon RX 7600 8GB GDDR6 Steel Legend OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video Asrock Radeon RX 7600 8GB GDDR6 Steel Legend OC', 355.92, 10, 'Asrock');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (105, 'Radeon RX 7600', 3, 0, 0, 1, 3, '165 W', 0, 1, 0, 0, '2250 MHz', '2655 MHz', 'GDDR6', '8 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-Asrock-RX-7600-8GB-1.jpg', 105),
       ('PlacaDeVideo-Asrock-RX-7600-8GB-2.jpg', 105),
       ('PlacaDeVideo-Asrock-RX-7600-8GB-3.jpg', 105),
       ('PlacaDeVideo-Asrock-RX-7600-8GB-4.jpg', 105);

-- Componente 106: Placa de Video XFX Radeon RX 9060 XT 8GB GDDR6 SWIFT OC Gaming Edition
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video XFX Radeon RX 9060 XT 8GB GDDR6 SWIFT OC Gaming Edition', 416.33, 10, 'XFX');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (106, 'Radeon RX 9060 XT', 2, 0, 0, 1, 3, '200 W', 0, 2, 0, 0, '2500 MHz', '2800 MHz', 'GDDR6', '8 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-XFX-RX-7600-XT-8GB-1.jpg', 106),
       ('PlacaDeVideo-XFX-RX-7600-XT-8GB-2.jpg', 106),
       ('PlacaDeVideo-XFX-RX-7600-XT-8GB-3.jpg', 106),
       ('PlacaDeVideo-XFX-RX-7600-XT-8GB-4.jpg', 106),
       ('PlacaDeVideo-XFX-RX-7600-XT-8GB-5.jpg', 106);

-- Componente 107: Placa de Video XFX Radeon RX 9060 XT 16GB GDDR6 SWIFT OC Triple Fan Gaming Edition
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video XFX Radeon RX 9060 XT 16GB GDDR6 SWIFT OC Triple Fan Gaming Edition', 538.78, 10, 'XFX');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (107, 'Radeon RX 9060 XT', 3, 0, 0, 1, 3, '210 W', 0, 2, 0, 0, '2500 MHz', '2815 MHz', 'GDDR6', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-XFX-RX-9060-XT-16GB-1.jpg', 107),
       ('PlacaDeVideo-XFX-RX-9060-XT-16GB-2.jpg', 107),
       ('PlacaDeVideo-XFX-RX-9060-XT-16GB-3.jpg', 107),
       ('PlacaDeVideo-XFX-RX-9060-XT-16GB-4.jpg', 107),
       ('PlacaDeVideo-XFX-RX-9060-XT-16GB-5.jpg', 107);

-- Componente 108: Placa de Video XFX Radeon RX 7800 XT Core 16GB GDDR6 SPEEDSTER QICK 319
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video XFX Radeon RX 7800 XT Core 16GB GDDR6 SPEEDSTER QICK 319', 571.43, 10, 'XFX');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (108, 'Radeon RX 7800 XT', 3, 0, 0, 1, 3, '263 W', 0, 2, 0, 0, '1295 MHz', '2430 MHz', 'GDDR6', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-XFX-RX-7800-XT-16GB-1.jpg', 108),
       ('PlacaDeVideo-XFX-RX-7800-XT-16GB-2.jpg', 108),
       ('PlacaDeVideo-XFX-RX-7800-XT-16GB-3.jpg', 108),
       ('PlacaDeVideo-XFX-RX-7800-XT-16GB-4.jpg', 108);

-- Componente 109: Placa de Video XFX Radeon RX 7900 XT 20GB GDDR6 SPEEDSTER MERC 310 ULTRA
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video XFX Radeon RX 7900 XT 20GB GDDR6 SPEEDSTER MERC 310 ULTRA', 857.14, 10, 'XFX');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (109, 'Radeon RX 7900 XT', 3, 0, 0, 1, 3, '315 W', 0, 2, 0, 0, '1500 MHz', '2500 MHz', 'GDDR6', '20 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-XFX-RX-7900-XT-20GB-1.jpg', 109),
       ('PlacaDeVideo-XFX-RX-7900-XT-20GB-2.jpg', 109),
       ('PlacaDeVideo-XFX-RX-7900-XT-20GB-3.jpg', 109);

-- Componente 110: Placa de Video XFX Radeon RX 9070 XT 16GB GDDR6 Mercury OC Gaming Edition RGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video XFX Radeon RX 9070 XT 16GB GDDR6 Mercury OC Gaming Edition RGB', 911.84, 10, 'XFX');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (110, 'Radeon RX 9070 XT', 3, 0, 0, 1, 3, '300 W', 0, 2, 0, 0, '2600 MHz', '2950 MHz', 'GDDR6', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-XFX-RX-9070-XT-16GB-1.jpg', 110),
       ('PlacaDeVideo-XFX-RX-9070-XT-16GB-2.jpg', 110),
       ('PlacaDeVideo-XFX-RX-9070-XT-16GB-3.jpg', 110),
       ('PlacaDeVideo-XFX-RX-9070-XT-16GB-4.jpg', 110);

-- Componente 111: Placa de Video ASUS PRIME Radeon RX 9070 XT 16GB GDDR6 OC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Placa de Video ASUS PRIME Radeon RX 9070 XT 16GB GDDR6 OC', 938.78, 10, 'ASUS');

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (111, 'Radeon RX 9070 XT', 3, 0, 0, 1, 3, '290 W', 0, 2, 0, 0, '2580 MHz', '2920 MHz', 'GDDR6', '16 GB');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('PlacaDeVideo-ASUS-RX-9070-XT-16GB-1.jpg', 111),
       ('PlacaDeVideo-ASUS-RX-9070-XT-16GB-2.jpg', 111),
       ('PlacaDeVideo-ASUS-RX-9070-XT-16GB-3.jpg', 111),
       ('PlacaDeVideo-ASUS-RX-9070-XT-16GB-4.jpg', 111),
       ('PlacaDeVideo-ASUS-RX-9070-XT-16GB-5.jpg', 111);

-- =================================================================
-- Almacenamiento
-- =================================================================

-- Componente 112: Disco Rigido Seagate 2TB Barracuda 6GB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Rigido Seagate 2TB Barracuda 6GB/s', 65.44, 10, 'Seagate');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (112, '2 TB', 'SATA', '5 W', 'Mecanico', '256 MB', '220 MB/s', NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Rigido-Seagate-2TB-1.jpg', 112);

-- Componente 113: Disco Rigido Seagate 4TB Barracuda 256MB SATA 6GB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Rigido Seagate 4TB Barracuda 6GB/s', 100.41, 10, 'Seagate');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (113, '4 TB', 'SATA', '5 W', 'Mecanico', '256 MB', '190 MB/s', '130 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Rigido-Seagate-4TB-1.jpg', 113);

-- Componente 114: Disco Rigido WD 6TB Purple 5.6K RPM 256MB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Rigido WD 6TB Purple 6GB/s', 146.94, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (114, '6 TB', 'SATA', '5 W', 'Mecanico', '256 MB', '180 MB/s', NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Rigido-WD-6TB-1.jpg', 114);

-- Componente 115: Disco Rigido Seagate 8TB Barracuda 256MB SATA 6GB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Rigido Seagate 8TB Barracuda 6GB/s', 153.47, 10, 'Seagate');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (115, '8 TB', 'SATA', '5 W', 'Mecanico', '256 MB', '190 MB/s', NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Rigido-Seagate-8TB-1.jpg', 115);

-- Componente 116: Disco Solido SSD Team 256GB CX2 520MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD Team 256GB CX2 520MB/s', 19.51, 10, 'Team Group');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (116, '256 GB', 'SATA', '4 W', 'Solido', '128 MB', '520 MB/s', '430 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-TG-256GB-1.jpg', 116);

-- Componente 117: Disco Solido SSD ADATA 512GB SU650SS 520MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD ADATA 512GB SU650SS 520MB/s', 33.63, 10, 'ADATA');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-ADATA-512GB-1.jpg', 117);

-- Componente 118: Disco Solido SSD WD 480GB GREEN 545MB/s SATA
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD WD 480GB GREEN 545MB/s', 34.92, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (118, '480 GB', 'SATA', '3 W', 'Solido', '0 MB', '545 MB/s', '435 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-WD-480GB-1.jpg', 118);

-- Componente 119: Disco Solido SSD Kingston 480GB A400 500MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD Kingston 480GB A400 500MB/s', 39.18, 10, 'Kingston');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (119, '480 GB', 'SATA', '3 W', 'Solido', '0 MB', '500 MB/s', '450 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-Kingston-480GB-1.jpg', 119);

-- Componente 120: Disco Solido SSD WD 1TB Green 545MB/s WDS100T3G0A
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD WD 1TB Green 545MB/s', 61.76, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (120, '1 TB', 'SATA', '3 W', 'Solido', '128 MB', '545 MB/s', '250 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-WD-1TB-1.jpg', 120);

-- Componente 121: Disco Solido SSD ADATA 2TB SU650SS 520MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD ADATA 2TB SU650SS 520MB/s', 107.76, 10, 'ADATA');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (121, '2 TB', 'SATA', '3 W', 'Solido', '128 MB', '520 MB/s', NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-ADATA-2TB-1.jpg', 121);

-- Componente 122: Disco Solido SSD Team 4TB T-Force Vulcan Z 500MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD Team 4TB T-Force Vulcan Z 500MB/s', 236.16, 10, 'T-Force');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (122, '4 TB', 'SATA', '3 W', 'Solido', '128 MB', '500 MB/s', NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-T-Force-4TB-1.jpg', 122);

-- Componente 123: Disco Solido SSD M.2 WD 500GB Green SN350 2400MB/s
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 WD 500GB Green SN350 2400MB/s', 40.90, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (123, '500 GB', 'M2', '3 W', 'Solido', '128 MB', '2400 MB/s', '1500 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-WD-500GB-1.jpg', 123);

-- Componente 124: Disco Solido SSD M.2 Kingston 1TB NV3 6000MB/s NVMe PCIe Gen4 x4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 Kingston 1TB NV3 6000MB/s', 69.71, 10, 'Kingston');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (124, '1 TB', 'M2', '3 W', 'Solido', '128 MB', '6000 MB/s', '4000 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-Kingston-1TB-1.jpg', 124);

-- Componente 125: Disco Solido SSD M.2 Corsair MP600 Core XT 1TB 5000MB/s NVMe PCIe x4 Gen4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 Corsair MP600 Core XT 1TB 5000MB/s', 98.78, 10, 'Corsair');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (125, '1 TB', 'M2', '3 W', 'Solido', '128 MB', '5000 MB/s', '4400 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-Corsair-1TB-1.jpg', 125);

-- Componente 126: Disco Solido SSD M.2 WD 2TB Green SN350 3200MB/s NVMe PCI-E Gen3 x4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 WD 2TB Green SN350 3200MB/s', 129.47, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (126, '2 TB', 'M2', '3 W', 'Solido', '128 MB', '3200 MB/s', '3000 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-WD-2TB-1.jpg', 126);

-- Componente 127: Disco Solido SSD M.2 Crucial 2TB P310 7100MB/s NVMe PCI-E Gen4 x4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 Crucial 2TB P310 7100MB/s', 169.79, 10, 'Crucial');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (127, '2 TB', 'M2', '3 W', 'Solido', '128 MB', '7100 MB/s', '6000 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-Crucial-2TB-1.jpg', 127);

-- Componente 128: Disco Solido SSD M.2 WD 2TB Black SN850X 7300MB/s NVMe PCIe Gen4 x4
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 WD 2TB Black SN850X 7300MB/s', 206.53, 10, 'WD');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (128, '2 TB', 'M2', '7 W', 'Solido', '128 MB', '7300 MB/s', '6600 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-WD-Black-2TB-1.jpg', 128);

-- Componente 129: Disco Solido SSD M.2 Corsair MP600 PRO XT 4TB 7100MB/s NVMe PCIe x4 Gen4 Heatsink Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Disco Solido SSD M.2 Corsair MP600 PRO XT 4TB 7100MB/s Heatsink Black', 402.45, 10, 'Corsair');

INSERT INTO Almacenamiento (id, capacidad, tipoDeConexion, consumo, tipoDeDisco, memoriaCache, velocidadLecturaSecuencial, velocidadEscrituraSecuencial)
VALUES (129, '4 TB', 'M2', '3 W', 'Solido', '128 MB', '7100 MB/s', '6800 MB/s');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Almacenamiento-Disco-Solido-SSD-M2-Corsair-4TB-1.jpg', 129);

-- =================================================================
-- Fuentes de Alimentacion
-- =================================================================

-- Componente 130: Fuente Be Quiet 1200W 80 Plus Platinum STRAIGHT POWER 12
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Be Quiet 1200W 80 Plus Platinum STRAIGHT POWER 12 Full Modular ATX 3.1 PCIe 5.1', 336.33, 7, 'Be Quiet!');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           130, 'ATX', '1200W', '1200W', 'Platinum', 'Full Modular', true, 0, 2, 0, 4, 12, 2, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Be-Quiet-1200W-80-Plus-Platinum-STRAIGHT-POWER-12-Full-Modular-ATX-3-1-PCIe-5-1-1.jpg', 130),
       ('Fuente-Be-Quiet-1200W-80-Plus-Platinum-STRAIGHT-POWER-12-Full-Modular-ATX-3-1-PCIe-5-1-2.jpg', 130),
       ('Fuente-Be-Quiet-1200W-80-Plus-Platinum-STRAIGHT-POWER-12-Full-Modular-ATX-3-1-PCIe-5-1-3.jpg', 130),
       ('Fuente-Be-Quiet-1200W-80-Plus-Platinum-STRAIGHT-POWER-12-Full-Modular-ATX-3-1-PCIe-5-1-4.jpg', 130);

-- Componente 131: Fuente Gigabyte AORUS Elite P850W 80 Plus Platinum
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Gigabyte AORUS Elite P850W 80 Plus Platinum GP-AE850PM PG5 ICE ATX 3.0 PCIe 5.0 Full Modular', 175.51, 10, 'Gigabyte');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           131, 'ATX', '850W', '850W', 'Platinum', 'Full Modular', true, 0, 2, 0, 4, 8, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Gigabyte-AORUS-Elite-P850W-80-Plus-Platinum-GP-AE850PM-PG5-ICE-ATX-3-0-PCIe-5-0-Full-Modular-1.jpg', 131),
       ('Fuente-Gigabyte-AORUS-Elite-P850W-80-Plus-Platinum-GP-AE850PM-PG5-ICE-ATX-3-0-PCIe-5-0-Full-Modular-2.jpg', 131),
       ('Fuente-Gigabyte-AORUS-Elite-P850W-80-Plus-Platinum-GP-AE850PM-PG5-ICE-ATX-3-0-PCIe-5-0-Full-Modular-3.jpg', 131),
       ('Fuente-Gigabyte-AORUS-Elite-P850W-80-Plus-Platinum-GP-AE850PM-PG5-ICE-ATX-3-0-PCIe-5-0-Full-Modular-4.jpg', 131),
       ('Fuente-Gigabyte-AORUS-Elite-P850W-80-Plus-Platinum-GP-AE850PM-PG5-ICE-ATX-3-0-PCIe-5-0-Full-Modular-5.jpg', 131);

-- Componente 132: Fuente Gigabyte AORUS Elite P1000W 80 Plus Platinum
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Gigabyte AORUS Elite P1000W 80 Plus Platinum GP-AE1000PM PG5 ATX 3.0 PCIe 5.0 Full Modular', 207.96, 9, 'Gigabyte');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           132, 'ATX', '1000W', '1000W', 'Platinum', 'Full Modular', true, 0, 2, 0, 6, 12, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Gigabyte-AORUS-Elite-P1000W-80-Plus-Platinum-GP-AE1000PM-PG5-ATX-3-0-PCIe-5-0-Full-Modular-1.jpg', 132),
       ('Fuente-Gigabyte-AORUS-Elite-P1000W-80-Plus-Platinum-GP-AE1000PM-PG5-ATX-3-0-PCIe-5-0-Full-Modular-2.jpg', 132),
       ('Fuente-Gigabyte-AORUS-Elite-P1000W-80-Plus-Platinum-GP-AE1000PM-PG5-ATX-3-0-PCIe-5-0-Full-Modular-3.jpg', 132),
       ('Fuente-Gigabyte-AORUS-Elite-P1000W-80-Plus-Platinum-GP-AE1000PM-PG5-ATX-3-0-PCIe-5-0-Full-Modular-4.jpg', 132),
       ('Fuente-Gigabyte-AORUS-Elite-P1000W-80-Plus-Platinum-GP-AE1000PM-PG5-ATX-3-0-PCIe-5-0-Full-Modular-5.jpg', 132);

-- Componente 133: Fuente ADATA XPG 1200W 80 Plus Gold CORE REACTOR II
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente ADATA XPG 1200W 80 Plus Gold Full Modular ATX 3.0 PCIe 5.0 CORE REACTOR II', 253.47, 11, 'ADATA XPG');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           133, 'ATX', '1200W', '1200W', 'Gold', 'Full Modular', true, 0, 2, 0, 8, 12, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-ADATA-XPG-1200W-80-Plus-Gold-Full-Modular-ATX-3-0-PCIe-5-0-CORE-REACTOR-II-1.jpg', 133),
       ('Fuente-ADATA-XPG-1200W-80-Plus-Gold-Full-Modular-ATX-3-0-PCIe-5-0-CORE-REACTOR-II-2.jpg', 133),
       ('Fuente-ADATA-XPG-1200W-80-Plus-Gold-Full-Modular-ATX-3-0-PCIe-5-0-CORE-REACTOR-II-3.jpg', 133),
       ('Fuente-ADATA-XPG-1200W-80-Plus-Gold-Full-Modular-ATX-3-0-PCIe-5-0-CORE-REACTOR-II-4.jpg', 133);

-- Componente 134: Fuente Gigabyte 850W 80 Plus Gold UD850GM PG5 White
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Gigabyte 850W 80 Plus Gold UD850GM PG5 White PCIe 5.0 Full Modular', 124.00, 15, 'Gigabyte');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           134, 'ATX', '850W', '850W', 'Gold', 'Full Modular', true, 0, 2, 0, 4, 8, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Gigabyte-850W-80-Plus-Gold-UD850GM-PG5-White-PCIe-5-0-Full-Modular-1.jpg', 134),
       ('Fuente-Gigabyte-850W-80-Plus-Gold-UD850GM-PG5-White-PCIe-5-0-Full-Modular-2.jpg', 134),
       ('Fuente-Gigabyte-850W-80-Plus-Gold-UD850GM-PG5-White-PCIe-5-0-Full-Modular-3.jpg', 134),
       ('Fuente-Gigabyte-850W-80-Plus-Gold-UD850GM-PG5-White-PCIe-5-0-Full-Modular-4.jpg', 134),
       ('Fuente-Gigabyte-850W-80-Plus-Gold-UD850GM-PG5-White-PCIe-5-0-Full-Modular-5.jpg', 134);

-- Componente 135: Fuente Cooler Master 850W 80 Plus Gold Full Modular GX2
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Cooler Master 850W 80 Plus Gold Full Modular GX2 12VHPWR ATX 3.0', 134.69, 14, 'Cooler Master');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           135, 'ATX', '850W', '850W', 'Gold', 'Full Modular', true, 0, 2, 0, 4, 12, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Cooler-Master-850W-80-Plus-Gold-Full-Modular-GX2-12VHPWR-ATX-3-0-1.jpg', 135),
       ('Fuente-Cooler-Master-850W-80-Plus-Gold-Full-Modular-GX2-12VHPWR-ATX-3-0-2.jpg', 135),
       ('Fuente-Cooler-Master-850W-80-Plus-Gold-Full-Modular-GX2-12VHPWR-ATX-3-0-3.jpg', 135);

-- Componente 136: Fuente MSI 750W 80 Plus Gold MAG A750GL
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente MSI 750W 80 Plus Gold Full Modular PCIe 5.1 MAG A750GL ATX 3.1', 113.47, 18, 'MSI');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           136, 'ATX', '750W', '750W', 'Gold', 'Full Modular', true, 0, 2, 0, 4, 8, 4, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-MSI-750W-80-Plus-Gold-Full-Modular-PCIe-5-1-MAG-A750GL-ATX-3-1-1.jpg', 136),
       ('Fuente-MSI-750W-80-Plus-Gold-Full-Modular-PCIe-5-1-MAG-A750GL-ATX-3-1-2.jpg', 136),
       ('Fuente-MSI-750W-80-Plus-Gold-Full-Modular-PCIe-5-1-MAG-A750GL-ATX-3-1-3.jpg', 136),
       ('Fuente-MSI-750W-80-Plus-Gold-Full-Modular-PCIe-5-1-MAG-A750GL-ATX-3-1-4.jpg', 136);

-- Componente 137: Fuente Corsair 750W 80 Plus Bronze CX750
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Corsair 750W 80 Plus Bronze CX750', 86.20, 20, 'Corsair');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           137, 'ATX', '750W', '750W', 'Bronze', 'Fijo', true, 0, 2, 0, 4, 6, 4, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Corsair-750W-80-Plus-Bronze-CX750-1.jpg', 137),
       ('Fuente-Corsair-750W-80-Plus-Bronze-CX750-2.jpg', 137),
       ('Fuente-Corsair-750W-80-Plus-Bronze-CX750-3.jpg', 137);

-- Componente 138: Fuente Be Quiet 750W 80 Plus Gold PURE POWER 12 M
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Fuente Be Quiet 750W 80 Plus Gold PURE POWER 12 M Full Modular ATX 3.1 PCIe 5.1', 121.22, 16, 'Be Quiet!');

INSERT INTO FuenteDeAlimentacion (id, formato, wattsNominales, wattsReales, certificacion80Plus, tipoDeCableado, conector24Pines, cantConectoresCpu4Pines, cantConectoresCpu4PinesPlus, cantConectoresCpu6Pines, cantConectoresCpu2PinesPlus, cantConexionesSata, cantConexionesMolex, cantConexionesPcie16Pines)
VALUES (
           138, 'ATX', '750W', '750W', 'Gold', 'Full Modular', true, 0, 2, 0, 4, 6, 2, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Fuente-Be-Quiet-750W-80-Plus-Gold-PURE-POWER-12-M-Full-Modular-ATX-3-1-PCIe-5-1-1.jpg', 138),
       ('Fuente-Be-Quiet-750W-80-Plus-Gold-PURE-POWER-12-M-Full-Modular-ATX-3-1-PCIe-5-1-2.jpg', 138),
       ('Fuente-Be-Quiet-750W-80-Plus-Gold-PURE-POWER-12-M-Full-Modular-ATX-3-1-PCIe-5-1-3.jpg', 138);

-- =================================================================
-- Gabinetes
-- =================================================================

-- Componente 139: Gabinete Antec NX292 ARGB MESH Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Antec NX292 ARGB MESH Black 4x120mm ARGB Fans Vidrio Templado', 68.16, 15, 'Antec');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           139, 'Negro', true, 'Vidrio Templado', 'Mid Tower', 3, true, 0, 0, 8, 4, 2, 0, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Antec-NX292-ARGB-MESH-Black-4x120mm-ARGB-Fans-Vidrio-Templado-1.jpg', 139),
       ('Gabinete-Antec-NX292-ARGB-MESH-Black-4x120mm-ARGB-Fans-Vidrio-Templado-2.jpg', 139),
       ('Gabinete-Antec-NX292-ARGB-MESH-Black-4x120mm-ARGB-Fans-Vidrio-Templado-3.jpg', 139),
       ('Gabinete-Antec-NX292-ARGB-MESH-Black-4x120mm-ARGB-Fans-Vidrio-Templado-4.jpg', 139);

-- Componente 140: Gabinete XYZ Airone 100 Mesh Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete XYZ Airone 100 Mesh Black 3x140mm + 3x120mm ARGB Fans Vidrio Templado', 85.71, 10, 'Xigmatek');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           140, 'Negro', true, 'Vidrio Templado', 'Mid Tower', 3, true, 0, 0, 9, 3, 3, 3, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-XYZ-Airone-100-Mesh-Black-3x140mm-+-3x120mm-ARGB-Fans-Vidrio-Templado-1.jpg', 140),
       ('Gabinete-XYZ-Airone-100-Mesh-Black-3x140mm-+-3x120mm-ARGB-Fans-Vidrio-Templado-2.jpg', 140),
       ('Gabinete-XYZ-Airone-100-Mesh-Black-3x140mm-+-3x120mm-ARGB-Fans-Vidrio-Templado-3.jpg', 140),
       ('Gabinete-XYZ-Airone-100-Mesh-Black-3x140mm-+-3x120mm-ARGB-Fans-Vidrio-Templado-4.jpg', 140);


-- Componente 141: Gabinete Antec C5 ARGB White
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Antec C5 ARGB White 7x120mm ARGB Fans Vidrio Templado 270° Inc Controladora', 130.53, 8, 'Antec');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           141, 'Blanco', true, 'Vidrio Templado', 'Mid Tower', 3, true, 0, 0, 10, 7, 3, 0, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Antec-C5-ARGB-White-7x120mm-ARGB-Fans-Vidrio-Templado-270-Inc-Controladora-1.jpg', 141),
       ('Gabinete-Antec-C5-ARGB-White-7x120mm-ARGB-Fans-Vidrio-Templado-270-Inc-Controladora-2.jpg', 141),
       ('Gabinete-Antec-C5-ARGB-White-7x120mm-ARGB-Fans-Vidrio-Templado-270-Inc-Controladora-3.jpg', 141),
       ('Gabinete-Antec-C5-ARGB-White-7x120mm-ARGB-Fans-Vidrio-Templado-270-Inc-Controladora-4.jpg', 141),
       ('Gabinete-Antec-C5-ARGB-White-7x120mm-ARGB-Fans-Vidrio-Templado-270-Inc-Controladora-5.jpg', 141);


-- Componente 142: Gabinete ASUS ROG STRIX GX601 Helios
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete ASUS ROG STRIX GX601 Helios Aluminum Black RGB USB-C', 322.45, 5, 'ASUS');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           142, 'Negro', true, 'Vidrio Templado', 'Full Tower', 5, true, 0, 0, 4, 1, 7, 3, 0, 0, 1, 1, 1, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-ASUS-ROG-STRIX-GX601-Helios-Aluminum-Black-RGB-USB-C-1.jpg', 142),
       ('Gabinete-ASUS-ROG-STRIX-GX601-Helios-Aluminum-Black-RGB-USB-C-2.jpg', 142),
       ('Gabinete-ASUS-ROG-STRIX-GX601-Helios-Aluminum-Black-RGB-USB-C-3.jpg', 142),
       ('Gabinete-ASUS-ROG-STRIX-GX601-Helios-Aluminum-Black-RGB-USB-C-4.jpg', 142),
       ('Gabinete-ASUS-ROG-STRIX-GX601-Helios-Aluminum-Black-RGB-USB-C-5.jpg', 142);


-- Componente 143: Gabinete Corsair 3000D TG Airflow Black RGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Corsair 3000D TG Airflow Black RGB 3x120mm AR120 ARGB Fans ATX', 100.73, 12, 'Corsair');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           143, 'Negro', true, 'Vidrio Templado', 'Mid Tower', 2, true, 0, 0, 8, 3, 2, 0, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Corsair-3000D-TG-Airflow-Black-RGB-3x120mm-AR120-ARGB-Fans-ATX-1.jpg', 143),
       ('Gabinete-Corsair-3000D-TG-Airflow-Black-RGB-3x120mm-AR120-ARGB-Fans-ATX-2.jpg', 143),
       ('Gabinete-Corsair-3000D-TG-Airflow-Black-RGB-3x120mm-AR120-ARGB-Fans-ATX-3.jpg', 143);


-- Componente 144: Gabinete Corsair 6500X TG Black Dual Chamber
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Corsair 6500X TG Black Dual Chamber', 195.10, 7, 'Corsair');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           144, 'Negro', true, 'Vidrio Templado', 'Mid Tower', 5, true, 0, 0, 10, 0, 6, 0, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Corsair-6500X-TG-Black-Dual-Chamber-1.jpg', 144),
       ('Gabinete-Corsair-6500X-TG-Black-Dual-Chamber-2.jpg', 144),
       ('Gabinete-Corsair-6500X-TG-Black-Dual-Chamber-3.jpg', 144),
       ('Gabinete-Corsair-6500X-TG-Black-Dual-Chamber-4.jpg', 144);


-- Componente 145: Gabinete Thermaltake The Tower 300 Turquoise
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Thermaltake The Tower 300 Turquoise 2x140mm Fans Vidrio Templado USB-C Gen2', 146.82, 6, 'Thermaltake');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           145, 'Turquesa', true, 'Vidrio Templado', 'Micro-ATX Tower', 3, true, 0, 0, 8, 0, 3, 2, 0, 0, 1, 1, 1, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Thermaltake-The-Tower-300-Turquoise-2x140mm-Fans-Vidrio-Templado-USB-C-Gen2-1.jpg', 145),
       ('Gabinete-Thermaltake-The-Tower-300-Turquoise-2x140mm-Fans-Vidrio-Templado-USB-C-Gen2-2.jpg', 145),
       ('Gabinete-Thermaltake-The-Tower-300-Turquoise-2x140mm-Fans-Vidrio-Templado-USB-C-Gen2-3.jpg', 145),
       ('Gabinete-Thermaltake-The-Tower-300-Turquoise-2x140mm-Fans-Vidrio-Templado-USB-C-Gen2-4.jpg', 145);

-- Componente 146: Gabinete Be Quiet! PURE BASE 500DX Black ARGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Be Quiet! PURE BASE 500DX Black ARGB 3x140mm Pure Wings 2 Fans Vidrio Templado USB-C', 118.37, 9, 'Be Quiet!');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           146, 'Negro', true, 'Vidrio Templado', 'Mid Tower', 2, true, 0, 0, 3, 1, 3, 2, 0, 0, 1, 1, 1, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Be-Quiet-PURE-BASE-500DX-Black-ARGB-3x140mm-Pure-Wings-2-Fans-Vidrio-Templado-USB-C-1.jpg', 146),
       ('Gabinete-Be-Quiet-PURE-BASE-500DX-Black-ARGB-3x140mm-Pure-Wings-2-Fans-Vidrio-Templado-USB-C-2.jpg', 146),
       ('Gabinete-Be-Quiet-PURE-BASE-500DX-Black-ARGB-3x140mm-Pure-Wings-2-Fans-Vidrio-Templado-USB-C-3.jpg', 146),
       ('Gabinete-Be-Quiet-PURE-BASE-500DX-Black-ARGB-3x140mm-Pure-Wings-2-Fans-Vidrio-Templado-USB-C-4.jpg', 146),
       ('Gabinete-Be-Quiet-PURE-BASE-500DX-Black-ARGB-3x140mm-Pure-Wings-2-Fans-Vidrio-Templado-USB-C-5.jpg', 146);


-- Componente 147: Gabinete Thermaltake CTE E660 MX Racing Green
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete Thermaltake CTE E660 MX Racing Green Incl. Cable Riser PCIe 4.0 Vidrio Templado USB-C', 176.00, 4, 'Thermaltake');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           147, 'Verde', true, 'Vidrio Templado', 'Mid Tower', 3, true, 0, 0, 11, 0, 9, 0, 0, 0, 1, 1, 1, 1
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-Thermaltake-CTE-E660-MX-Racing-Green-Incl-Cable-Riser-PCIe-4-0-Vidrio-Templado-USB-C-1.jpg', 147),
       ('Gabinete-Thermaltake-CTE-E660-MX-Racing-Green-Incl-Cable-Riser-PCIe-4-0-Vidrio-Templado-USB-C-2.jpg', 147),
       ('Gabinete-Thermaltake-CTE-E660-MX-Racing-Green-Incl-Cable-Riser-PCIe-4-0-Vidrio-Templado-USB-C-3.jpg', 147),
       ('Gabinete-Thermaltake-CTE-E660-MX-Racing-Green-Incl-Cable-Riser-PCIe-4-0-Vidrio-Templado-USB-C-4.jpg', 147);

-- Componente 148: Gabinete DeepCool CH260 Mini Mesh
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Gabinete DeepCool CH260 Mini Mesh Vidrio Templado MATX', 53.06, 20, 'DeepCool');

INSERT INTO Gabinete (id, color, conVentana, tipoDeVentana, tamanioGabinete, cantUSB, audioFrontal, cantCoolerFanDe80mmSoportados, cantCoolerFanDe80mmIncluidos, cantCoolerFanDe120mmSoportados, cantCoolerFanDe120mmIncluidos, cantCoolerFanDe140mmSoportados, cantCoolerFanDe140mmIncluidos, cantCoolerFanDe200mmSoportados, cantCoolerFanDe200mmIncluidos, cantRadiador240mmSoportados, cantRadiador280mmSoportados, cantRadiador360mmSoportados, cantRadiador420mmSoportados)
VALUES (
           148, 'Negro', true, 'Vidrio Templado', 'Mini Tower', 2, true, 0, 0, 4, 0, 2, 0, 0, 0, 1, 1, 0, 0
       );

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Gabinete-DeepCool-CH260-Mini-Mesh-Vidrio-Templado-MATX-1.jpg', 148);

-- =================================================================
-- Monitores
-- =================================================================

-- Componente 149: Monitor Gamer LG Ultragear 24GS60F-B 24" IPS FHD 1920*1080 180Hz G-SYNC FreeSync 1ms
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer LG Ultragear 24GS60F-B 24" IPS FHD 1920*1080 180Hz G-SYNC FreeSync 1ms', 200.00, 16, 'LG');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (149, 'LED', 'IPS', false, 1, 1,
        0, 0, 0, 0, true, '24',
        '1920x1080', '180hz', '1ms', true,
        true, '541mm', '324mm', '43mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-LG-Ultragear-24GS60F-B-24_-IPS-FHD-1920_1080-180Hz-G-SYNC-FreeSync-1ms-1.jpg', 149),
       ('Monitor-Gamer-LG-Ultragear-24GS60F-B-24_-IPS-FHD-1920_1080-180Hz-G-SYNC-FreeSync-1ms-2.jpg', 149);

-- Componente 150: Monitor Samsung 22" T350FH FHD IPS 75Hz
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Samsung 22" T350FH FHD IPS 75Hz', 104.42, 12, 'Samsung');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (150, 'LED', 'IPS', false, 1, 0,
        0, 1, 0, 0, false, '22',
        '1920x1080', '75hz', '5ms', false,
        false, '487mm', '369mm', '65mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Samsung-22_-T350FH-FHD-IPS-75Hz-1.jpg', 150),
       ('Monitor-Samsung-22_-T350FH-FHD-IPS-75Hz-2.jpg', 150);

-- Componente 151: Monitor Gamer Curvo LG 34" UltraWide 34GP63A 160Hz WQHD 2K FreeSync Premium
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer Curvo LG 34" UltraWide 34GP63A 160Hz WQHD 2K FreeSync Premium', 707.24, 8, 'LG');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (151, 'LED', 'VA', true, 2, 1,
        0, 0, 0, 1, true, '34',
        '3440x1440', '160hz', '5ms', false,
        true, '808mm', '359mm', '92mm', '1800r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-Curvo-LG-34_-UltraWide-34GP63A-160Hz-WQHD-2K-FreeSync-Premium-1.jpg', 151),
       ('Monitor-Gamer-Curvo-LG-34_-UltraWide-34GP63A-160Hz-WQHD-2K-FreeSync-Premium-2.jpg', 151);

-- Componente 152: Monitor LG 24MS500-B 24" IPS FHD 100Hz 5ms
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor LG 24MS500-B 24" IPS FHD 100Hz 5ms', 140.33, 14, 'LG');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (152, 'EDGE', 'IPS', false, 2, 0,
        0, 0, 0, 0, true, '24',
        '1920x1080', '100hz', '5ms', false,
        false, '541mm', '324mm', '43mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-LG-24MS500-B-24_-IPS-FHD-100Hz-5ms-1.jpg', 152),
       ('Monitor-LG-24MS500-B-24_-IPS-FHD-1920_1080-100Hz-5ms-2.jpg', 152);

-- Componente 153: Monitor Gamer Curvo ViewSonic VX3218C-2K 32" 1500R QHD 1440p 180Hz VA 1ms MPRT FreeSync Premium
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer Curvo ViewSonic VX3218C-2K 32" 1500R QHD 1440p 180Hz VA 1ms MPRT FreeSync Premium', 390.04, 6, 'ViewSonic');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (153, 'LED', 'VA', true, 2, 1,
        0, 0, 0, 2, true, '32',
        '2560x1440', '180hz', '1ms', false,
        true, '709mm', '423mm', '94mm', '1500r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-Curvo-ViewSonic-VX3218C-2K-32_-1500R-QHD-1440p-180Hz-VA-1ms-MPRT-FreeSync-Premium-1.jpg', 153),
       ('Monitor-Gamer-Curvo-ViewSonic-VX3218C-2K-32_-1500R-QHD-1440p-180Hz-VA-1ms-MPRT-FreeSync-Premium-2.jpg', 153);

-- Componente 154: Monitor Gamer AsRock Phantom PG32QF2B 31.5" VA QHD 165Hz FreeSync Antena WiFi Integrada
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer AsRock Phantom PG32QF2B 31.5" VA QHD 165Hz FreeSync Antena WiFi Integrada', 379.74, 5, 'AsRock');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (154, 'LED', 'VA', false, 2, 1,
        0, 0, 0, 1, true, '31.5',
        '2560x1440', '165hz', '1ms', false,
        true, '708mm', '420mm', '56mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-AsRock-Phantom-PG32QF2B-31.5_-VA-QHD-165Hz-FreeSync-Antena-WiFi-Integrada-1.jpg', 154),
       ('Monitor-Gamer-AsRock-Phantom-PG32QF2B-31.5_-VA-QHD-165Hz-FreeSync-Antena-WiFi-Integrada-2.jpg', 154);

-- Componente 155: Monitor Gamer ASUS VA27EHF-J 27" FHD IPS 100Hz
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer ASUS VA27EHF-J 27" FHD IPS 100Hz', 138.49, 10, 'ASUS');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (155, 'LED', 'IPS', false, 2, 1,
        0, 1, 0, 0, true, '27',
        '1920x1080', '100hz', '1ms', false,
        true, '611mm', '364mm', '52mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-ASUS-VA27EHF-J-27_-FHD-IPS-100Hz-1.jpg', 155),
       ('Monitor-Gamer-ASUS-VA27EHF-J-27_-FHD-IPS-100Hz-2.jpg', 155);

-- Componente 156: Monitor Lenovo ThinkVision S22i-30 21.5" FHD IPS 75Hz Anti Glare VESA
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Lenovo ThinkVision S22i-30 21.5" FHD IPS 75Hz Anti Glare VESA', 96.89, 9, 'Lenovo');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (156, 'WLED', 'IPS', false, 1, 1,
        0, 1, 0, 2, true, '21.5',
        '1920x1080', '75hz', '4ms', false,
        false, '489mm', '295mm', '38mm', '0r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Lenovo-ThinkVision-S22i-30-21.5_-FHD-IPS-75Hz-Anti-Glare-VESA-1.jpg', 156),
       ('Monitor-Lenovo-ThinkVision-S22i-30-21.5_-FHD-IPS-75Hz-Anti-Glare-VESA-2.jpg', 156);

-- Componente 157: Monitor LG 34WP75C-B 34" WQHD 3440x1440 VA 160Hz Ultrapanoramico
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor LG 34WP75C-B 34" WQHD 3440x1440 VA 160Hz Ultrapanoramico', 823.63, 7, 'LG');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (157, 'EDGE', 'VA', true, 2, 1,
        0, 0, 0, 2, true, '34',
        '3440x1440', '160hz', '5ms', false,
        true, '808mm', '359mm', '92mm', '1800r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-LG-34WP75C-B-34_-WQHD-3440x1440-VA-160Hz-Ultrapanoramico-1.jpg', 157),
       ('Monitor-LG-34WP75C-B-34_-WQHD-3440x1440-VA-160Hz-Ultrapanoramico-2.jpg', 157);

-- Componente 158: Monitor Gamer Samsung Odyssey G8 34" OLED UWQHD Curvo 1800R 175Hz AMD Freesync Premium
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Monitor Gamer Samsung Odyssey G8 34" OLED UWQHD Curvo 1800R 175Hz AMD Freesync Premium', 2035.87, 3, 'Samsung');

INSERT INTO Monitor (id, tipoDeIluminacion, tipoDePanel, pantallaCurva, puertosHDMI,
                     puertosDisplayPort, puertosMiniDisplayPort, puertosVGA, puertosDVI,
                     puertosUSB, conectorAuriculares, pulgadas, resolucionMaxima,
                     frecuenciaMaxima, tiempoDeRespuesta, nvidiaGSync, amdFreesync, ancho, alto, espesor, curvatura)
VALUES (158, 'OLED', 'OLED', true, 0, 0,
        1, 0, 0, 1, false, '34',
        '3440x1440', '175hz', '1ms', false,
        true, '814mm', '364mm', '128mm', '1800r');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Monitor-Gamer-Samsung-Odyssey-G8-34_-OLED-UWQHD-Curvo-1800R-175Hz-AMD-Freesync-Premium-1.jpg', 158),
       ('Monitor-Gamer-Samsung-Odyssey-G8-34_-OLED-UWQHD-Curvo-1800R-175Hz-AMD-Freesync-Premium-2.jpg', 158);

-- Componente 159: Mouse Logitech M240 Silent Bluetooth off white
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Logitech M240 Silent Bluetooth off white', 24.48, 20, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (159, '0', 'inalambrico', true, true, false, false, null, false, null);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Logitech-M240-Silent-Bluetooth-off-white-1.jpg', 159),
       ('Periferico-Mouse-Logitech-M240-Silent-Bluetooth-off-white-2.jpg', 159);

-- Componente 160: Mouse Cougar Surpassion RX Wireless Pink RGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Cougar Surpassion RX Wireless Pink RGB', 73.46, 12, 'Cougar');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (160, '1', 'inalambrico', false, false, true, true, 'USB-C', true, '1800m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Cougar-Surpassion-RX-Wireless-Pink-RGB-1.jpg', 160),
       ('Periferico-Mouse-Cougar-Surpassion-RX-Wireless-Pink-RGB-2.jpg', 160);

-- Componente 161: Mouse Logitech G903 Wireless 2.4Ghz Lightspeed 1ms HERO 25K 110g 24Hs
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Logitech G903 Wireless 2.4Ghz Lightspeed 1ms HERO 25K 110g 24Hs', 87.10, 8, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (161, '1', 'inalambrico', false, false, true, true, 'USB-C', true, '1500m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Logitech-G903-Wireless-2-4Ghz-Lightspeed-1ms-HERO-25K-110g-24Hs-1.jpg', 161),
       ('Periferico-Mouse-Logitech-G903-Wireless-2-4Ghz-Lightspeed-1ms-HERO-25K-110g-24Hs-2.jpg', 161),
       ('Periferico-Mouse-Logitech-G903-Wireless-2-4Ghz-Lightspeed-1ms-HERO-25K-110g-24Hs-3.jpg', 161);

-- Componente 162: Mouse Redragon Impact M908 RGB 12.400dpi
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Redragon Impact M908 RGB 12.400dpi', 21.88, 15, 'Redragon');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (162, '1', 'cableado', false, false, false, false, 'USB-A', false, '1800m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Redragon-Impact-M908-RGB-12-400dpi-1.jpg', 162),
       ('Periferico-Mouse-Redragon-Impact-M908-RGB-12-400dpi-2.jpg', 162),
       ('Periferico-Mouse-Redragon-Impact-M908-RGB-12-400dpi-3.jpg', 162);

-- Componente 163: Mouse Logitech Wireless M280 Gray
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Logitech Wireless M280 Gray', 13.87, 22, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (163, '1', 'inalambrico', false, false, true, true, null, false, null);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Logitech-Wireless-M280-Gray-1.jpg', 163),
       ('Periferico-Mouse-Logitech-Wireless-M280-Gray-2.jpg', 163);

-- Componente 164: Mouse Redragon Griffin M607W RGB White
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Mouse Redragon Griffin M607W RGB White', 15.80, 16, 'Redragon');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (164, '1', 'inalambrico', false, false, true, true, 'USB-C', true, '1600m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Mouse-Redragon-Griffin-M607W-RGB-White-1.jpg', 164),
       ('Periferico-Mouse-Redragon-Griffin-M607W-RGB-White-2.jpg', 164);

-- Componente 165: Teclado Logitech K120 Negro USB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Logitech K120 Negro USB', 13.05, 25, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (165, '1', 'cableado', false, false, false, false, 'USB-A', false, '1500m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Logitech-K120-Negro-USB-1.jpg', 165),
       ('Periferico-Teclado-Logitech-K120-Negro-USB-2.jpg', 165);

-- Componente 166: Teclado Redragon Mitra K551 Mechanical Retroiluminado RGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Redragon Mitra K551 Mechanical Retroiluminado RGB', 65.29, 18, 'Redragon');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (166, '1', 'cableado', false, false, false, false, 'USB-A', false, '1800m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Redragon-Mitra-K551-Mechanical-Retroiluminado-RGB-1.jpg', 166),
       ('Periferico-Teclado-Redragon-Mitra-K551-Mechanical-Retroiluminado-RGB-2.jpg', 166);

-- Componente 167: Teclado Mecanico Logitech G915 Low Profile Lightspeed Wireless 2.4Ghz Bluetooth LightSync RGB Switch Brown TKL White 40hs
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Mecanico Logitech G915 Low Profile Lightspeed Wireless 2.4Ghz Bluetooth LightSync RGB Switch Brown TKL White 40hs', 215.14, 6, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (167, '1', 'inalambrico', true, false, true, true, 'USB-C', true, '1200m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Mecanico-Logitech-G915-Low-Profile-Lightspeed-Wireless-2-4Ghz-Bluetooth-LightSync-RGB-Switch-Brown-TKL-White-40hs-1.jpg', 167),
       ('Periferico-Teclado-Mecanico-Logitech-G915-Low-Profile-Lightspeed-Wireless-2-4Ghz-Bluetooth-LightSync-RGB-Switch-Brown-TKL-White-40hs-2.jpg', 167),
       ('Periferico-Teclado-Mecanico-Logitech-G915-Low-Profile-Lightspeed-Wireless-2-4Ghz-Bluetooth-LightSync-RGB-Switch-Brown-TKL-White-40hs-3.jpg', 167);

-- Componente 168: Teclado Mecanico ASUS ROG Strix M701 Azoth RGB Wireless Bluetooth Switch NX Storm White
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Mecanico ASUS ROG Strix M701 Azoth RGB Wireless Bluetooth Switch NX Storm White', 244.89, 4, 'ASUS');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (168, '1', 'inalambrico', true, false, true, true, 'USB-C', true, '1000m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Mecanico-ASUS-ROG-Strix-M701-Azoth-RGB-Wireless-Bluetooth-Switch-NX-Storm-White-1.jpg', 168),
       ('Periferico-Teclado-Mecanico-ASUS-ROG-Strix-M701-Azoth-RGB-Wireless-Bluetooth-Switch-NX-Storm-White-2.jpg', 168),
       ('Periferico-Teclado-Mecanico-ASUS-ROG-Strix-M701-Azoth-RGB-Wireless-Bluetooth-Switch-NX-Storm-White-3.jpg', 168);

-- Componente 169: Teclado Logitech MX Key Graphite VC
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Logitech MX Key Graphite VC', 130.60, 10, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (169, '1', 'inalambrico', true, false, true, true, 'USB-C', true, '1400m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Logitech-MX-Key-Graphite-VC-1.jpg', 169),
       ('Periferico-Teclado-Logitech-MX-Key-Graphite-VC-2.jpg', 169),
       ('Periferico-Teclado-Logitech-MX-Key-Graphite-VC-3.jpg', 169);

-- Componente 170: Teclado Logitech Pebble Keys 2 K380S Multidispositivo Bluetooth Black
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Teclado Logitech Pebble Keys 2 K380S Multidispositivo Bluetooth Black', 33.63, 14, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (170, '0', 'inalambrico', true, true, false, false, null, false, null);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Teclado-Logitech-Pebble-Keys-2-K380S-Multidispositivo-Bluetooth-Black-1.jpg', 170);

-- Componente 171: Auriculares Logitech G PRO X Gaming Premium 7.1
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Logitech G PRO X Gaming Premium 7.1', 171.42, 10, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (171, '1', 'cableado', false, false, false, false, 'USB-C', 0, '2000m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Logitech-G-PRO-X-Gaming-Premium-1.jpg', 171),
       ('Periferico-Auriculares-Logitech-G-PRO-X-Gaming-Premium-2.jpg', 171),
       ('Periferico-Auriculares-Logitech-G-PRO-X-Gaming-Premium-3.jpg', 171);

-- Componente 172: Auriculares Redragon Ire H848 Ultra light Wireless USB Bluetooth White/Pink USB C
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Redragon Ire H848 Ultra light Wireless USB Bluetooth White/Pink USB C', 73.46, 15, 'Redragon');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (172, '0', 'inalambrico', true, false, true, true, 'USB-C', true, '1500m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Redragon-Ire-H848-Ultra-light-Wireless-USB-Bluetooth-White-Pink-USB-C-1.jpg', 172),
       ('Periferico-Auriculares-Redragon-Ire-H848-Ultra-light-Wireless-USB-Bluetooth-White-Pink-USB-C-2.jpg', 172),
       ('Periferico-Auriculares-Redragon-Ire-H848-Ultra-light-Wireless-USB-Bluetooth-White-Pink-USB-C-3.jpg', 172);

-- Componente 173: Auriculares Logitech G935 Wireless 2.4Ghz 7.1 Lightsync DTS Headphone X RGB
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Logitech G935 Wireless 2.4Ghz 7.1 Lightsync DTS Headphone X RGB', 138.77, 8, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (173, '1', 'inalambrico', false, false, true, true, null, false, null);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Logitech-G935-Wireless-2-4Ghz-7-1-Lightsync-DTS-Headphone-X-RGB-1.jpg', 173),
       ('Periferico-Auriculares-Logitech-G935-Wireless-2-4Ghz-7-1-Lightsync-DTS-Headphone-X-RGB-2.jpg', 173),
       ('Periferico-Auriculares-Logitech-G935-Wireless-2-4Ghz-7-1-Lightsync-DTS-Headphone-X-RGB-3.jpg', 173),
       ('Periferico-Auriculares-Logitech-G935-Wireless-2-4Ghz-7-1-Lightsync-DTS-Headphone-X-RGB-4.jpg', 173);

-- Componente 174: Auriculares Logitech G335 White PC/XBOX/PS
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Logitech G335 White PC/XBOX/PS', 88.33, 12, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (174, '0', 'cableado', false, false, false, false, 'Jack 3.5mm', false, '2500m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Logitech-G335-White-PC-XBOX-PS-1.jpg', 174),
       ('Periferico-Auriculares-Logitech-G335-White-PC-XBOX-PS-2.jpg', 174);

-- Componente 175: Auriculares Logitech Astro A50X Wireless Lightspeed 2.4Ghz Bluetooth Black PC/PS5/XBOX HDMI 2.1 24HS
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Logitech Astro A50X Wireless Lightspeed 2.4Ghz Bluetooth Black PC/PS5/XBOX HDMI 2.1 24HS', 447.76, 5, 'Logitech');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (175, '1', 'inalambrico', true, false, true, true, 'USB-C', true, '1800m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Logitech-Astro-A50X-Wireless-Lightspeed-2-4Ghz-Bluetooth-Black-PC-PS5-XBOX-HDMI-2-1-24HS-1.jpg', 175),
       ('Periferico-Auriculares-Logitech-Astro-A50X-Wireless-Lightspeed-2-4Ghz-Bluetooth-Black-PC-PS5-XBOX-HDMI-2-1-24HS-2.jpg', 175),
       ('Periferico-Auriculares-Logitech-Astro-A50X-Wireless-Lightspeed-2-4Ghz-Bluetooth-Black-PC-PS5-XBOX-HDMI-2-1-24HS-3.jpg', 175);

-- Componente 176: Auriculares Redragon Ire H848 Ultra light Wireless USB Bluetooth Black USB C
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Auriculares Redragon Ire H848 Ultra light Wireless USB Bluetooth Black USB C', 73.46, 15, 'Redragon');

INSERT INTO Periferico (id, usbRequeridos, tipoDeConexion, conexionBluetooth, receptorBluetoothIncluido,
                        conexionWireless, receptorWirelessIncluido, tipoDeCable, cableExtraible, largoDelCable)
VALUES (176, '1', 'inalambrico', true, false, true, true, 'USB-C', true, '1500m');

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Periferico-Auriculares-Redragon-Ire-H848-Ultra-light-Wireless-USB-Bluetooth-Black-USB-C-1.jpg', 176),
       ('Periferico-Auriculares-Redragon-Ire-H848-Ultra-light-Wireless-USB-Bluetooth-Black-USB-C-2.jpg', 176);

-- =================================================================
-- Cooler CPU y Graficos Integrados
-- =================================================================

-- Componente 177: Cooler CPU Integrado
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Cooler CPU Incluido en el Procesador', 0.0, 10000, NULL);

INSERT INTO CoolerCPU (
    id, consumo, tdpPredeterminado, tipoDeDisipacion,
    cantCoolersIncluidos, tamanioCoolers, tipoDeIlumninacion, nivelMaximoDeRuido
) VALUES (
             177, '5 W', NULL, 'Aire',
             1, '120 mm', NULL, '25 dBA'
         );

INSERT INTO cooler_socket (cooler_id, socket_id)
VALUES (177, 1), -- AM4
       (177, 2), -- AM5
       (177, 3); -- LGA1700

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Imagen-Para-Producto-Sin-Foto.jpg', 177);

-- Componente 178: Graficos Integrados en Procesador
INSERT INTO Componente (nombre, precio, stock, marca)
VALUES ('Graficos Integrados en el Procesador', 0.0, 10000, NULL);

INSERT INTO PlacaDeVideo (id, chipsetGPU, cantidadDeCoolers, cantidadDeVGA, cantidadDeDVIDigital, cantidadDeHDMI, cantidadDeDisplayport, consumo, cantidadDePCIE6Pines, cantidadDePCIE8Pines, cantidadDePCIE16Pines, cantidadDeAdaptadores16Pines, velocidadDelCoreBase, velocidadDelCoreTurbo, tecnologiaRAM, capacidadRAM)
VALUES (178, NULL, NULL, NULL, NULL, NULL, NULL, '50 W', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO Imagen (urlImagen, componente_id)
VALUES ('Imagen-Para-Producto-Sin-Foto.jpg', 178);
