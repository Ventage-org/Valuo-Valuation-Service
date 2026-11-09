package pe.valuo.msprediccion.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "valuo")
public record ValuoProperties(Jwt jwt, Cors cors, Ml ml, Inmuebles inmuebles, TipoCambio tipoCambio) {

    /** @param secret clave HMAC en Base64 (mínimo 32 bytes); debe ser la misma que usa msusuarios para firmar */
    public record Jwt(String secret) {
    }

    public record Cors(List<String> origins) {
    }

    // PENDIENTE (X-Admin-Token): si el FastAPI lo exige, agregar `String adminToken` después de `url`
    // (y actualizar los `new ValuoProperties.Ml(...)` de los tests). Ver PlataformaMlAdapter.
    public record Ml(String url, Duration timeoutConexion, Duration timeoutLectura) {
    }

    public record Inmuebles(String url) {
    }

    public record TipoCambio(BigDecimal penPorUsd) {
    }
}
