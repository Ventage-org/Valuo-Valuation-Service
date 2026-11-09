package pe.valuo.msprediccion.infrastructure.ml;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.valuo.msprediccion.application.port.PlataformaMl;
import pe.valuo.msprediccion.infrastructure.config.ValuoProperties;
import pe.valuo.msprediccion.infrastructure.http.ClientesHttp;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/** Consultas de lectura al servicio de ML. */
@Component
class PlataformaMlAdapter implements PlataformaMl {

    private static final String SERVICIO = "El servicio de ML";

    private final RestClient ml;

    PlataformaMlAdapter(ValuoProperties properties) {
        ValuoProperties.Ml config = properties.ml();
        this.ml = ClientesHttp.crear(config.url(), config.timeoutConexion(), config.timeoutLectura());
        // PENDIENTE (X-Admin-Token): si el FastAPI lo exige, reemplazar la línea anterior por:
        // RestClient cliente = ClientesHttp.crear(config.url(), config.timeoutConexion(), config.timeoutLectura());
        // String token = config.adminToken();
        // this.ml = token == null || token.isBlank() ? cliente
        //         : cliente.mutate().defaultHeader("X-Admin-Token", token).build();
    }

    @Override
    public JsonNode consultar(String ruta, Map<String, ?> parametros) {
        try {
            return ml.get()
                    .uri(uri -> {
                        uri.path(ruta);
                        parametros.forEach((nombre, valor) -> {
                            if (valor != null) {
                                uri.queryParam(nombre, valor);
                            }
                        });
                        return uri.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw ClientesHttp.traducir(e, SERVICIO);
        }
    }
}
