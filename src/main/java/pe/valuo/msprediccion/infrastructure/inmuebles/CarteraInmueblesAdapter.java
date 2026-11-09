package pe.valuo.msprediccion.infrastructure.inmuebles;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.valuo.msprediccion.application.estimacion.DatosCartera;
import pe.valuo.msprediccion.application.port.CarteraInmuebles;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.infrastructure.config.ValuoProperties;
import pe.valuo.msprediccion.infrastructure.http.ClientesHttp;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** POST /api/v1/inmuebles de msinmuebles con el JWT del agente: el inmueble queda a su nombre. */
@Component
class CarteraInmueblesAdapter implements CarteraInmuebles {

    private static final String SERVICIO = "El servicio de cartera (msinmuebles)";

    private final RestClient cliente;

    CarteraInmueblesAdapter(ValuoProperties properties) {
        this.cliente = ClientesHttp.crear(properties.inmuebles().url(), Duration.ofSeconds(2), Duration.ofSeconds(10));
    }

    @Override
    public UUID registrar(String tokenAgente, CaracteristicasInmueble c, DatosCartera d) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("titulo", d.titulo());
        cuerpo.put("descripcion", d.descripcion());
        cuerpo.put("tipoInmueble", c.tipo().name());
        cuerpo.put("distritoId", c.distritoId());
        cuerpo.put("superficieTotalM2", c.superficieTotalM2());
        cuerpo.put("superficieTechadaM2", d.superficieTechadaM2());
        cuerpo.put("habitaciones", c.habitaciones());
        cuerpo.put("banos", c.banos());
        cuerpo.put("estacionamientos", c.estacionamientos());
        cuerpo.put("piso", c.piso());
        cuerpo.put("antiguedadAnios", c.antiguedadAnios());
        cuerpo.put("esEstreno", d.esEstreno());
        cuerpo.put("vistaExterior", c.vistaExterior());
        cuerpo.put("direccion", d.direccion());
        cuerpo.put("latitud", d.latitud());
        cuerpo.put("longitud", d.longitud());
        cuerpo.put("precioPublicado", d.precioPublicado());
        cuerpo.put("imagenesUrls", d.imagenesUrls());
        try {
            JsonNode respuesta = cliente.post().uri("/api/v1/inmuebles")
                    .headers(h -> h.setBearerAuth(tokenAgente))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
            return UUID.fromString(respuesta.path("id").asString());
        } catch (RestClientException e) {
            throw ClientesHttp.traducir(e, SERVICIO);
        }
    }
}
