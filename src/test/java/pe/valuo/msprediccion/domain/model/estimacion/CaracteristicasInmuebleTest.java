package pe.valuo.msprediccion.domain.model.estimacion;

import org.junit.jupiter.api.Test;
import pe.valuo.msprediccion.domain.exception.ValorInvalidoException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaracteristicasInmuebleTest {

    private static CaracteristicasInmueble crear(TipoInmueble tipo, Integer piso, String banos) {
        return new CaracteristicasInmueble(tipo, (short) 16, new BigDecimal("110"), 3, new BigDecimal(banos), 1, piso,
                5, true);
    }

    @Test
    void departamentoValidoSeCrea() {
        assertThatCode(() -> crear(TipoInmueble.DEPARTAMENTO, 4, "2.5")).doesNotThrowAnyException();
    }

    @Test
    void unaCasaNoTienePiso() {
        assertThatThrownBy(() -> crear(TipoInmueble.CASA, 2, "2.0")).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void unDepartamentoExigePiso() {
        assertThatThrownBy(() -> crear(TipoInmueble.DEPARTAMENTO, null, "2.0")).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void losBanosVanEnPasosDeMedio() {
        assertThatThrownBy(() -> crear(TipoInmueble.CASA, null, "1.25")).isInstanceOf(ValorInvalidoException.class);
    }
}
