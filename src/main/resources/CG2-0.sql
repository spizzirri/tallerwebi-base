CREATE TABLE productos (
    idProducto BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150),
    precio DOUBLE,
    stock INTEGER
);

/*Estas imagenes van a ser de todos los productos*/
CREATE TABLE imagenes (
    idImagen BIGINT PRIMARY KEY AUTO_INCREMENT,
    idProducto BIGINT NOT NULL,
    urlImagen TEXT NOT NULL,
    FOREIGN KEY (idProducto) REFERENCES productos(idProducto) ON DELETE CASCADE
    /*Lo que hace el ON DELETE CASCADE es que no queden tablas que no esten asociadas a un producto, si se borra un producto se borra lo que lo conforma*/
);

CREATE TABLE procesadores (
    idProcesador BIGINT PRIMARY KEY,
    FOREIGN KEY (idProcesador) REFERENCES productos(idProducto) ON DELETE CASCADE
);

CREATE TABLE procesadorCaracteristicasGenerales (
    idProcesador BIGINT PRIMARY KEY,
    modelo VARCHAR(100),
    socket VARCHAR(100),
    procesoFabricacion VARCHAR(100),
    incluyeGraficosIntegrados BOOLEAN,
    chipsetGPU VARCHAR(100),
    familia VARCHAR(100),
    FOREIGN KEY (idProcesador) REFERENCES procesadores(idProcesador) ON DELETE CASCADE
);

CREATE TABLE procesadorEspecificacionesCPU (
    idProcesador BIGINT PRIMARY KEY,
    nucleos INTEGER,
    hilos INTEGER,
    frecuencia VARCHAR(100),
    frecuenciaTurbo VARCHAR(100),
    FOREIGN KEY (idProcesador) REFERENCES procesadores(idProcesador) ON DELETE CASCADE
);

CREATE TABLE procesadorCoolersYDisipadores (
    idProcesador BIGINT PRIMARY KEY,
    incluyeCooler BOOLEAN,
    tdpPredeterminado VARCHAR(100),
    FOREIGN KEY (idProcesador) REFERENCES procesadores(idProcesador) ON DELETE CASCADE
);

CREATE TABLE procesadorMemoria (
    idProcesador BIGINT PRIMARY KEY,
    l1Cache VARCHAR(100),
    l2Cache VARCHAR(100),
    l3Cache VARCHAR(100),
    FOREIGN KEY (idProcesador) REFERENCES procesadores(idProcesador) ON DELETE CASCADE
);


