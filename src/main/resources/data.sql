INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- insert de entidades del proyecto para componentes default

-- Inserts para la tabla de lookup: Socket
-- Se crean primero ya que Motherboard y CoolerCPU dependen de ellos.
INSERT INTO Socket (id, nombre) VALUES (1, 'LGA1700');
INSERT INTO Socket (id, nombre) VALUES (2, 'AM5');
INSERT INTO Socket (id, nombre) VALUES (3, 'AM4');
INSERT INTO Socket (id, nombre) VALUES (4, 'LGA1200');

-- =================================================================
-- Procesadores
-- =================================================================

-- Componente 1: Intel Core i5 (Socket LGA1700 → id = 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (1, 'Intel Core i5', 150000.0, 10, 'Intel');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             1, 'i5-12400F', 1, '10 nm', false,
             NULL, 'Core i5', 6, 12, '2.5 GHz', '4.4 GHz',
             true, '65W', '480KB', '7.5MB', '18MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (1, 'intel-core-i5-1.jpg', 1);

-- Componente 2: AMD Ryzen 5 (Socket AM4 → id = 3)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (2, 'AMD Ryzen 5', 140000.0, 8, 'AMD');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             2, 'Ryzen 5 5600X', 3, '7 nm', false,
             NULL, 'Ryzen 5', 6, 12, '3.7 GHz', '4.6 GHz',
             true, '65W', '384KB', '3MB', '32MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (2, 'amd-ryzen-5-1.jpg', 2);

-- Componente 3: Intel Core i7 (Socket LGA1700 → id = 1)
INSERT INTO Componente (id, nombre, precio, stock, marca)
VALUES (3, 'Intel Core i7', 200000.0, 5, 'Intel');

INSERT INTO Procesador (
    id, modelo, socket_id, procesoDeFabricacion, incluyeGraficosIntegrados,
    chipsetGPU, familia, nucleos, hilos, frecuencia, frecuenciaTurbo,
    incluyeCooler, tdpPredeterminado, l1Cache, l2Cache, l3Cache
) VALUES (
             3, 'i7-13700K', 1, '10 nm', true,
             'Intel UHD Graphics 770', 'Core i7', 16, 24, '3.4 GHz', '5.4 GHz',
             false, '125W', '1.4MB', '24MB', '30MB'
         );

INSERT INTO Imagen (id, urlImagen, componente_id)
VALUES (3, 'intel-core-i7-1.jpg', 3);

-- =================================================================
-- Motherboards
-- =================================================================

-- Componente 4: ASUS Prime B550
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (4, 'ASUS Prime B550', 100000.0, 12, 'ASUS');
INSERT INTO Motherboard (id, socket_id, chipsetPrincipal, plataforma, factor, cantSlotsM2, cantPuertosSata, cantPuertosUSB, tipoMemoria, cantSlotsRAM, consumo, cantConector24Pines, cantConector4Pines) VALUES (4, 3, 'B550', 'AMD', 'ATX', 2, 6, 8, 'DDR4', 4, '50W', 1, 1);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (4, 'asus-prime-b550-1.jpg', 4);

-- Componente 5: Gigabyte B450
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (5, 'Gigabyte B450', 95000.0, 9, 'Gigabyte');
INSERT INTO Motherboard (id, socket_id, chipsetPrincipal, plataforma, factor, cantSlotsM2, cantPuertosSata, cantPuertosUSB, tipoMemoria, cantSlotsRAM, consumo, cantConector24Pines, cantConector4Pines) VALUES (5, 3, 'B450', 'AMD', 'Micro-ATX', 1, 4, 6, 'DDR4', 2, '50W', 1, 1);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (5, 'gigabyte-b450-1.jpg', 5);

-- Componente 6: MSI Z490
INSERT INTO Componente (id, nombre, precio, stock, marca) VALUES (6, 'MSI Z490', 120000.0, 6, 'MSI');
INSERT INTO Motherboard (id, socket_id, chipsetPrincipal, plataforma, factor, cantSlotsM2, cantPuertosSata, cantPuertosUSB, tipoMemoria, cantSlotsRAM, consumo, cantConector24Pines, cantConector4Pines) VALUES (6, 4, 'Z490', 'Intel', 'ATX', 2, 6, 7, 'DDR4', 4, '60W', 1, 1);
INSERT INTO Imagen (id, urlImagen, componente_id) VALUES (6, 'msi-z490-1.jpg', 6);


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