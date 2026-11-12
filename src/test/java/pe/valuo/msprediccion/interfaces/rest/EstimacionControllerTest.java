package pe.valuo.msprediccion.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pe.valuo.msprediccion.application.estimacion.ConsultarEstimacionUseCase;
import pe.valuo.msprediccion.application.estimacion.ConsultarKpisUseCase;
import pe.valuo.msprediccion.application.estimacion.ListarEstimacionesUseCase;
import pe.valuo.msprediccion.application.estimacion.RegistrarEstimacionUseCase;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResult;
import pe.valuo.msprediccion.application.exception.ServicioNoDisponibleException;
import pe.valuo.msprediccion.application.port.PlataformaMl;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.infrastructure.config.PropiedadesConfig;
import pe.valuo.msprediccion.infrastructure.security.JwtConfig;
import pe.valuo.msprediccion.infrastructure.security.SecurityConfig;
import pe.valuo.msprediccion.interfaces.rest.error.GlobalExceptionHandler;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EstimacionController.class, ModeloController.class})
@Import({SecurityConfig.class, JwtConfig.class, PropiedadesConfig.class, GlobalExceptionHandler.class})
class EstimacionControllerTest {

    private static final UUID AGENTE = UUID.randomUUID();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mockMvc;
    @MockitoBean RegistrarEstimacionUseCase registrarEstimacion;
    @MockitoBean ListarEstimacionesUseCase listarEstimaciones;
    @MockitoBean ConsultarEstimacionUseCase consultarEstimacion;
    @MockitoBean ConsultarKpisUseCase consultarKpis;
    @MockitoBean PlataformaMl plataformaMl;

    private static RequestPostProcessor agente() {
        return jwt().jwt(j -> j.subject(AGENTE.toString())).authorities(new SimpleGrantedAuthority("ROLE_AGENTE"));
    }

    private static String formulario(String tipo, String piso) {
        return """
                {"tipoPrediccion":"VENTA","guardarEnCartera":false,"tipoInmueble":"%s","distritoId":16,
                 "superficieTotalM2":110.0,"habitaciones":3,"banos":2.5,"estacionamientos":2,"piso":%s,
                 "antiguedadAnios":2,"vistaExterior":true}""".formatted(tipo, piso);
    }

    private static EstimacionResult estimacion() {
        CaracteristicasInmueble datos = new CaracteristicasInmueble(TipoInmueble.DEPARTAMENTO, (short) 16,
                new BigDecimal("110"), 3, new BigDecimal("2.5"), 2, 4, 2, true);
        ResultadoModelo modelo = new ResultadoModelo(new BigDecimal("750000.00"), new BigDecimal("6818.18"), null,
                null, new BigDecimal("14.16"), "v8_mlflow", "2026-Q1",
                "{\"contribuciones\":[{\"feature\":\"distrito_encoded\"}]}", null);
        return EstimacionResult.de(Estimacion.registrar(AGENTE, TipoOperacion.VENTA, datos, "Miraflores", null, null,
                modelo, new BigDecimal("3.75"), null));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/estimaciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void estimacionValidaDevuelve201ConDolaresYExplicacionShap() throws Exception {
        when(registrarEstimacion.ejecutar(any())).thenReturn(estimacion());

        mockMvc.perform(post("/api/v1/estimaciones").with(agente()).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario("DEPARTAMENTO", "4")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valores.valorEstimadoUsd").value(200000.0))
                .andExpect(jsonPath("$.explicabilidad.contribuciones[0].feature").value("distrito_encoded"));
    }

    @Test
    void unaCasaConPisoDevuelve400PorReglaDeDominio() throws Exception {
        mockMvc.perform(post("/api/v1/estimaciones").with(agente()).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario("CASA", "3")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unaCasaSinPisoDevuelve400ConElMensajeDeSoloDepartamentos() throws Exception {
        when(registrarEstimacion.ejecutar(any()))
                .thenThrow(new ValorInvalidoException("La estimación solo está disponible para departamentos"));

        mockMvc.perform(post("/api/v1/estimaciones").with(agente()).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario("CASA", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La estimación solo está disponible para departamentos"));
    }

    @Test
    void modeloCaidoDevuelve503ConMensajeAmigable() throws Exception {
        when(registrarEstimacion.ejecutar(any()))
                .thenThrow(new ServicioNoDisponibleException("Servicio de predicción no disponible, intente más tarde"));

        mockMvc.perform(post("/api/v1/estimaciones").with(agente()).contentType(MediaType.APPLICATION_JSON)
                        .content(formulario("DEPARTAMENTO", "4")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Servicio de predicción no disponible, intente más tarde"));
    }

    @Test
    void tipoDeOperacionInvalidoEnElHistorialDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/estimaciones").with(agente()).param("tipoOperacion", "permuta"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laSensibilidadDeFactoresEsPublica() throws Exception {
        when(plataformaMl.consultar(eq("/api/v1/modelos/sensibilidad-factores/venta"), any()))
                .thenReturn(JSON.readTree("{\"tipo_operacion\":\"venta\"}"));

        mockMvc.perform(get("/api/v1/modelos/sensibilidad-factores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo_operacion").value("venta"));
    }

    @Test
    void lasRutasAdminYaNoExistenEnEstimaciones() throws Exception {
        mockMvc.perform(get("/api/v1/admin/monitoreo/recursos").with(agente())).andExpect(status().isNotFound());
    }
}
