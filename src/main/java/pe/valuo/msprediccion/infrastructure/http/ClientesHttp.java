package pe.valuo.msprediccion.infrastructure.http;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.valuo.msprediccion.application.exception.ServicioExternoException;
import pe.valuo.msprediccion.application.exception.ServicioNoDisponibleException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

import java.time.Duration;

/** Creación de clientes REST hacia otros servicios y traducción de sus errores. */
public final class ClientesHttp {

    private ClientesHttp() {
    }

    public static RestClient crear(String url, Duration conexion, Duration lectura) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(conexion);
        fabrica.setReadTimeout(lectura);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    /**
     * Un 4xx se reenvía con el detalle que dio el servicio. Caídas, timeouts, 5xx y credenciales rechazadas
     * (401/403: es un error de configuración entre servicios, no del usuario) se informan como no disponible.
     */
    public static RuntimeException traducir(RestClientException e, String servicio) {
        if (e instanceof RestClientResponseException r && r.getStatusCode().is4xxClientError()) {
            int status = r.getStatusCode().value();
            if (status == 401 || status == 403) {
                return new ServicioNoDisponibleException(
                        servicio + " rechazó las credenciales de msprediccion; revise la configuración");
            }
            JsonNode cuerpo = leer(r);
            JsonNode detail = cuerpo.path("detail");
            String detalle = detail.isString() ? detail.asString() : servicio + " rechazó la solicitud";
            Object errores = cuerpo.has("errores") ? cuerpo.get("errores") : (detail.isContainer() ? detail : null);
            return new ServicioExternoException(status, detalle, errores);
        }
        return new ServicioNoDisponibleException(servicio + " no disponible, intente más tarde");
    }

    private static JsonNode leer(RestClientResponseException e) {
        try {
            JsonNode cuerpo = e.getResponseBodyAs(JsonNode.class);
            return cuerpo == null ? MissingNode.getInstance() : cuerpo;
        } catch (RuntimeException cuerpoNoJson) {
            return MissingNode.getInstance();
        }
    }
}
