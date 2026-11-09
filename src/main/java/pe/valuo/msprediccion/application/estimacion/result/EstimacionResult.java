package pe.valuo.msprediccion.application.estimacion.result;

import com.fasterxml.jackson.annotation.JsonRawValue;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Estimación completa. {@code explicabilidad} (SHAP, 33 variables ordenadas por impacto) y {@code comparables}
 * (muestras RNTP) se devuelven tal como las entrega el modelo, en snake_case; son nulas si el modelo no las envió.
 */
public record EstimacionResult(
        UUID id,
        TipoOperacion tipoOperacion,
        Instant fecha,
        UUID inmuebleId,
        String titulo,
        String direccion,
        TipoInmueble tipoInmueble,
        Short distritoId,
        String distritoNombre,
        BigDecimal superficieTotalM2,
        int habitaciones,
        BigDecimal banos,
        int estacionamientos,
        Integer piso,
        int antiguedadAnios,
        boolean vistaExterior,
        Valores valores,
        BigDecimal mapePct,
        String versionModelo,
        String periodoIpc,
        @JsonRawValue String explicabilidad,
        @JsonRawValue String comparables) {

    /** Montos en dólares y en soles nominales. En alquiler son la renta mensual. */
    public record Valores(
            BigDecimal tipoCambio,
            BigDecimal valorEstimadoUsd,
            BigDecimal precioM2Usd,
            BigDecimal rangoInferiorUsd,
            BigDecimal rangoSuperiorUsd,
            BigDecimal valorEstimadoSoles,
            BigDecimal precioM2Soles,
            BigDecimal rangoInferiorSoles,
            BigDecimal rangoSuperiorSoles) {
    }

    public static EstimacionResult de(Estimacion e) {
        Valores valores = new Valores(e.getTipoCambio(),
                e.enDolares(e.getPrecioSoles()), e.enDolares(e.getPrecioM2Soles()),
                e.enDolares(e.getRangoInferiorSoles()), e.enDolares(e.getRangoSuperiorSoles()),
                e.getPrecioSoles(), e.getPrecioM2Soles(), e.getRangoInferiorSoles(), e.getRangoSuperiorSoles());
        return new EstimacionResult(e.getId(), e.getTipoOperacion(), e.getCreatedAt(), e.getInmuebleId(),
                e.getTitulo(), e.getDireccion(), e.getTipoInmueble(), e.getDistritoId(), e.getDistritoNombre(),
                e.getSuperficieTotalM2(), e.getHabitaciones(), e.getBanos(), e.getEstacionamientos(), e.getPiso(),
                e.getAntiguedadAnios(), e.isVistaExterior(), valores, e.getMapePct(), e.getVersionModelo(),
                e.getPeriodoIpc(), e.getExplicabilidad(), e.getComparables());
    }
}
