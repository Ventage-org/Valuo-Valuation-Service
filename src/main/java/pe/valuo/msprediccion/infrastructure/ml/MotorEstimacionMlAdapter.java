package pe.valuo.msprediccion.infrastructure.ml;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.valuo.msprediccion.application.exception.ServicioNoDisponibleException;
import pe.valuo.msprediccion.application.port.MotorEstimacion;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.infrastructure.config.ValuoProperties;
import pe.valuo.msprediccion.infrastructure.http.ClientesHttp;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/** POST /api/v1/prediccion/{venta|alquiler} del servicio de ML. */
@Component
class MotorEstimacionMlAdapter implements MotorEstimacion {

    private static final String SERVICIO = "Servicio de predicción";

    private final RestClient ml;

    MotorEstimacionMlAdapter(ValuoProperties properties) {
        ValuoProperties.Ml config = properties.ml();
        this.ml = ClientesHttp.crear(config.url(), config.timeoutConexion(), config.timeoutLectura());
    }

    @Override
    public ResultadoModelo estimar(TipoOperacion tipo, CaracteristicasInmueble c, String distritoModelo) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("distrito", distritoModelo);
        cuerpo.put("superficie", c.superficieTotalM2());
        cuerpo.put("habitaciones", c.habitaciones());
        cuerpo.put("banios", c.banos());
        cuerpo.put("garajes", c.estacionamientos());
        cuerpo.put("piso", c.piso());
        cuerpo.put("antiguedad", c.antiguedadAnios());
        cuerpo.put("vista_exterior", c.vistaExterior());

        JsonNode respuesta;
        try {
            respuesta = ml.post().uri("/api/v1/prediccion/{tipo}", tipo.clave())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 400 || status == 422) {
                throw new ValorInvalidoException("Datos de entrada inválidos para predicción");
            }
            throw ClientesHttp.traducir(e, SERVICIO);
        } catch (RestClientException e) {
            throw ClientesHttp.traducir(e, SERVICIO);
        }
        return aResultado(respuesta);
    }

    private static ResultadoModelo aResultado(JsonNode r) {
        BigDecimal precio = r == null ? null : monto(r.path("prediccion").path("soles_nominales"));
        if (precio == null || precio.signum() <= 0) {
            throw new ServicioNoDisponibleException(SERVICIO + " devolvió una respuesta sin precio estimado");
        }
        JsonNode intervalo = r.path("intervalo_confianza");
        JsonNode modelo = r.path("modelo");
        BigDecimal mape = monto(intervalo.path("mape_pct"));
        return new ResultadoModelo(
                precio,
                monto(r.path("prediccion").path("precio_m2_nominales")),
                monto(intervalo.path("inferior_nominales")),
                monto(intervalo.path("superior_nominales")),
                mape != null ? mape : monto(modelo.path("mape_test")),
                texto(modelo.path("version")),
                texto(modelo.path("periodo_ipc")),
                r.path("explicabilidad").isObject() ? r.get("explicabilidad").toString() : null,
                comparables(r));
    }

    /** Agrupa las muestras RNTP y su resumen en un solo JSON; nulo si el modelo no los envió. */
    private static String comparables(JsonNode r) {
        if (!r.has("comparables_rntp") && !r.has("resumen_rntp")) {
            return null;
        }
        ObjectNode nodo = JsonNodeFactory.instance.objectNode();
        nodo.set("comparables_rntp", r.get("comparables_rntp"));
        nodo.set("resumen_rntp", r.get("resumen_rntp"));
        return nodo.toString();
    }

    private static BigDecimal monto(JsonNode nodo) {
        return nodo.isNumber() ? nodo.decimalValue().setScale(2, RoundingMode.HALF_UP) : null;
    }

    private static String texto(JsonNode nodo) {
        return nodo.isValueNode() && !nodo.isNull() ? nodo.asString() : null;
    }
}
