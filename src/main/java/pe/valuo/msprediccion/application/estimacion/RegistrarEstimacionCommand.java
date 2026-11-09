package pe.valuo.msprediccion.application.estimacion;

import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.util.UUID;

/** @param tokenAgente JWT del agente, que se reenvía a msinmuebles si guarda el inmueble en su cartera */
public record RegistrarEstimacionCommand(
        UUID agenteId,
        String tokenAgente,
        TipoOperacion tipoOperacion,
        CaracteristicasInmueble caracteristicas,
        boolean guardarEnCartera,
        DatosCartera datosCartera) {
}
