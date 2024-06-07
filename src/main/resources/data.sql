INSERT INTO Usuario (id, email, password, rol, activo, objetivo)
SELECT null, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'PERDIDA_DE_PESO'
    WHERE NOT EXISTS (
    SELECT 1
    FROM Usuario
    WHERE email = 'test@unlam.edu.ar'
);

