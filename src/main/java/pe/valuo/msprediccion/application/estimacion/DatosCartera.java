package pe.valuo.msprediccion.application.estimacion;

import java.math.BigDecimal;
import java.util.List;

/** Campos del formulario que el modelo no usa pero msinmuebles necesita para registrar el inmueble. */
public record DatosCartera(
        String titulo,
        String descripcion,
        BigDecimal superficieTechadaM2,
        boolean esEstreno,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal precioPublicado,
        List<String> imagenesUrls) {
}
