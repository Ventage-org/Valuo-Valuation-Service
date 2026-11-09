package pe.valuo.msprediccion.domain.model.estimacion;

import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;

import java.math.BigDecimal;

/**
 * Value object con las características que necesita el modelo para estimar. Repite las invariantes de
 * msinmuebles porque una estimación puede hacerse sin guardar el inmueble en la cartera.
 */
public record CaracteristicasInmueble(
        TipoInmueble tipo,
        Short distritoId,
        BigDecimal superficieTotalM2,
        int habitaciones,
        BigDecimal banos,
        int estacionamientos,
        Integer piso,
        int antiguedadAnios,
        boolean vistaExterior) {

    private static final BigDecimal MEDIO = new BigDecimal("0.5");

    public CaracteristicasInmueble {
        if (tipo == null) {
            throw new ValorInvalidoException("El tipo de inmueble es obligatorio");
        }
        if (distritoId == null) {
            throw new ValorInvalidoException("El distrito es obligatorio");
        }
        if (superficieTotalM2 == null || superficieTotalM2.signum() <= 0) {
            throw new ValorInvalidoException("La superficie total debe ser mayor a 0");
        }
        if (habitaciones < 0 || estacionamientos < 0 || antiguedadAnios < 0) {
            throw new ValorInvalidoException("Habitaciones, estacionamientos y antigüedad no pueden ser negativos");
        }
        if (banos == null || banos.compareTo(BigDecimal.ONE) < 0 || banos.remainder(MEDIO).signum() != 0) {
            throw new ValorInvalidoException("Los baños deben ser mínimo 1.0 y en pasos de 0.5");
        }
        if (tipo == TipoInmueble.CASA && piso != null) {
            throw new ValorInvalidoException("Una casa no tiene piso");
        }
        if (tipo == TipoInmueble.DEPARTAMENTO && (piso == null || piso < 1)) {
            throw new ValorInvalidoException("El piso de un departamento debe ser mayor o igual a 1");
        }
    }
}
