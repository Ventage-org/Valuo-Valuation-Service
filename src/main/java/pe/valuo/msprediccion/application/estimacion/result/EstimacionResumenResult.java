package pe.valuo.msprediccion.application.estimacion.result;

import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Fila del historial de estimaciones. En alquiler el valor es la renta mensual. */
public record EstimacionResumenResult(
        UUID id,
        TipoOperacion tipoOperacion,
        Instant fecha,
        String titulo,
        String direccion,
        String distritoNombre,
        TipoInmueble tipoInmueble,
        BigDecimal superficieTotalM2,
        BigDecimal valorEstimadoUsd,
        BigDecimal valorEstimadoSoles,
        UUID inmuebleId,
        BigDecimal mapePct) {

    public static EstimacionResumenResult de(Estimacion e) {
        return new EstimacionResumenResult(e.getId(), e.getTipoOperacion(), e.getCreatedAt(), e.getTitulo(),
                e.getDireccion(), e.getDistritoNombre(), e.getTipoInmueble(), e.getSuperficieTotalM2(),
                e.enDolares(e.getPrecioSoles()), e.getPrecioSoles(), e.getInmuebleId(), e.getMapePct());
    }
}
