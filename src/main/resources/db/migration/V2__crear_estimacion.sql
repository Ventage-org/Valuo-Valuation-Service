CREATE TABLE estimacion (
    id                    UUID          PRIMARY KEY,
    agente_id             UUID          NOT NULL,
    tipo_operacion        VARCHAR(10)   NOT NULL CHECK (tipo_operacion IN ('VENTA', 'ALQUILER')),
    -- Inmueble creado en msinmuebles cuando el agente eligió guardarlo en su cartera.
    inmueble_id           UUID,
    titulo                VARCHAR(150),
    direccion             VARCHAR(255),
    tipo_inmueble         VARCHAR(20)   NOT NULL CHECK (tipo_inmueble IN ('DEPARTAMENTO', 'CASA')),
    distrito_id           SMALLINT      NOT NULL REFERENCES distrito (id),
    distrito_nombre       VARCHAR(80)   NOT NULL,
    superficie_total_m2   NUMERIC(10,2) NOT NULL CHECK (superficie_total_m2 > 0),
    habitaciones          INTEGER       NOT NULL CHECK (habitaciones >= 0),
    banos                 NUMERIC(3,1)  NOT NULL CHECK (banos >= 1),
    estacionamientos      INTEGER       NOT NULL CHECK (estacionamientos >= 0),
    piso                  INTEGER       CHECK (piso >= 1),
    antiguedad_anios      INTEGER       NOT NULL CHECK (antiguedad_anios >= 0),
    vista_exterior        BOOLEAN       NOT NULL,
    -- Valores en soles nominales tal como los devuelve el modelo; en alquiler es la renta mensual.
    precio_soles          NUMERIC(14,2) NOT NULL CHECK (precio_soles > 0),
    precio_m2_soles       NUMERIC(14,2),
    rango_inferior_soles  NUMERIC(14,2),
    rango_superior_soles  NUMERIC(14,2),
    -- Soles por dólar vigente al estimar: los montos en USD se calculan con este valor.
    tipo_cambio           NUMERIC(8,4)  NOT NULL CHECK (tipo_cambio > 0),
    mape_pct              NUMERIC(6,2),
    version_modelo        VARCHAR(40),
    periodo_ipc           VARCHAR(10),
    explicabilidad        JSONB,
    comparables           JSONB,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_estimacion_agente_created ON estimacion (agente_id, created_at DESC);

CREATE TABLE auditoria_gobernanza (
    id               UUID         PRIMARY KEY,
    accion           VARCHAR(30)  NOT NULL
        CHECK (accion IN ('ENTRENAMIENTO_LANZADO', 'MODELO_PROMOVIDO', 'MODELO_REVERTIDO', 'IPC_ACTUALIZADO')),
    tipo_operacion   VARCHAR(10)  CHECK (tipo_operacion IN ('VENTA', 'ALQUILER')),
    version_origen   VARCHAR(20),
    version_destino  VARCHAR(20),
    delta_mape_pct   NUMERIC(8,4),
    motivo           VARCHAR(500),
    ejecutado_por    VARCHAR(150) NOT NULL,
    exitoso          BOOLEAN      NOT NULL,
    metadata         JSONB,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_created ON auditoria_gobernanza (created_at DESC);
