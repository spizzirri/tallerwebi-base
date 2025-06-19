
DELETE FROM Usuario;
ALTER TABLE Usuario AUTO_INCREMENT = 1;
INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);