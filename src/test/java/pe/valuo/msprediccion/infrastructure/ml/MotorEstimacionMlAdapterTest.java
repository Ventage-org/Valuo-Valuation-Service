package pe.valuo.msprediccion.infrastructure.ml;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.valuo.msprediccion.application.exception.ServicioNoDisponibleException;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.infrastructure.config.ValuoProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Contrato con el servicio de ML según su guía, contra un FastAPI simulado. */
class MotorEstimacionMlAdapterTest {

    private static final String RESPUESTA_VENTA = """
            {"distrito":"Miraflores","superficie_m2":120.0,
             "prediccion":{"soles_constantes":480165.21,"soles_nominales":812343.4567,
                           "precio_m2_constantes":4001.38,"precio_m2_nominales":6769.53},
             "intervalo_confianza":{"inferior_nominales":697300.1,"superior_nominales":927386.8,
                                    "inferior_constantes":412165.0,"superior_constantes":548165.4,"mape_pct":14.162},
             "explicabilidad":{"valor_base_constantes":12.9,"n_features_total":33,
                               "contribuciones":[{"feature":"distrito_encoded","label":"Distrito (Miraflores)",
                                                  "shap_value":0.21,"impacto":"positivo","peso_porcentaje":31.2}]},
             "modelo":{"version":"v8_mlflow","mape_test":14.162,"r2_test":0.7472,"ipc_factor":1.6918,
                       "periodo_ipc":"2026-Q1"},
             "comparables_rntp":[{"posicion":1,"distrito":"Miraflores","precio_total_pen":800000.0}],
             "resumen_rntp":{"cantidad_muestras":3,"cumple_regla_tres_muestras":true}}""";

    private static final CaracteristicasInmueble CASA = new CaracteristicasInmueble(TipoInmueble.DEPARTAMENTO,
            (short) 16, new BigDecimal("120"), 3, new BigDecimal("2.0"), 1, 4, 14, true);

    private HttpServer fastApi;
    private volatile int status;
    private volatile String respuesta;
    private volatile String cuerpoRecibido;

    @BeforeEach
    void iniciarFastApiSimulado() throws IOException {
        fastApi = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        fastApi.createContext("/api/v1/prediccion/venta", intercambio -> {
            cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(status, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        fastApi.start();
    }

    @AfterEach
    void detener() {
        fastApi.stop(0);
    }

    private static MotorEstimacionMlAdapter adapter(int puerto) {
        ValuoProperties.Ml ml = new ValuoProperties.Ml("http://localhost:" + puerto, Duration.ofSeconds(1),
                Duration.ofSeconds(2));
        return new MotorEstimacionMlAdapter(new ValuoProperties(null, null, ml, null, null));
    }

    private MotorEstimacionMlAdapter adapter() {
        return adapter(fastApi.getAddress().getPort());
    }

    @Test
    void traduceLaRespuestaDelModeloYEnviaElContratoEsperado() {
        status = 200;
        respuesta = RESPUESTA_VENTA;

        ResultadoModelo resultado = adapter().estimar(TipoOperacion.VENTA, CASA, "Miraflores");

        assertThat(resultado.precioSoles()).isEqualByComparingTo("812343.46");
        assertThat(resultado.precioM2Soles()).isEqualByComparingTo("6769.53");
        assertThat(resultado.rangoInferiorSoles()).isEqualByComparingTo("697300.10");
        assertThat(resultado.mapePct()).isEqualByComparingTo("14.16");
        assertThat(resultado.versionModelo()).isEqualTo("v8_mlflow");
        assertThat(resultado.periodoIpc()).isEqualTo("2026-Q1");
        assertThat(resultado.explicabilidadJson()).contains("distrito_encoded");
        assertThat(resultado.comparablesJson()).contains("comparables_rntp", "cumple_regla_tres_muestras");
        assertThat(cuerpoRecibido).contains("\"distrito\":\"Miraflores\"", "\"banios\":2.0", "\"garajes\":1",
                "\"piso\":4","\"vista_exterior\":true");
    }

    @Test
    void unRechazoDeValidacionDelModeloEsUnDatoInvalido() {
        status = 422;
        respuesta = "{\"detail\":[{\"loc\":[\"body\",\"superficie\"],\"msg\":\"Input should be greater than 0\"}]}";

        assertThatThrownBy(() -> adapter().estimar(TipoOperacion.VENTA, CASA, "Miraflores"))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("Datos de entrada inválidos para predicción");
    }

    @Test
    void modelosSinCargarEsServicioNoDisponible() {
        status = 503;
        respuesta = "{\"detail\":\"Modelos no inicializados\"}";

        assertThatThrownBy(() -> adapter().estimar(TipoOperacion.VENTA, CASA, "Miraflores"))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessage("Servicio de predicción no disponible, intente más tarde");
    }

    @Test
    void fastApiApagadoEsServicioNoDisponible() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }

        assertThatThrownBy(() -> adapter(puertoLibre).estimar(TipoOperacion.VENTA, CASA, "Miraflores"))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
