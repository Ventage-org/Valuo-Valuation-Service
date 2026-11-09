package pe.valuo.msprediccion.application.port;

import java.math.BigDecimal;

public interface ProveedorTipoCambio {

    /** Soles por dólar vigentes. */
    BigDecimal penPorUsd();
}
