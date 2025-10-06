-- Usuario de prueba (ADMIN)
INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- ======================
-- Categorías principales
-- ======================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE categorias;
SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO categorias(id, nombre, nombreEnUrl)
VALUES
    (null, 'Electrónica', 'electronica'),
    (null, 'Moda', 'moda'),
    (null, 'Juegos y pasatiempos', 'juegos-y-pasatiempos'),
    (null, 'Hogar', 'hogar'),
    (null, 'Oficina', 'oficina'),
    (null, 'Deportes', 'deportes'),
    (null, 'Automotriz', 'automotriz'),
    (null, 'Bebés', 'bebes'),
    (null, 'Salud y belleza', 'salud-y-belleza');

-- ======================
-- Subcategorías
-- ======================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE subcategorias;
SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO subcategorias (id, nombre, nombreEnUrl, categoria_id)
VALUES
    (NULL, 'Cámaras', 'camaras', 1),
    (NULL, 'Equipos de audio', 'equipos-de-audio', 1),
    (NULL, 'Belleza y bienestar', 'belleza-y-bienestar', 1),
    (NULL, 'Tabletas', 'tabletas', 1),
    (NULL, 'Ordenadores', 'ordenadores', 1),
    (NULL, 'Electrodomésticos', 'electrodomesticos', 1),
    (NULL, 'Equipos de vídeo', 'equipos-de-video', 1),
    (NULL, 'Smartphones', 'smartphones', 1),
    (NULL, 'Teléfonos móviles', 'telefonos-moviles', 1),
    (NULL, 'Otros', 'otros', 1),

    (NULL, 'Ropa', 'ropa', 2),
    (NULL, 'Calzado', 'calzado', 2),
    (NULL, 'Bolsos', 'bolsos', 2),
    (NULL, 'Joyería', 'joyeria', 2),
    (NULL, 'Accesorios', 'accesorios', 2),
    (NULL, 'Relojes de pulsera', 'relojes-de-pulsera', 2);
    ;