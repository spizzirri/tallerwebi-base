INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true );

INSERT INTO Publicacion(id, descripcion, usuario_id)
VALUES (1, 'Mi primera publicación', 1);