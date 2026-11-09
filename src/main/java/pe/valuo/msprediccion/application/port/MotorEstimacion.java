package pe.valuo.msprediccion.application.port;

import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

/** Modelo de ML (FastAPI) que calcula la estimación de precio de mercado. */
public interface MotorEstimacion {

    /** @param distritoModelo nombre del distrito tal como lo conoce el modelo */
    ResultadoModelo estimar(TipoOperacion tipo, CaracteristicasInmueble caracteristicas, String distritoModelo);
}
