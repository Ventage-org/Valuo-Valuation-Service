package pe.valuo.msprediccion.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.valuo.msprediccion.application.port.PlataformaMl;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Información del modelo en servicio. Las respuestas son las del servicio de
 * ML, sin cambios.
 */
@RestController
@RequestMapping("/api/v1/modelos")
@RequiredArgsConstructor
public class ModeloController {

    private final PlataformaMl plataformaMl;

    /**
     * Público (landing page): peso de los 4 macro-factores del mercado
     * inmobiliario.
     */
    @GetMapping("/sensibilidad-factores")
    public JsonNode sensibilidadFactores(@RequestParam(defaultValue = "venta") String tipoOperacion) {
        return sensibilidadFactoresPorTipo(tipoOperacion);
    }

    /**
     * Público: sensibilidad de factores por tipo de operación en la ruta (venta |
     * alquiler).
     */
    @GetMapping("/sensibilidad-factores/{tipoOperacion}")
    public JsonNode sensibilidadFactoresPorTipo(@PathVariable String tipoOperacion) {
        String clave = TipoOperacion.desde(tipoOperacion).clave();
        return plataformaMl.consultar("/api/v1/modelos/sensibilidad-factores/" + clave, Map.of());
    }

    /** Versión, métricas, IPC vigente y distritos que conoce el modelo. */
    @GetMapping("/info")
    public JsonNode info(@RequestParam(defaultValue = "venta") String tipoOperacion) {
        return plataformaMl.consultar("/api/v1/modelo/info", parametroTipo(tipoOperacion));
    }

    /** Contexto socioeconómico y territorial enriquecido por coordenadas para micro-entorno. */
    @GetMapping("/contexto-distrital/{distrito}")
    public JsonNode contextoDistrital(
            @PathVariable String distrito,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        String distModelo = mapearDistritoModelo(distrito);
        Map<String, Object> params = new java.util.HashMap<>();
        if (lat != null) params.put("lat", lat);
        if (lon != null) params.put("lon", lon);
        return plataformaMl.consultar("/api/v1/distritos/" + distModelo + "/contexto", params);
    }

    /** Tendencias de mercado distritales (precios m2, variacion trimestral, tickets). */
    @GetMapping("/tendencias-distritales")
    public JsonNode tendenciasDistritales(
            @RequestParam(name = "tipo_operacion", defaultValue = "venta") String tipoOperacion,
            @RequestParam(name = "tipo_cambio", required = false) Double tipoCambio) {
        String clave = TipoOperacion.desde(tipoOperacion).clave();
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("tipo_operacion", clave);
        if (tipoCambio != null) params.put("tipo_cambio", tipoCambio);
        return plataformaMl.consultar("/api/v1/mercado/tendencias-distritales", params);
    }

    private static String mapearDistritoModelo(String distrito) {
        if (distrito == null) return "";
        String trimmed = distrito.trim();
        if (trimmed.equalsIgnoreCase("Magdalena del Mar")) return "Magdalena";
        if (trimmed.equalsIgnoreCase("Santiago de Surco")) return "Surco";
        return trimmed;
    }

    private static Map<String, Object> parametroTipo(String tipoOperacion) {
        return Map.of("tipo_operacion", TipoOperacion.desde(tipoOperacion).clave());
    }
}
