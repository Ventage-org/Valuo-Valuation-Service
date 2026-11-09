package pe.valuo.msprediccion.application.port;

import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Consultas de solo lectura al servicio de ML (info del modelo y sensibilidad de factores). La gobernanza MLOps vive
 * en valuo-monitoring-service. Las respuestas se reenvían tal cual (JSON en snake_case).
 */
public interface PlataformaMl {

    /** Los parámetros con valor nulo no se envían. */
    JsonNode consultar(String ruta, Map<String, ?> parametros);
}
