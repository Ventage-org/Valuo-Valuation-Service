package pe.valuo.msprediccion.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import pe.valuo.msprediccion.infrastructure.config.ValuoProperties;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/** Este servicio solo valida los tokens que emite msusuarios; no firma ninguno. */
@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(ValuoProperties properties) {
        byte[] clave = Base64.getDecoder().decode(properties.jwt().secret());
        if (clave.length < 32) {
            throw new IllegalStateException("valuo.jwt.secret debe tener al menos 32 bytes (Base64)");
        }
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(clave, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
