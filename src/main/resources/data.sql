INSERT INTO Usuario (id, email, password, rol, activo)
SELECT null, 'test@unlam.edu.ar', 'test', 'ADMIN', true
    WHERE NOT EXISTS (
    SELECT 1
    FROM Usuario
    WHERE email = 'test@unlam.edu.ar'
);

