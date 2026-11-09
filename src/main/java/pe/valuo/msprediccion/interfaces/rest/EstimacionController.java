package pe.valuo.msprediccion.interfaces.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.valuo.msprediccion.application.estimacion.ConsultarEstimacionUseCase;
import pe.valuo.msprediccion.application.estimacion.ConsultarKpisUseCase;
import pe.valuo.msprediccion.application.estimacion.ListarEstimacionesUseCase;
import pe.valuo.msprediccion.application.estimacion.RegistrarEstimacionUseCase;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResult;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResumenResult;
import pe.valuo.msprediccion.application.estimacion.result.KpisResult;
import pe.valuo.msprediccion.domain.model.Pagina;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.interfaces.rest.dto.EstimarRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estimaciones")
@RequiredArgsConstructor
public class EstimacionController {

    private final RegistrarEstimacionUseCase registrarEstimacion;
    private final ListarEstimacionesUseCase listarEstimaciones;
    private final ConsultarEstimacionUseCase consultarEstimacion;
    private final ConsultarKpisUseCase consultarKpis;

    @PostMapping
    public ResponseEntity<EstimacionResult> estimar(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody EstimarRequest request) {
        EstimacionResult resultado = registrarEstimacion.ejecutar(request.aCommand(agenteId(jwt), jwt.getTokenValue()));
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /** Historial del agente; {@code tipoOperacion} (venta / alquiler) es opcional. */
    @GetMapping
    public Pagina<EstimacionResumenResult> historial(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(required = false) String tipoOperacion,
                                                     @RequestParam(defaultValue = "0") @Min(0) int pagina,
                                                     @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamanio) {
        TipoOperacion tipo = tipoOperacion == null ? null : TipoOperacion.desde(tipoOperacion);
        return listarEstimaciones.ejecutar(agenteId(jwt), tipo, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public EstimacionResult consultar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return consultarEstimacion.ejecutar(agenteId(jwt), id);
    }

    /** {@code estimacionesTotales} del dashboard. */
    @GetMapping("/dashboard/kpis")
    public KpisResult kpis(@AuthenticationPrincipal Jwt jwt) {
        return consultarKpis.ejecutar(agenteId(jwt));
    }

    private UUID agenteId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
