CREATE TABLE distrito (
    id            SMALLINT    PRIMARY KEY,
    nombre        VARCHAR(80) NOT NULL UNIQUE,
    nombre_modelo VARCHAR(80) NOT NULL,
    zona_urbana   VARCHAR(30) NOT NULL,
    activo        BOOLEAN     NOT NULL DEFAULT TRUE
);

-- Copia del catalogo BCRP de msusuarios (mismos ids). Sin llamadas entre servicios.
-- nombre_modelo es el nombre que espera el servicio de ML (GET /api/v1/modelo/info -> distritos_disponibles).
INSERT INTO distrito (id, nombre, nombre_modelo, zona_urbana) VALUES
    (1,  'Ate Vitarte',        'Ate Vitarte',        'Lima Este'),
    (2,  'Barranco',           'Barranco',           'Lima Top'),
    (3,  'Bellavista',         'Bellavista',         'Callao'),
    (4,  'Breña',              'Breña',              'Lima Centro'),
    (5,  'Carabayllo',         'Carabayllo',         'Lima Norte'),
    (6,  'Cercado de Lima',    'Lima',               'Lima Centro'),
    (7,  'Chorrillos',         'Chorrillos',         'Lima Sur'),
    (8,  'Comas',              'Comas',              'Lima Norte'),
    (9,  'Jesús María',        'Jesus Maria',        'Lima Moderna'),
    (10, 'La Molina',          'La Molina',          'Lima Top'),
    (11, 'La Perla',           'Callao',             'Callao'),
    (12, 'La Victoria',        'La Victoria',        'Lima Centro'),
    (13, 'Lince',              'Lince',              'Lima Moderna'),
    (14, 'Los Olivos',         'Los Olivos',         'Lima Norte'),
    (15, 'Magdalena del Mar',  'Magdalena del Mar',  'Lima Moderna'),
    (16, 'Miraflores',         'Miraflores',         'Lima Top'),
    (17, 'Pueblo Libre',       'Pueblo Libre',       'Lima Moderna'),
    (18, 'San Borja',          'San Borja',          'Lima Top'),
    (19, 'San Isidro',         'San Isidro',         'Lima Top'),
    (20, 'San Miguel',         'San Miguel',         'Lima Moderna'),
    (21, 'Santiago de Surco',  'Santiago de Surco',  'Lima Top'),
    (22, 'Surquillo',          'Surquillo',          'Lima Moderna');
