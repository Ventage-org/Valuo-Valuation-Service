package pe.valuo.msprediccion.application.estimacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.valuo.msprediccion.application.estimacion.result.EstimacionResult;
import pe.valuo.msprediccion.application.exception.ServicioExternoException;
import pe.valuo.msprediccion.application.port.CarteraInmuebles;
import pe.valuo.msprediccion.application.port.MotorEstimacion;
import pe.valuo.msprediccion.application.port.ProveedorTipoCambio;
import pe.valuo.msprediccion.domain.exception.DistritoNoEncontradoException;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;
import pe.valuo.msprediccion.domain.model.distrito.Distrito;
import pe.valuo.msprediccion.domain.model.estimacion.CaracteristicasInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.Estimacion;
import pe.valuo.msprediccion.domain.model.estimacion.ResultadoModelo;
import pe.valuo.msprediccion.domain.model.estimacion.TipoInmueble;
import pe.valuo.msprediccion.domain.model.estimacion.TipoOperacion;
import pe.valuo.msprediccion.domain.repository.DistritoRepository;
import pe.valuo.msprediccion.domain.repository.EstimacionRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarEstimacionUseCaseTest {

    private static final UUID AGENTE = UUID.randomUUID();
    private static final CaracteristicasInmueble DEPARTAMENTO = new CaracteristicasInmueble(TipoInmueble.DEPARTAMENTO,
            (short) 9, new BigDecimal("100"), 3, new BigDecimal("2.0"), 1, 5, 10, true);
    private static final DatosCartera CARTERA = new DatosCartera("Depa en Jesús María", null, new BigDecimal("100"),
            false, "Av. Brasil 1200", null, null, null, null);
    private static final ResultadoModelo RESULTADO = new ResultadoModelo(new BigDecimal("750000.00"),
            new BigDecimal("7500.00"), new BigDecimal("650000.00"), new BigDecimal("850000.00"), new BigDecimal("14.16"),
            "v8_mlflow", "2026-Q1", "{\"contribuciones\":[]}", null);

    @Mock DistritoRepository distritoRepository;
    @Mock EstimacionRepository estimacionRepository;
    @Mock MotorEstimacion motorEstimacion;
    @Mock CarteraInmuebles carteraInmuebles;
    @Mock ProveedorTipoCambio proveedorTipoCambio;

    RegistrarEstimacionUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new RegistrarEstimacionUseCase(distritoRepository, estimacionRepository, motorEstimacion,
                carteraInmuebles, proveedorTipoCambio);
    }

    private void conDistritoJesusMaria() {
        Distrito distrito = mock(Distrito.class);
        lenient().when(distrito.getNombre()).thenReturn("Jesús María");
        lenient().when(distrito.getNombreModelo()).thenReturn("Jesus Maria");
        when(distritoRepository.buscarActivoPorId((short) 9)).thenReturn(Optional.of(distrito));
    }

    private static RegistrarEstimacionCommand command(boolean guardarEnCartera) {
        return new RegistrarEstimacionCommand(AGENTE, "token", TipoOperacion.VENTA, DEPARTAMENTO, guardarEnCartera,
                CARTERA);
    }

    @Test
    void estimaConElNombreDelModeloYConvierteADolares() {
        conDistritoJesusMaria();
        when(motorEstimacion.estimar(TipoOperacion.VENTA, DEPARTAMENTO, "Jesus Maria")).thenReturn(RESULTADO);
        when(proveedorTipoCambio.penPorUsd()).thenReturn(new BigDecimal("3.75"));
        when(estimacionRepository.guardar(any())).thenAnswer(i -> i.getArgument(0));

        EstimacionResult resultado = useCase.ejecutar(command(false));

        assertThat(resultado.distritoNombre()).isEqualTo("Jesús María");
        assertThat(resultado.valores().valorEstimadoUsd()).isEqualByComparingTo("200000.00");
        assertThat(resultado.valores().rangoSuperiorUsd()).isEqualByComparingTo("226666.67");
        assertThat(resultado.inmuebleId()).isNull();
        verifyNoInteractions(carteraInmuebles);
    }

    @Test
    void alGuardarEnCarteraConservaElIdDelInmueble() {
        UUID inmueble = UUID.randomUUID();
        conDistritoJesusMaria();
        when(motorEstimacion.estimar(any(), any(), any())).thenReturn(RESULTADO);
        when(carteraInmuebles.registrar("token", DEPARTAMENTO, CARTERA)).thenReturn(inmueble);
        when(proveedorTipoCambio.penPorUsd()).thenReturn(new BigDecimal("3.75"));
        when(estimacionRepository.guardar(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(useCase.ejecutar(command(true)).inmuebleId()).isEqualTo(inmueble);
    }

    @Test
    void siLaCarteraRechazaElInmuebleNoSeGuardaLaEstimacion() {
        conDistritoJesusMaria();
        when(motorEstimacion.estimar(any(), any(), any())).thenReturn(RESULTADO);
        when(carteraInmuebles.registrar(any(), any(), any()))
                .thenThrow(new ServicioExternoException(400, "Datos de entrada inválidos", null));

        assertThatThrownBy(() -> useCase.ejecutar(command(true))).isInstanceOf(ServicioExternoException.class);
        verify(estimacionRepository, never()).guardar(any());
    }

    @Test
    void unaCasaSeRechazaSinLlegarAlModeloNiAlDistrito() {
        CaracteristicasInmueble casa = new CaracteristicasInmueble(TipoInmueble.CASA, (short) 9,
                new BigDecimal("100"), 3, new BigDecimal("2.0"), 1, null, 10, true);
        RegistrarEstimacionCommand command = new RegistrarEstimacionCommand(AGENTE, "token", TipoOperacion.VENTA,
                casa, false, CARTERA);

        assertThatThrownBy(() -> useCase.ejecutar(command)).isInstanceOf(ValorInvalidoException.class)
                .hasMessage("La estimación solo está disponible para departamentos");
        verifyNoInteractions(distritoRepository, motorEstimacion);
    }

    @Test
    void retasacionRechazaSiNoHayCambiosEnCaracteristicas() {
        UUID inmueble = UUID.randomUUID();
        conDistritoJesusMaria();
        Estimacion previa = Estimacion.registrar(AGENTE, TipoOperacion.VENTA, DEPARTAMENTO, "Jesús María",
                "Depa", "Av. Brasil", RESULTADO, new BigDecimal("3.75"), inmueble);
        when(estimacionRepository.buscarUltimaPorInmueble(AGENTE, inmueble)).thenReturn(Optional.of(previa));

        RegistrarEstimacionCommand command = new RegistrarEstimacionCommand(AGENTE, "token", TipoOperacion.VENTA,
                DEPARTAMENTO, false, inmueble, CARTERA);

        assertThatThrownBy(() -> useCase.ejecutar(command))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("Las características del inmueble no han cambiado. No requiere nueva tasación.");
        verifyNoInteractions(motorEstimacion);
        verify(estimacionRepository, never()).guardar(any());
    }

    @Test
    void retasacionPermiteSiCambiaronCaracteristicas() {
        UUID inmueble = UUID.randomUUID();
        conDistritoJesusMaria();
        CaracteristicasInmueble previas = new CaracteristicasInmueble(TipoInmueble.DEPARTAMENTO,
                (short) 9, new BigDecimal("80"), 2, new BigDecimal("1.0"), 0, 3, 5, false);
        Estimacion previa = Estimacion.registrar(AGENTE, TipoOperacion.VENTA, previas, "Jesús María",
                "Depa", "Av. Brasil", RESULTADO, new BigDecimal("3.75"), inmueble);
        when(estimacionRepository.buscarUltimaPorInmueble(AGENTE, inmueble)).thenReturn(Optional.of(previa));
        when(motorEstimacion.estimar(any(), any(), any())).thenReturn(RESULTADO);
        when(proveedorTipoCambio.penPorUsd()).thenReturn(new BigDecimal("3.75"));
        when(estimacionRepository.guardar(any())).thenAnswer(i -> i.getArgument(0));

        RegistrarEstimacionCommand command = new RegistrarEstimacionCommand(AGENTE, "token", TipoOperacion.VENTA,
                DEPARTAMENTO, false, inmueble, CARTERA);

        EstimacionResult resultado = useCase.ejecutar(command);
        assertThat(resultado.inmuebleId()).isEqualTo(inmueble);
        verifyNoInteractions(carteraInmuebles);
        verify(estimacionRepository).guardar(any());
    }

    @Test
    void unDistritoInexistenteNoLlegaAlModelo() {
        when(distritoRepository.buscarActivoPorId((short) 9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(command(false))).isInstanceOf(DistritoNoEncontradoException.class);
        verifyNoInteractions(motorEstimacion);
    }
}
