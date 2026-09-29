INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- Planes (nivel 1 = Básico, 2 = Premium)
INSERT INTO Plan(id, nombre, descripcion, precio, nivel) VALUES
(1, 'Plan Básico', 'Prevención y atención primaria. 1 consulta mensual cubierta al 100%.', 20000, 1),
(2, 'Plan Premium', 'Cobertura de alta complejidad y bienestar integral. Consultas ilimitadas.', 50000, 2);

-- Servicios: nivelRequerido indica el plan mínimo que los cubre
INSERT INTO Servicio(id, nombre, descripcion, nivelRequerido) VALUES
(1, 'Consulta clínica', 'Atención primaria, chequeo de rutina y control de peso.', 1),
(2, 'Vacunación', 'Antirrábica y séxtuple (perros) / triple felina (gatos).', 1),
(3, 'Desparasitación', 'Control parasitario interno y externo.', 1),
(4, 'Control preventivo', 'Revisión general periódica.', 1),
(5, 'Corte de uñas', 'Cuidado higiénico básico en consulta.', 1),
(6, 'Limpieza de oídos', 'Cuidado higiénico básico en consulta.', 1),
(7, 'Dermatología', 'Consulta con especialista en piel.', 2),
(8, 'Cardiología', 'Consulta con especialista y estudios cardíacos.', 2),
(9, 'Traumatología', 'Consulta con especialista en huesos y articulaciones.', 2),
(10, 'Análisis clínicos', 'Hemograma, bioquímica y análisis de orina.', 2),
(11, 'Radiografía', 'Diagnóstico por imágenes con rayos X.', 2),
(12, 'Ecografía', 'Ecografía abdominal.', 2),
(13, 'Cirugía', 'Quirófano, anestesia e intervenciones programadas.', 2),
(14, 'Internación', 'Estadía 24 hs bajo monitoreo veterinario.', 2),
(15, 'Guardia 24 hs', 'Atención de urgencias a toda hora.', 2);

-- Veterinarias adheridas de ejemplo (datos ficticios)
INSERT INTO Veterinaria(id, nombre, direccion, telefono, cuit, horaApertura, horaCierre) VALUES
(1, 'Veterinaria San Justo', 'Av. Illia 2500, San Justo', '4484-1000', '30-71234567-1', '09:00:00', '18:00:00'),
(2, 'Clínica Animal Ramos', 'Av. de Mayo 800, Ramos Mejía', '4658-2000', '30-71234567-2', '08:00:00', '14:00:00'),
(3, 'Centro Veterinario Casanova', 'Av. Cristianía 3200, Isidro Casanova', '4485-3000', '30-71234567-3', '10:00:00', '19:00:00'),
(4, 'Hospital Veterinario Oeste', 'Florencio Varela 1900, San Justo', '4441-4000', '30-71234567-4', '08:00:00', '20:00:00');

INSERT INTO Veterinaria_Servicio(veterinaria_id, servicio_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 5), (1, 6), (1, 7),
(2, 1), (2, 2), (2, 4), (2, 10), (2, 11),
(3, 1), (3, 3), (3, 4), (3, 8), (3, 9), (3, 12),
(4, 1), (4, 2), (4, 10), (4, 11), (4, 12), (4, 13), (4, 14), (4, 15);

-- Socio de demo: socio@medipet.com / 1234
INSERT INTO Usuario(id, email, password, rol, activo) VALUES(2, 'socio@medipet.com', '1234', 'SOCIO', true);
INSERT INTO Socio(id, nombre, apellido, dni, telefono, usuario_id) VALUES(1, 'Lucía', 'Gómez', '35111222', '1155554444', 2);
INSERT INTO Mascota(id, nombre, especie, raza, fechaNacimiento, peso, socio_id, plan_id) VALUES
(1, 'Firulais', 'PERRO', 'Mestizo', '2019-04-10', 18.5, 1, 1),
(2, 'Michi', 'GATO', 'Siamés', '2021-08-22', 4.2, 1, 2);
