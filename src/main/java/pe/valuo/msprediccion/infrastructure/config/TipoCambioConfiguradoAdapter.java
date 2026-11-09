package pe.valuo.msprediccion.infrastructure.config;

import org.springframework.stereotype.Component;
import pe.valuo.msprediccion.application.port.ProveedorTipoCambio;

import java.math.BigDecimal;

/** Tipo de cambio fijo de la configuración. Para usar el del BCRP basta con otro adaptador de este puerto. */
@Component
class TipoCambioConfiguradoAdapter implements ProveedorTipoCambio {

    private final BigDecimal penPorUsd;

    TipoCambioConfiguradoAdapter(ValuoProperties properties) {
        this.penPorUsd = properties.tipoCambio().penPorUsd();
        if (penPorUsd == null || penPorUsd.signum() <= 0) {
            throw new IllegalStateException("valuo.tipo-cambio.pen-por-usd debe ser mayor a 0");
        }
    }

    @Override
    public BigDecimal penPorUsd() {
        return penPorUsd;
    }
}
