SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

ALTER DATABASE tallerwebi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Producto CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE CategoriaProductos CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Usuario CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE Hijo CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO Usuario
(id,dni,nombre,apellido,celular, email, password, rol, activo,fotoPerfil)
VALUES(null,1234567890, 'Pepe','Sujeto',1112341234,'test@unlam.edu.ar', '$2a$10$2ll9SsWXF8a0HWPdz3iKKuBQ9S37sQxnfCFlghyUp7jKJFzlKhazy', 'CLIENTE', true,'https://res.cloudinary.com/dqrka5zry/image/upload/v1780520000/istockphoto-1447126543-612x612_ek0kjw.jpg');

INSERT INTO Hijo (id,curso, fechaNac, fotoPerfil, nombre, idPadre,dni,apellido)
VALUES (null,'TERCERO_C','2020-06-18',null,'Santiago',1,12345,'Sujeto');

INSERT INTO Hijo (id,curso, fechaNac, fotoPerfil, nombre, idPadre,dni,apellido)
VALUES (null,'CUARTO_C','2022-07-28',null,'Ariana',1,223345,'Sujeto');


INSERT INTO Usuario
(id, dni, nombre, apellido, celular, email, password, rol, activo, fotoPerfil)
VALUES(
          null,
          12345678,
          'Señor',
          'Kiosquero',
          1122334456,
          'kionettallerwebi@gmail.com',
          '$2a$10$2ll9SsWXF8a0HWPdz3iKKuBQ9S37sQxnfCFlghyUp7jKJFzlKhazy', -- HASH CORREGIDO (60 chars)
          'KIOSQUERO',
          true,
          null
      );

-- Insert de Categorías
INSERT INTO CategoriaProductos (id, nombreCategoria)
VALUES (1, 'Golosina');

INSERT INTO CategoriaProductos (id, nombreCategoria)
VALUES (2, 'Bebidas');

INSERT INTO CategoriaProductos (id, nombreCategoria)
VALUES (3, 'Librería');

INSERT INTO CategoriaProductos (id, nombreCategoria)
VALUES (4, 'Buffet');

INSERT INTO CategoriaProductos (id, nombreCategoria)
VALUES (5, 'Varios');

-- Insert de productos
INSERT INTO Producto (id, nombre, descripcion, precio,categoria_id,imagen,cantidad)
VALUES (null, 'Rollo Mogul', 'Gomitas de frutas surtidas', 600.00,1,
        'https://res.cloudinary.com/dqrka5zry/image/upload/v1780514334/GOMITAS-MOGUL-ROLLO-FRUTALES-X35GR-1-145_cpjdhr.jpg',5);

INSERT INTO Producto (id, nombre, descripcion, precio,categoria_id,imagen,cantidad)
VALUES (null, 'Alfajor Jorgito Negro', 'Alfajor de chocolate relleno con dulce de leche', 1200.00,1,
        'https://res.cloudinary.com/dqrka5zry/image/upload/v1780514266/alf-jorgito-negro_bf2cig.jpg',5);

INSERT INTO Producto (id, nombre, descripcion, precio,categoria_id,imagen,cantidad)
VALUES (null, 'Chupetín Pico Dulce', 'Chupetín duro sabor frutal', 500.00,1,
        'https://res.cloudinary.com/dqrka5zry/image/upload/v1780514267/pico-dulce_r18ww8.jpg',5);



INSERT INTO Producto (id, nombre, descripcion, precio, categoria_id,imagen,cantidad)
VALUES (null, 'Jugo Baggio 200ml Multifruta', 'Jugo de fruta listo para tomar de 200ml', 800.00, 2,
        'https://res.cloudinary.com/dqrka5zry/image/upload/v1780514267/jugo-baggio-multi_zk6pjh.jpg',5);


INSERT INTO Producto (id, nombre, descripcion, precio, categoria_id,imagen,cantidad)
VALUES (null, 'Cuaderno Éxito N°1', 'Cuaderno tapa dura de 48 hojas rayadas', 8000.00, 3,
        'https://res.cloudinary.com/dqrka5zry/image/upload/v1780514267/cuad-exito-n1_sra7m3.jpg',5);
