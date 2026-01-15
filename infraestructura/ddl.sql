-- 1. Tabla de Clientes
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    dni_cif VARCHAR(20),
    tipo_cliente VARCHAR(20) -- 'VIP', 'ESTANDAR'
);

-- 2. Tabla de Expedientes
CREATE TABLE expedientes (
    id SERIAL PRIMARY KEY,
    codigo_expediente VARCHAR(50) UNIQUE,
    fecha_apertura DATE,
    sucursal VARCHAR(50)
);

-- 3. Tabla de Siniestros (La tabla principal para el CDC)
CREATE TABLE siniestros (
    id SERIAL PRIMARY KEY,
    expediente_id INT REFERENCES expedientes(id),
    cliente_id INT REFERENCES clientes(id),
    descripcion TEXT,
    estado VARCHAR(30), -- 'ABIERTO', 'EN_PERITAJE', 'CERRADO'
    cuantia_estimada DECIMAL(12, 2),
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Servicios (Detalle técnico)
CREATE TABLE servicios (
    id SERIAL PRIMARY KEY,
    siniestro_id INT REFERENCES siniestros(id),
    proveedor VARCHAR(100),
    tipo_servicio VARCHAR(50), -- 'GRUA', 'TALLER', 'MEDICO'
    coste DECIMAL(10, 2)
);



-- Insertar Clientes
INSERT INTO clientes (nombre, dni_cif, tipo_cliente) VALUES 
('Logística Norte S.A.', 'A12345678', 'VIP'),
('Juan García Lopez', '12345678Z', 'ESTANDAR');

-- Insertar Expedientes
INSERT INTO expedientes (codigo_expediente, fecha_apertura, sucursal) VALUES 
('EXP-2024-001', '2024-01-15', 'Madrid Central'),
('EXP-2024-002', '2024-02-10', 'Barcelona Norte');

-- Insertar Siniestros iniciales
INSERT INTO siniestros (expediente_id, cliente_id, descripcion, estado, cuantia_estimada) VALUES 
(1, 1, 'Colisión múltiple en autopista A-1', 'ABIERTO', 5000.00),
(2, 2, 'Rotura de luna delantera', 'EN_PERITAJE', 450.00);

-- Insertar Servicios asociados
INSERT INTO servicios (siniestro_id, proveedor, tipo_servicio, coste) VALUES 
(1, 'Grúas Express', 'GRUA', 150.00),
(1, 'Talleres Mecánicos S.L.', 'TALLER', 0.00);

INSERT INTO clientes (nombre, dni_cif, tipo_cliente) VALUES 
('Transportes Veloces S.L.', 'B98765432', 'VIP'),
('Ana María Rodríguez', '45678901K', 'ESTANDAR'),
('Construcciones y Reformas Paco', 'B11223344', 'ESTANDAR'),
('Tech Solutions Global', 'A55667788', 'VIP'),
('Pedro Martínez Soria', '12312312P', 'ESTANDAR'),
('Laura Gómez Espada', '98765432L', 'ESTANDAR'),
('Flota de Taxis Madrid', 'B99887766', 'VIP'),
('Hotel Gran Vía', 'A12312312', 'VIP'),
('Carlos Ruiz Zafón', '56565656C', 'ESTANDAR'),
('Muebles de Diseño IT', 'B44332211', 'ESTANDAR'),
('Sofía Vergara López', '11122233S', 'ESTANDAR'),
('Logística del Sur', 'A99988877', 'VIP'),
('Farmacia Central', 'B77766655', 'ESTANDAR'),
('Luis Enrique Martínez', '44455566L', 'VIP'),
('Restaurante El Buen Comer', 'B33322211', 'ESTANDAR'),
('Isabel La Católica', '00000001R', 'VIP'),
('Talleres Manolo', 'B66655544', 'ESTANDAR'),
('Consultoría Financiera Rex', 'A22233344', 'VIP'),
('Javier Bardem', '88877766J', 'ESTANDAR'),
('Penélope Cruz', '55544433P', 'ESTANDAR'),
('Electrodomésticos Pepe', 'B12398745', 'ESTANDAR');

INSERT INTO expedientes (codigo_expediente, fecha_apertura, sucursal) VALUES 
('EXP-2024-003', '2024-02-15', 'Valencia Puerto'),
('EXP-2024-004', '2024-02-18', 'Sevilla Sur'),
('EXP-2024-005', '2024-03-01', 'Madrid Norte'),
('EXP-2024-006', '2024-03-05', 'Barcelona Diagonal'),
('EXP-2024-007', '2024-03-10', 'Bilbao Centro'),
('EXP-2024-008', '2024-03-12', 'Galicia Costa'),
('EXP-2024-009', '2024-03-15', 'Madrid Central'),
('EXP-2024-010', '2024-03-20', 'Zaragoza'),
('EXP-2024-011', '2024-04-01', 'Valencia Centro'),
('EXP-2024-012', '2024-04-05', 'Málaga Sol'),
('EXP-2024-013', '2024-04-08', 'Sevilla Norte'),
('EXP-2024-014', '2024-04-12', 'Madrid Sur'),
('EXP-2024-015', '2024-04-15', 'Barcelona Sants'),
('EXP-2024-016', '2024-04-20', 'Alicante'),
('EXP-2024-017', '2024-05-01', 'Murcia'),
('EXP-2024-018', '2024-05-05', 'Valladolid'),
('EXP-2024-019', '2024-05-10', 'Toledo'),
('EXP-2024-020', '2024-05-12', 'Madrid Central'),
('EXP-2024-021', '2024-05-15', 'Canarias'),
('EXP-2024-022', '2024-05-20', 'Mallorca'),
('EXP-2024-023', '2024-05-22', 'Ibiza');

INSERT INTO siniestros (expediente_id, cliente_id, descripcion, estado, cuantia_estimada) VALUES 
(3, 3, 'Daños por agua en almacén debido a lluvias', 'ABIERTO', 12000.00),
(4, 4, 'Robo de material informático en oficina', 'EN_PERITAJE', 45000.00),
(5, 5, 'Caída de cascotes sobre vehículo de tercero', 'CERRADO', 350.00),
(6, 6, 'Incendio en cocina de restaurante', 'ABIERTO', 25000.00),
(7, 7, 'Colisión de taxi con mobiliario urbano', 'EN_PERITAJE', 2200.00),
(8, 3, 'Rotura de maquinaria industrial', 'ABIERTO', 8500.00),
(9, 8, 'Fuga de agua en habitación 304', 'CERRADO', 1200.00),
(10, 9, 'Golpe alcance trasero en semáforo', 'ABIERTO', 600.00),
(11, 10, 'Robo de camión de reparto', 'EN_PERITAJE', 60000.00),
(12, 11, 'Caída de cliente en escalera', 'ABIERTO', 3000.00),
(13, 12, 'Pérdida de mercancía refrigerada', 'CERRADO', 5500.00),
(14, 13, 'Rotura de cristal de escaparate', 'ABIERTO', 800.00),
(15, 14, 'Lesión de jugador estrella en entrenamiento', 'EN_PERITAJE', 150000.00),
(16, 15, 'Intoxicación alimentaria leve de cliente', 'ABIERTO', 2000.00),
(17, 16, 'Daños en patrimonio histórico por granizo', 'EN_PERITAJE', 30000.00),
(18, 17, 'Incendio eléctrico en taller', 'ABIERTO', 15000.00),
(19, 18, 'Error en asesoramiento fiscal (RC Profesional)', 'ABIERTO', 10000.00),
(20, 19, 'Accidente de moto en rodaje', 'CERRADO', 4000.00),
(21, 20, 'Mancha de humedad en vivienda vecina', 'ABIERTO', 500.00),
(22, 1, 'Vuelco de camión en carretera secundaria', 'EN_PERITAJE', 25000.00),
(23, 2, 'Robo de joyas en domicilio', 'ABIERTO', 3500.00);

INSERT INTO servicios (siniestro_id, proveedor, tipo_servicio, coste) VALUES 
-- Siniestro 3 (Agua en almacén)
(3, 'Desatascos Pepe', 'FONTANERIA', 300.00),
(3, 'Limpiezas Industriales SL', 'LIMPIEZA', 1200.00),

-- Siniestro 4 (Robo oficina)
(4, 'Seguridad Pro', 'VIGILANCIA', 500.00),
(4, 'Cerrajería 24h', 'CERRAJERIA', 250.00),
(4, 'Informática Forense', 'PERITAJE', 1500.00),

-- Siniestro 5 (Cerrado)
(5, 'Talleres Chapa', 'TALLER', 350.00),

-- Siniestro 6 (Incendio cocina)
(6, 'Bomberos Privados', 'URGENCIA', 2000.00),
(6, 'Reformas Rápidas', 'ALBAÑILERIA', 8000.00),
(6, 'Pinturas y Decoración', 'PINTURA', 3000.00),

-- Siniestro 7 (Taxi)
(7, 'Grúas Madrid', 'GRUA', 120.00),
(7, 'Taller Oficial Toyota', 'TALLER', 1800.00),

-- Siniestro 9 (Agua Hotel)
(9, 'Fontanería Hotelera', 'FONTANERIA', 400.00),
(9, 'Pintores Rápidos', 'PINTURA', 600.00),

-- Siniestro 11 (Robo Camión)
(11, 'Investigaciones Privadas', 'INVESTIGACION', 3000.00),

-- Siniestro 12 (Caída escalera)
(12, 'Clínica La Luz', 'MEDICO', 500.00),
(12, 'Abogados y Asociados', 'LEGAL', 1200.00),

-- Siniestro 14 (Cristal)
(14, 'Cristalería Express', 'REPARACION', 750.00),

-- Siniestro 15 (Lesión VIP)
(15, 'Hospital Ruber', 'MEDICO', 5000.00),
(15, 'Rehabilitación Deportiva', 'FISIOTERAPIA', 2500.00),
(15, 'Dr. Especialista Mundial', 'CONSULTA', 10000.00),

-- Siniestro 17 (Patrimonio)
(17, 'Restauradores de Arte', 'RESTAURACION', 12000.00),

-- Siniestro 18 (Incendio Taller)
(18, 'Limpiezas de Hollín', 'LIMPIEZA', 4000.00),

-- Siniestro 22 (Vuelco Camión - Logística Norte)
(22, 'Grúas de Gran Tonelaje', 'GRUA', 3500.00),
(22, 'Gestión de Residuos', 'LIMPIEZA', 1500.00),
(22, 'Taller de Camiones', 'TALLER', 8000.00),

-- Siniestro 23 (Robo joyas)
(23, 'Policía Científica (Tasas)', 'LEGAL', 150.00),
(23, 'Joyería Tasadora', 'PERITAJE', 300.00);