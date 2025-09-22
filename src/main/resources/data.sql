-- Usuario de prueba (ADMIN)
INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- ======================
-- Categorías principales
-- ======================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Categorias;
SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Electrónica', 'electronica');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Moda', 'moda');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Juegos y pasatiempos', 'juguetes-y-pasatiempos');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Hogar', 'hogar');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Oficina', 'oficina');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Deportes', 'deportes');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Automotriz', 'automotriz');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Bebés', 'bebes');
INSERT INTO Categorias(id, nombre, nombreEnUrl) VALUES (null, 'Salud y belleza', 'salud-y-belleza');
