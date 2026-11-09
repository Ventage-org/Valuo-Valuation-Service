package pe.valuo.msprediccion.domain.model.estimacion;

import java.math.BigDecimal;

/**
 * Lo que devuelve el modelo de ML, en soles nominales (en alquiler, renta mensual). La explicación SHAP y las
 * muestras comparables RNTP se conservan como JSON tal cual: este servicio no las interpreta.
 */
public record ResultadoModelo(
        BigDecimal precioSoles,
        BigDecimal precioM2Soles,
        BigDecimal rangoInferiorSoles,
        BigDecimal rangoSuperiorSoles,
        BigDecimal mapePct,
        String versionModelo,
        String periodoIpc,
        String explicabilidadJson,
        String comparablesJson) {
}
