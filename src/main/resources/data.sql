ALTER TABLE Comentario MODIFY contenido VARCHAR(2000) CHARACTER SET utf8mb4;

INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);