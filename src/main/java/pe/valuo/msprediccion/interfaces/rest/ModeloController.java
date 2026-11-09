package pe.valuo.msprediccion.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.valuo.msprediccion.application.port.PlataformaMl;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/** Información del modelo en servicio. Las respuestas son las del servicio de ML, sin cambios. */
@RestController
@RequestMapping("/api/v1/modelos")
@RequiredArgsConstructor
public class ModeloController {

    private final PlataformaMl plataformaMl;

    /** Público (landing page): peso de los 4 macro-factores del mercado inmobiliario. */
    @GetMapping("/sensibilidad-factores")
    public JsonNode sensibilidadFactores(@RequestParam(defaultValue = "venta") String tipoOperacion) {
        return plataformaMl.consultar("/api/v1/modelos/sensibilidad-factores", parametroTipo(tipoOperacion));
    }

    /** Versión, métricas, IPC vigente y distritos que conoce el modelo. */
    @GetMapping("/info")
    public JsonNode info(@RequestParam(defaultValue = "venta") String tipoOperacion) {
        return plataformaMl.consultar("/api/v1/modelo/info", parametroTipo(tipoOperacion));
    }

    private static Map<String, Object> parametroTipo(String tipoOperacion) {
        return Map.of("tipo_operacion", TipoOperacion.desde(tipoOperacion).clave());
    }
}
