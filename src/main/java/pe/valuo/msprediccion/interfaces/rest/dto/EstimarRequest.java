package pe.valuo.msprediccion.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.valuo.msprediccion.application.estimacion.DatosCartera;
import pe.valuo.msprediccion.application.estimacion.RegistrarEstimacionCommand;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Formulario de estimación. Es el mismo cuerpo que acepta POST /api/v1/inmuebles de msinmuebles más
 * {@code tipoPrediccion} y {@code guardarEnCartera}; los campos de cartera solo se usan si se guarda.
 * Los indicadores sí/no son opcionales: si no llegan valen {@code false}.
 */
public record EstimarRequest(
        @NotNull TipoOperacion tipoPrediccion,
        Boolean guardarEnCartera,
        @Size(max = 150) String titulo,
        @Size(max = 2000) String descripcion,
        @NotNull TipoInmueble tipoInmueble,
        @NotNull Short distritoId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal superficieTotalM2,
        @DecimalMin(value = "0", inclusive = false) BigDecimal superficieTechadaM2,
        @NotNull @Min(0) Integer habitaciones,
        @NotNull @DecimalMin("1.0") BigDecimal banos,
        @NotNull @Min(0) Integer estacionamientos,
        @Min(1) Integer piso,
        @NotNull @Min(0) Integer antiguedadAnios,
        Boolean esEstreno,
        Boolean vistaExterior,
        @Size(max = 255) String direccion,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal latitud,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal longitud,
        @DecimalMin(value = "0", inclusive = false) BigDecimal precioPublicado,
        @Size(max = 10) List<@NotBlank @Size(max = 500) String> imagenesUrls) {

    public RegistrarEstimacionCommand aCommand(UUID agenteId, String tokenAgente) {
        CaracteristicasInmueble caracteristicas = new CaracteristicasInmueble(tipoInmueble, distritoId,
                superficieTotalM2, habitaciones, banos, estacionamientos, piso, antiguedadAnios, Boolean.TRUE.equals(vistaExterior));
        DatosCartera cartera = new DatosCartera(titulo, descripcion, superficieTechadaM2, Boolean.TRUE.equals(esEstreno), direccion,
                latitud, longitud, precioPublicado, imagenesUrls);
        return new RegistrarEstimacionCommand(agenteId, tokenAgente, tipoPrediccion, caracteristicas,
                Boolean.TRUE.equals(guardarEnCartera), cartera);
    }
}
