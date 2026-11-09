package pe.valuo.msprediccion.application.port;

import pe.valuo.msprediccion.application.estimacion.DatosCartera;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;

import java.util.UUID;

/** Cartera del agente en msinmuebles. */
public interface CarteraInmuebles {

    /** Registra el inmueble a nombre del dueño del token y devuelve su id. */
    UUID registrar(String tokenAgente, CaracteristicasInmueble caracteristicas, DatosCartera datos);
}
